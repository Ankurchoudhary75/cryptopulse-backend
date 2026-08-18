package com.cryptopulse.pipeline;

import com.cryptopulse.model.MarketTicker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MarketDataValidatorTest {

    private MarketDataValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MarketDataValidator();
    }

    @Test
    void isValid_validTicker_returnsTrue() {
        MarketTicker ticker = new MarketTicker(
                "ETH",
                "Ethereum",
                new BigDecimal("2800.50"),
                new BigDecimal("500000.00"),
                new BigDecimal("2.10"),
                new BigDecimal("350000000.00"),
                "coingecko",
                "ethereum",
                Instant.now(),
                Instant.now()
        );

        assertTrue(validator.isValid(ticker));
    }

    @Test
    void isValid_zeroOrNegativePrice_returnsFalse() {
        MarketTicker ticker = new MarketTicker(
                "ETH",
                "Ethereum",
                BigDecimal.ZERO,
                new BigDecimal("500000.00"),
                new BigDecimal("2.10"),
                new BigDecimal("350000000.00"),
                "coingecko",
                "ethereum",
                Instant.now(),
                Instant.now()
        );

        assertFalse(validator.isValid(ticker));
    }

    @Test
    void isValid_nullSymbol_returnsFalse() {
        MarketTicker ticker = new MarketTicker(
                null,
                "Ethereum",
                new BigDecimal("2800.50"),
                new BigDecimal("500000.00"),
                new BigDecimal("2.10"),
                new BigDecimal("350000000.00"),
                "coingecko",
                "ethereum",
                Instant.now(),
                Instant.now()
        );

        assertFalse(validator.isValid(ticker));
    }
}
