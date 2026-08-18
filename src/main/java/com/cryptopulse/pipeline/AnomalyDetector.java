package com.cryptopulse.pipeline;

import com.cryptopulse.config.IngestionProperties;
import com.cryptopulse.model.MarketAnomaly;
import com.cryptopulse.model.MarketTicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Component
public class AnomalyDetector {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetector.class);

    private final IngestionProperties properties;

    public AnomalyDetector(IngestionProperties properties) {
        this.properties = properties;
    }

    public Optional<MarketAnomaly> evaluateTicker(MarketTicker freshTicker, MarketTicker previousTicker) {
        if (freshTicker == null || freshTicker.getPercentChange24h() == null) {
            return Optional.empty();
        }

        double percentChange = freshTicker.getPercentChange24h().doubleValue();
        double surgeThreshold = properties.getSurgeThresholdPercent();
        double crashThreshold = properties.getCrashThresholdPercent();

        MarketAnomaly.AnomalyType type = null;
        MarketAnomaly.Severity severity = MarketAnomaly.Severity.LOW;
        double thresholdUsed = 0.0;

        if (percentChange >= surgeThreshold) {
            type = MarketAnomaly.AnomalyType.SURGE;
            thresholdUsed = surgeThreshold;
            if (percentChange >= surgeThreshold * 3) {
                severity = MarketAnomaly.Severity.CRITICAL;
            } else if (percentChange >= surgeThreshold * 2) {
                severity = MarketAnomaly.Severity.HIGH;
            } else {
                severity = MarketAnomaly.Severity.MEDIUM;
            }
        } else if (percentChange <= crashThreshold) {
            type = MarketAnomaly.AnomalyType.CRASH;
            thresholdUsed = crashThreshold;
            if (percentChange <= crashThreshold * 3) {
                severity = MarketAnomaly.Severity.CRITICAL;
            } else if (percentChange <= crashThreshold * 2) {
                severity = MarketAnomaly.Severity.HIGH;
            } else {
                severity = MarketAnomaly.Severity.MEDIUM;
            }
        }

        if (type == null) {
            return Optional.empty();
        }

        log.info("Detected Market Anomaly [symbol={}, type={}, change={}%]",
                freshTicker.getSymbol(), type, percentChange);

        return Optional.of(new MarketAnomaly(
                freshTicker.getSymbol(),
                type,
                freshTicker.getPriceUsd(),
                freshTicker.getPercentChange24h(),
                BigDecimal.valueOf(thresholdUsed),
                Instant.now(),
                severity,
                freshTicker.getSource()
        ));
    }
}
