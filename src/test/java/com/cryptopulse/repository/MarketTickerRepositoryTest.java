package com.cryptopulse.repository;

import com.cryptopulse.model.MarketTicker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MarketTickerRepositoryTest {

    @Autowired
    private MarketTickerRepository tickerRepository;

    @Test
    void findWithFilters_keywordSearch_returnsMatchingTicker() {
        MarketTicker btc = new MarketTicker("BTC", "Bitcoin", new BigDecimal("95000.00"), BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("1800000000.00"), "coingecko", "bitcoin", Instant.now(), Instant.now());
        MarketTicker eth = new MarketTicker("ETH", "Ethereum", new BigDecimal("2700.00"), BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("320000000.00"), "coingecko", "ethereum", Instant.now(), Instant.now());
        tickerRepository.save(btc);
        tickerRepository.save(eth);

        Page<MarketTicker> page = tickerRepository.findWithFilters("Bit", null, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("BTC", page.getContent().get(0).getSymbol());
    }

    @Test
    void findBySymbolAndSource_found_returnsTicker() {
        MarketTicker sol = new MarketTicker("SOL", "Solana", new BigDecimal("180.00"), BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("80000000.00"), "coincap", "solana", Instant.now(), Instant.now());
        tickerRepository.save(sol);

        Optional<MarketTicker> result = tickerRepository.findBySymbolAndSource("SOL", "coincap");

        assertTrue(result.isPresent());
        assertEquals("Solana", result.get().getName());
    }
}
