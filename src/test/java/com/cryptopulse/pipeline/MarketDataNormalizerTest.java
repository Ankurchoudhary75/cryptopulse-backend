package com.cryptopulse.pipeline;

import com.cryptopulse.model.MarketTicker;
import com.cryptopulse.source.RawTickerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MarketDataNormalizerTest {

    private MarketDataNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new MarketDataNormalizer();
    }

    @Test
    void normalize_validRawData_normalizesSymbolAndScale() {
        RawTickerData raw = new RawTickerData(
                " btc ",
                " Bitcoin ",
                new BigDecimal("95000.123456789"),
                new BigDecimal("1000000.55"),
                new BigDecimal("4.5"),
                new BigDecimal("1800000000"),
                "coingecko",
                "bitcoin",
                Instant.now()
        );

        MarketTicker ticker = normalizer.normalize(raw);

        assertNotNull(ticker);
        assertEquals("BTC", ticker.getSymbol());
        assertEquals("Bitcoin", ticker.getName());
        assertEquals(8, ticker.getPriceUsd().scale());
        assertEquals("coingecko", ticker.getSource());
    }

    @Test
    void normalize_nullRawData_returnsNull() {
        assertNull(normalizer.normalize(null));
    }

    @Test
    void normalize_longName_truncatesName() {
        String longName = "A".repeat(150);
        RawTickerData raw = new RawTickerData(
                "TEST",
                longName,
                BigDecimal.TEN,
                BigDecimal.ONE,
                BigDecimal.ZERO,
                BigDecimal.TEN,
                "coincap",
                "test-id",
                Instant.now()
        );

        MarketTicker ticker = normalizer.normalize(raw);

        assertNotNull(ticker);
        assertTrue(ticker.getName().length() <= 100);
        assertTrue(ticker.getName().endsWith("..."));
    }
}
