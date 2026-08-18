package com.cryptopulse.source;

import java.math.BigDecimal;
import java.time.Instant;

public record RawTickerData(
    String symbol,
    String name,
    BigDecimal priceUsd,
    BigDecimal volume24h,
    BigDecimal percentChange24h,
    BigDecimal marketCapUsd,
    String source,
    String externalId,
    Instant lastUpdated
) {}
