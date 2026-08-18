package com.cryptopulse.source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CoinGeckoAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private CoinGeckoAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new CoinGeckoAdapter(restTemplate);
    }

    @Test
    void fetchTickers_successfulResponse_returnsRawTickerList() {
        CoinGeckoAdapter.CoinGeckoItem item = new CoinGeckoAdapter.CoinGeckoItem();
        item.id = "bitcoin";
        item.symbol = "btc";
        item.name = "Bitcoin";
        item.currentPrice = new BigDecimal("95000.00");
        item.totalVolume = new BigDecimal("30000000.00");
        item.priceChangePercentage24h = new BigDecimal("2.5");
        item.marketCap = new BigDecimal("1800000000000");

        when(restTemplate.getForObject(anyString(), eq(CoinGeckoAdapter.CoinGeckoItem[].class)))
                .thenReturn(new CoinGeckoAdapter.CoinGeckoItem[]{item});

        List<RawTickerData> tickers = adapter.fetchTickers();

        assertEquals(1, tickers.size());
        assertEquals("BTC", tickers.get(0).symbol());
        assertEquals("Bitcoin", tickers.get(0).name());
        assertEquals("coingecko", tickers.get(0).source());
    }

    @Test
    void fetchTickers_apiException_throwsSourceFetchException() {
        when(restTemplate.getForObject(anyString(), eq(CoinGeckoAdapter.CoinGeckoItem[].class)))
                .thenThrow(new RestClientException("Connection timed out"));

        assertThrows(SourceFetchException.class, () -> adapter.fetchTickers());
    }
}
