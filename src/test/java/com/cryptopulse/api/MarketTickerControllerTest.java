package com.cryptopulse.api;

import com.cryptopulse.model.MarketTicker;
import com.cryptopulse.repository.MarketTickerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MarketTickerController.class)
class MarketTickerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MarketTickerRepository tickerRepository;

    @Test
    void getTickers_returnsPaginatedTickers() throws Exception {
        MarketTicker ticker = new MarketTicker("BTC", "Bitcoin", new BigDecimal("95000.00"), BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, "coingecko", "bitcoin", Instant.now(), Instant.now());
        when(tickerRepository.findWithFilters(any(), any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(ticker)));

        mockMvc.perform(get("/api/v1/tickers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].symbol").value("BTC"))
                .andExpect(jsonPath("$.content[0].name").value("Bitcoin"));
    }

    @Test
    void getTickerBySymbol_found_returnsTickerDetail() throws Exception {
        MarketTicker ticker = new MarketTicker("ETH", "Ethereum", new BigDecimal("2700.00"), BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, "coingecko", "ethereum", Instant.now(), Instant.now());
        when(tickerRepository.findFirstBySymbol("ETH")).thenReturn(Optional.of(ticker));

        mockMvc.perform(get("/api/v1/tickers/symbol/ETH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("ETH"))
                .andExpect(jsonPath("$.name").value("Ethereum"));
    }
}
