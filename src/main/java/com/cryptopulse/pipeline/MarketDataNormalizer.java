package com.cryptopulse.pipeline;

import com.cryptopulse.model.MarketTicker;
import com.cryptopulse.source.RawTickerData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Component
public class MarketDataNormalizer {

    public MarketTicker normalize(RawTickerData raw) {
        if (raw == null) {
            return null;
        }

        String symbol = raw.symbol() != null ? raw.symbol().trim().toUpperCase() : "UNKNOWN";
        String name = raw.name() != null ? raw.name().trim() : symbol;
        if (name.length() > 100) {
            name = name.substring(0, 97) + "...";
        }

        BigDecimal price = raw.priceUsd() != null ? raw.priceUsd().setScale(8, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal volume = raw.volume24h() != null ? raw.volume24h().setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal change = raw.percentChange24h() != null ? raw.percentChange24h().setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal cap = raw.marketCapUsd() != null ? raw.marketCapUsd().setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        String source = raw.source() != null ? raw.source().trim().toLowerCase() : "unknown";
        String externalId = raw.externalId() != null ? raw.externalId().trim() : symbol;

        Instant updated = raw.lastUpdated() != null ? raw.lastUpdated() : Instant.now();
        Instant fetched = Instant.now();

        return new MarketTicker(symbol, name, price, volume, change, cap, source, externalId, updated, fetched);
    }
}
