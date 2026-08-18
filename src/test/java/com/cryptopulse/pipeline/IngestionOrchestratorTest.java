package com.cryptopulse.pipeline;

import com.cryptopulse.config.IngestionProperties;
import com.cryptopulse.model.IngestionLog;
import com.cryptopulse.model.MarketTicker;
import com.cryptopulse.repository.*;
import com.cryptopulse.source.CoinCapAdapter;
import com.cryptopulse.source.CoinGeckoAdapter;
import com.cryptopulse.source.RawTickerData;
import com.cryptopulse.source.SourceFetchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IngestionOrchestratorTest {

    @Mock
    private CoinGeckoAdapter primaryAdapter;
    @Mock
    private CoinCapAdapter secondaryAdapter;
    @Mock
    private MarketTickerRepository tickerRepository;
    @Mock
    private MarketAnomalyRepository anomalyRepository;
    @Mock
    private IngestionLogRepository logRepository;
    @Mock
    private ProviderHealthRepository healthRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private IngestionOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(primaryAdapter.getProviderName()).thenReturn("coingecko");
        when(primaryAdapter.getPriority()).thenReturn(1);

        when(secondaryAdapter.getProviderName()).thenReturn("coincap");
        when(secondaryAdapter.getPriority()).thenReturn(2);

        IngestionProperties properties = new IngestionProperties();
        properties.setCooldownMinutes(15);

        MarketDataNormalizer normalizer = new MarketDataNormalizer();
        MarketDataValidator validator = new MarketDataValidator();
        AnomalyDetector anomalyDetector = new AnomalyDetector(properties);

        orchestrator = new IngestionOrchestrator(
                List.of(primaryAdapter, secondaryAdapter),
                normalizer,
                validator,
                anomalyDetector,
                tickerRepository,
                anomalyRepository,
                logRepository,
                healthRepository,
                properties,
                eventPublisher
        );

        when(logRepository.save(any(IngestionLog.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void triggerIngestion_primarySourceSucceeds_ingestsFromPrimary() {
        RawTickerData raw = new RawTickerData("BTC", "Bitcoin", new BigDecimal("95000.00"), BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, "coingecko", "bitcoin", Instant.now());
        when(primaryAdapter.fetchTickers()).thenReturn(List.of(raw));
        when(tickerRepository.save(any(MarketTicker.class))).thenAnswer(i -> i.getArgument(0));

        IngestionLog log = orchestrator.triggerIngestion(true);

        assertEquals(IngestionLog.Status.SUCCESS, log.getStatus());
        assertEquals("coingecko", log.getSourceUsed());
        assertEquals(1, log.getFetchedCount());
        verify(primaryAdapter).fetchTickers();
        verify(secondaryAdapter, never()).fetchTickers();
    }

    @Test
    void triggerIngestion_primarySourceFails_fallsBackToSecondary() {
        when(primaryAdapter.fetchTickers()).thenThrow(new SourceFetchException("CoinGecko 429 Rate Limited"));

        RawTickerData rawFallback = new RawTickerData("BTC", "Bitcoin", new BigDecimal("95000.00"), BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, "coincap", "bitcoin", Instant.now());
        when(secondaryAdapter.fetchTickers()).thenReturn(List.of(rawFallback));
        when(tickerRepository.save(any(MarketTicker.class))).thenAnswer(i -> i.getArgument(0));

        IngestionLog log = orchestrator.triggerIngestion(true);

        assertEquals(IngestionLog.Status.DEGRADED_FALLBACK, log.getStatus());
        assertEquals("coincap", log.getSourceUsed());
        assertEquals(1, log.getFetchedCount());
        verify(primaryAdapter).fetchTickers();
        verify(secondaryAdapter).fetchTickers();
    }
}
