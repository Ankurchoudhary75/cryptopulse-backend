package com.cryptopulse.pipeline;

import com.cryptopulse.config.IngestionProperties;
import com.cryptopulse.model.MarketAnomaly;
import com.cryptopulse.model.MarketTicker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AnomalyDetectorTest {

    private AnomalyDetector anomalyDetector;
    private IngestionProperties properties;

    @BeforeEach
    void setUp() {
        properties = new IngestionProperties();
        properties.setSurgeThresholdPercent(5.0);
        properties.setCrashThresholdPercent(-5.0);
        anomalyDetector = new AnomalyDetector(properties);
    }

    @Test
    void evaluateTicker_surgeAboveThreshold_flagsSurgeAnomaly() {
        MarketTicker ticker = new MarketTicker(
                "SOL", "Solana", new BigDecimal("180.00"),
                BigDecimal.TEN, new BigDecimal("12.50"), BigDecimal.TEN,
                "coingecko", "solana", Instant.now(), Instant.now()
        );

        Optional<MarketAnomaly> result = anomalyDetector.evaluateTicker(ticker, null);

        assertTrue(result.isPresent());
        assertEquals("SOL", result.get().getSymbol());
        assertEquals(MarketAnomaly.AnomalyType.SURGE, result.get().getAnomalyType());
        assertEquals(MarketAnomaly.Severity.HIGH, result.get().getSeverity());
    }

    @Test
    void evaluateTicker_crashBelowThreshold_flagsCrashAnomaly() {
        MarketTicker ticker = new MarketTicker(
                "SOL", "Solana", new BigDecimal("120.00"),
                BigDecimal.TEN, new BigDecimal("-16.00"), BigDecimal.TEN,
                "coingecko", "solana", Instant.now(), Instant.now()
        );

        Optional<MarketAnomaly> result = anomalyDetector.evaluateTicker(ticker, null);

        assertTrue(result.isPresent());
        assertEquals(MarketAnomaly.AnomalyType.CRASH, result.get().getAnomalyType());
        assertEquals(MarketAnomaly.Severity.CRITICAL, result.get().getSeverity());
    }

    @Test
    void evaluateTicker_normalMovement_returnsEmpty() {
        MarketTicker ticker = new MarketTicker(
                "BTC", "Bitcoin", new BigDecimal("95000.00"),
                BigDecimal.TEN, new BigDecimal("1.20"), BigDecimal.TEN,
                "coingecko", "bitcoin", Instant.now(), Instant.now()
        );

        Optional<MarketAnomaly> result = anomalyDetector.evaluateTicker(ticker, null);

        assertFalse(result.isPresent());
    }
}
