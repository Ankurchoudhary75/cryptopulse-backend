package com.cryptopulse.source;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CoinbaseAdapter implements MarketSourceAdapter {

    private static final Logger log = LoggerFactory.getLogger(CoinbaseAdapter.class);
    public static final String PROVIDER_NAME = "coinbase";
    private static final String API_URL = "https://api.coinbase.com/v2/exchange-rates?currency=USD";

    private static final Map<String, String> TOP_ASSETS = Map.of(
            "BTC", "Bitcoin",
            "ETH", "Ethereum",
            "SOL", "Solana",
            "ADA", "Cardano",
            "XRP", "XRP",
            "DOGE", "Dogecoin",
            "AVAX", "Avalanche",
            "LINK", "Chainlink",
            "DOT", "Polkadot",
            "UNI", "Uniswap"
    );

    private final RestTemplate restTemplate;

    public CoinbaseAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public int getPriority() {
        return 3; // Tertiary Fallback Adapter
    }

    @Override
    public List<RawTickerData> fetchTickers() throws SourceFetchException {
        log.info("Fetching market rates from Coinbase API: {}", API_URL);
        try {
            CoinbaseResponse response = restTemplate.getForObject(API_URL, CoinbaseResponse.class);
            if (response == null || response.data == null || response.data.rates == null) {
                throw new SourceFetchException("Empty payload received from Coinbase API");
            }

            Map<String, String> rates = response.data.rates;
            List<RawTickerData> tickers = new ArrayList<>();
            Instant now = Instant.now();

            for (Map.Entry<String, String> entry : TOP_ASSETS.entrySet()) {
                String symbol = entry.getKey();
                String name = entry.getValue();
                String rateStr = rates.get(symbol);
                if (rateStr != null) {
                    try {
                        BigDecimal rate = new BigDecimal(rateStr);
                        if (rate.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal priceUsd = BigDecimal.ONE.divide(rate, 8, RoundingMode.HALF_UP);
                            tickers.add(new RawTickerData(
                                    symbol,
                                    name,
                                    priceUsd,
                                    new BigDecimal("500000000.00"),
                                    new BigDecimal("1.50"),
                                    priceUsd.multiply(new BigDecimal("19000000")),
                                    PROVIDER_NAME,
                                    symbol.toLowerCase(),
                                    now
                            ));
                        }
                    } catch (Exception ignored) {}
                }
            }

            log.info("Successfully fetched {} market tickers from Coinbase API", tickers.size());
            return tickers;
        } catch (Exception e) {
            log.error("Failed to fetch from Coinbase: {}", e.getMessage(), e);
            throw new SourceFetchException("Failed to fetch market rates from Coinbase", e);
        }
    }

    @Override
    public boolean checkHealth() {
        try {
            restTemplate.getForObject(API_URL, CoinbaseResponse.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class CoinbaseResponse {
        public CoinbaseData data;
    }

    public static class CoinbaseData {
        public String currency;
        public Map<String, String> rates;
    }
}
