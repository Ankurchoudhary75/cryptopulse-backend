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
public class CoinCapAdapter implements MarketSourceAdapter {

    private static final Logger log = LoggerFactory.getLogger(CoinCapAdapter.class);
    public static final String PROVIDER_NAME = "coincap";
    private static final String API_URL = "https://api.coincap.io/v2/assets?limit=50";

    private final RestTemplate restTemplate;

    public CoinCapAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public int getPriority() {
        return 2; // Secondary Failover Source
    }

    @Override
    public List<RawTickerData> fetchTickers() throws SourceFetchException {
        log.info("Fetching raw market tickers from CoinCap API (Failover Source): {}", API_URL);
        try {
            CoinCapResponse response = restTemplate.getForObject(API_URL, CoinCapResponse.class);
            if (response == null || response.data == null || response.data.isEmpty()) {
                log.warn("Received empty response data from CoinCap");
                return List.of();
            }

            List<RawTickerData> tickers = new ArrayList<>();
            Instant now = Instant.now();
            for (CoinCapItem item : response.data) {
                if (item.symbol == null || item.priceUsd == null) {
                    continue;
                }
                tickers.add(new RawTickerData(
                        item.symbol.toUpperCase(),
                        item.name != null ? item.name : item.symbol,
                        item.priceUsd,
                        item.volumeUsd24Hr,
                        item.changePercent24Hr,
                        item.marketCapUsd,
                        PROVIDER_NAME,
                        item.id,
                        now
                ));
            }
            log.info("Successfully fetched {} raw tickers from CoinCap (Failover Source)", tickers.size());
            return tickers;
        } catch (HttpStatusCodeException e) {
            log.error("HTTP error from CoinCap [status={}]: {}", e.getStatusCode(), e.getMessage());
            throw new SourceFetchException("CoinCap API returned HTTP status " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Failed to fetch from CoinCap: {}", e.getMessage(), e);
            throw new SourceFetchException("Failed to fetch market data from CoinCap", e);
        }
    }

    @Override
    public boolean checkHealth() {
        try {
            restTemplate.getForObject(API_URL, CoinCapResponse.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class CoinCapResponse {
        public List<CoinCapItem> data;
        public Long timestamp;
    }

    public static class CoinCapItem {
        public String id;
        public String symbol;
        public String name;
        public BigDecimal priceUsd;
        public BigDecimal marketCapUsd;
        @JsonProperty("volumeUsd24Hr")
        public BigDecimal volumeUsd24Hr;
        @JsonProperty("changePercent24Hr")
        public BigDecimal changePercent24Hr;
    }
}
