package com.cryptopulse.source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CoinCapAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private CoinCapAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new CoinCapAdapter(restTemplate);
    }

    @Test
    void fetchTickers_successfulResponse_returnsRawTickerList() {
        CoinCapAdapter.CoinCapItem item = new CoinCapAdapter.CoinCapItem();
        item.id = "ethereum";
        item.symbol = "ETH";
        item.name = "Ethereum";
        item.priceUsd = new BigDecimal("2700.00");
        item.volumeUsd24Hr = new BigDecimal("15000000.00");
        item.changePercent24Hr = new BigDecimal("-1.2");
        item.marketCapUsd = new BigDecimal("320000000000");

        CoinCapAdapter.CoinCapResponse response = new CoinCapAdapter.CoinCapResponse();
        response.data = List.of(item);

        when(restTemplate.getForObject(anyString(), eq(CoinCapAdapter.CoinCapResponse.class)))
                .thenReturn(response);

        List<RawTickerData> tickers = adapter.fetchTickers();

        assertEquals(1, tickers.size());
        assertEquals("ETH", tickers.get(0).symbol());
        assertEquals("coincap", tickers.get(0).source());
    }
}
