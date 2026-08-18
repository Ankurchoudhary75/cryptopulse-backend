package com.cryptopulse.pipeline;

import com.cryptopulse.config.IngestionProperties;
import com.cryptopulse.event.MarketAnomalyEvent;
import com.cryptopulse.model.*;
import com.cryptopulse.repository.*;
import com.cryptopulse.source.MarketSourceAdapter;
import com.cryptopulse.source.RawTickerData;
import com.cryptopulse.source.SourceFetchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class IngestionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(IngestionOrchestrator.class);

    private final List<MarketSourceAdapter> adapters;
    private final MarketDataNormalizer normalizer;
    private final MarketDataValidator validator;
    private final AnomalyDetector anomalyDetector;
    private final MarketTickerRepository tickerRepository;
    private final MarketAnomalyRepository anomalyRepository;
    private final IngestionLogRepository logRepository;
    private final ProviderHealthRepository healthRepository;
    private final IngestionProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public IngestionOrchestrator(List<MarketSourceAdapter> adapters,
                                 MarketDataNormalizer normalizer,
                                 MarketDataValidator validator,
                                 AnomalyDetector anomalyDetector,
                                 MarketTickerRepository tickerRepository,
                                 MarketAnomalyRepository anomalyRepository,
                                 IngestionLogRepository logRepository,
                                 ProviderHealthRepository healthRepository,
                                 IngestionProperties properties,
                                 ApplicationEventPublisher eventPublisher) {
        // Sort adapters by priority
        List<MarketSourceAdapter> sortedAdapters = new ArrayList<>(adapters);
        sortedAdapters.sort(Comparator.comparingInt(MarketSourceAdapter::getPriority));
        this.adapters = sortedAdapters;
        this.normalizer = normalizer;
        this.validator = validator;
        this.anomalyDetector = anomalyDetector;
        this.tickerRepository = tickerRepository;
        this.anomalyRepository = anomalyRepository;
        this.logRepository = logRepository;
        this.healthRepository = healthRepository;
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    public synchronized IngestionLog triggerIngestion(boolean force) {
        Instant startedAt = Instant.now();

        // 1. Single-Flight Concurrency Guard
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("Ingestion trigger rejected: Ingestion process already running");
            return createFailureLog("Ingestion already in progress", IngestionLog.Status.FAILED, "none", startedAt, Instant.now());
        }

        try {
            // 2. Cooldown Check (if not forced)
            if (!force) {
                Optional<IngestionLog> latestRun = logRepository.findLatestSuccessfulRun();
                if (latestRun.isPresent()) {
                    long minutesSinceLastRun = Duration.between(latestRun.get().getCompletedAt(), startedAt).toMinutes();
                    if (minutesSinceLastRun < properties.getCooldownMinutes()) {
                        log.info("Ingestion skipped due to active cooldown window ({} min remaining)",
                                properties.getCooldownMinutes() - minutesSinceLastRun);
                        return createFailureLog("Cooldown window active", IngestionLog.Status.RATE_LIMITED, "none", startedAt, Instant.now());
                    }
                }
            }

            // 3. Resilient Multi-Source Adapter Fetching with Failover
            List<RawTickerData> rawTickers = null;
            String sourceUsed = null;
            boolean isFallback = false;

            for (MarketSourceAdapter adapter : adapters) {
                String providerName = adapter.getProviderName();
                ProviderHealth health = healthRepository.findByProviderName(providerName)
                        .orElseGet(() -> new ProviderHealth(providerName, ProviderHealth.HealthStatus.HEALTHY));

                try {
                    log.info("Attempting market data ingestion from source adapter: {}", providerName);
                    rawTickers = adapter.fetchTickers();
                    sourceUsed = providerName;
                    health.recordSuccess();
                    healthRepository.save(health);

                    if (adapter.getPriority() > 1) {
                        isFallback = true;
                    }
                    break; // Successfully fetched
                } catch (SourceFetchException e) {
                    log.warn("Source adapter [{}] failed: {}. Evaluating failover...", providerName, e.getMessage());
                    health.recordFailure(e.getMessage());
                    healthRepository.save(health);
                }
            }

            if (rawTickers == null || sourceUsed == null) {
                log.error("All market source adapters failed!");
                return createFailureLog("All market source adapters failed", IngestionLog.Status.FAILED, "none", startedAt, Instant.now());
            }

            // 4. Normalization, Validation, Deduplication & Anomaly Detection
            int newCount = 0;
            int duplicateCount = 0;
            int anomalyCount = 0;

            for (RawTickerData raw : rawTickers) {
                MarketTicker ticker = normalizer.normalize(raw);
                if (!validator.isValid(ticker)) {
                    log.warn("Rejected invalid ticker payload: {}", raw);
                    continue;
                }

                Optional<MarketTicker> existingOpt = tickerRepository.findBySymbolAndSource(ticker.getSymbol(), ticker.getSource());
                MarketTicker savedTicker;
                if (existingOpt.isPresent()) {
                    MarketTicker existing = existingOpt.get();
                    existing.setPriceUsd(ticker.getPriceUsd());
                    existing.setVolume24h(ticker.getVolume24h());
                    existing.setPercentChange24h(ticker.getPercentChange24h());
                    existing.setMarketCapUsd(ticker.getMarketCapUsd());
                    existing.setLastUpdated(ticker.getLastUpdated());
                    existing.setFetchedAt(ticker.getFetchedAt());
                    savedTicker = tickerRepository.save(existing);
                    duplicateCount++;
                } else {
                    savedTicker = tickerRepository.save(ticker);
                    newCount++;
                }

                // 5. Evaluate Anomaly & Emit Spring Event
                Optional<MarketAnomaly> anomalyOpt = anomalyDetector.evaluateTicker(savedTicker, existingOpt.orElse(null));
                if (anomalyOpt.isPresent()) {
                    MarketAnomaly anomaly = anomalyRepository.save(anomalyOpt.get());
                    anomalyCount++;
                    eventPublisher.publishEvent(new MarketAnomalyEvent(this, anomaly));
                }
            }

            Instant completedAt = Instant.now();
            long durationMs = Duration.between(startedAt, completedAt).toMillis();

            IngestionLog.Status logStatus = isFallback ? IngestionLog.Status.DEGRADED_FALLBACK : IngestionLog.Status.SUCCESS;
            IngestionLog logEntry = new IngestionLog(
                    logStatus,
                    sourceUsed,
                    rawTickers.size(),
                    newCount,
                    duplicateCount,
                    anomalyCount,
                    null,
                    durationMs,
                    startedAt,
                    completedAt
            );
            IngestionLog savedLog = logRepository.save(logEntry);
            log.info("Ingestion completed successfully [source={}, status={}, fetched={}, new={}, duplicates={}, anomalies={}, duration={}ms]",
                    sourceUsed, logStatus, rawTickers.size(), newCount, duplicateCount, anomalyCount, durationMs);
            return savedLog;

        } finally {
            isRunning.set(false);
        }
    }

    private IngestionLog createFailureLog(String message, IngestionLog.Status status, String source, Instant startedAt, Instant completedAt) {
        long durationMs = Duration.between(startedAt, completedAt).toMillis();
        IngestionLog failedLog = new IngestionLog(status, source, 0, 0, 0, 0, message, durationMs, startedAt, completedAt);
        return logRepository.save(failedLog);
    }

    public List<ProviderHealth> getProviderHealthList() {
        return healthRepository.findAll();
    }
}
