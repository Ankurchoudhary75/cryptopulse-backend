package com.cryptopulse.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class CoinGeckoAdapter implements MarketSourceAdapter {

    private static final Logger log = LoggerFactory.getLogger(CoinGeckoAdapter.class);
    public static final String PROVIDER_NAME = "coingecko";
    private static final String API_URL = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=50&page=1&sparkline=false";

    private final RestTemplate restTemplate;

    public CoinGeckoAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public int getPriority() {
        return 1; // Primary Source
    }

    @Override
    public List<RawTickerData> fetchTickers() throws SourceFetchException {
        log.info("Fetching raw market tickers from CoinGecko API: {}", API_URL);
        try {
            CoinGeckoItem[] response = restTemplate.getForObject(API_URL, CoinGeckoItem[].class);
            if (response == null || response.length == 0) {
                log.warn("Received empty response array from CoinGecko");
                return List.of();
            }

            List<RawTickerData> tickers = new ArrayList<>();
            for (CoinGeckoItem item : response) {
                if (item.symbol == null || item.currentPrice == null) {
                    continue;
                }
                Instant updated = item.lastUpdated != null ? Instant.parse(item.lastUpdated) : Instant.now();
                tickers.add(new RawTickerData(
                        item.symbol.toUpperCase(),
                        item.name != null ? item.name : item.symbol,
                        item.currentPrice,
                        item.totalVolume,
                        item.priceChangePercentage24h,
                        item.marketCap,
                        PROVIDER_NAME,
                        item.id,
                        updated
                ));
            }
            log.info("Successfully fetched {} raw tickers from CoinGecko", tickers.size());
            return tickers;
        } catch (HttpStatusCodeException e) {
            log.error("HTTP error from CoinGecko [status={}]: {}", e.getStatusCode(), e.getMessage());
            throw new SourceFetchException("CoinGecko API returned HTTP status " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Failed to fetch from CoinGecko: {}", e.getMessage(), e);
            throw new SourceFetchException("Failed to fetch market data from CoinGecko", e);
        }
    }

    @Override
    public boolean checkHealth() {
        try {
            restTemplate.headForHeaders("https://api.coingecko.com/api/v3/ping");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class CoinGeckoItem {
        public String id;
        public String symbol;
        public String name;
        @JsonProperty("current_price")
        public BigDecimal currentPrice;
        @JsonProperty("market_cap")
        public BigDecimal marketCap;
        @JsonProperty("total_volume")
        public BigDecimal totalVolume;
        @JsonProperty("price_change_percentage_24h")
        public BigDecimal priceChangePercentage24h;
        @JsonProperty("last_updated")
        public String lastUpdated;
    }
}
