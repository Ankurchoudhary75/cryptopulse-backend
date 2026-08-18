package com.cryptopulse.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "market_tickers",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_symbol_source", columnNames = {"symbol", "source"})
    },
    indexes = {
        @Index(name = "idx_ticker_symbol", columnList = "symbol"),
        @Index(name = "idx_ticker_source", columnList = "source"),
        @Index(name = "idx_ticker_updated", columnList = "last_updated")
    }
)
public class MarketTicker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "price_usd", nullable = false, precision = 24, scale = 8)
    private BigDecimal priceUsd;

    @Column(name = "volume_24h", precision = 24, scale = 4)
    private BigDecimal volume24h;

    @Column(name = "percent_change_24h", precision = 8, scale = 4)
    private BigDecimal percentChange24h;

    @Column(name = "market_cap_usd", precision = 24, scale = 4)
    private BigDecimal marketCapUsd;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    public MarketTicker() {}

    public MarketTicker(String symbol, String name, BigDecimal priceUsd, BigDecimal volume24h,
                        BigDecimal percentChange24h, BigDecimal marketCapUsd, String source,
                        String externalId, Instant lastUpdated, Instant fetchedAt) {
        this.symbol = symbol;
        this.name = name;
        this.priceUsd = priceUsd;
        this.volume24h = volume24h;
        this.percentChange24h = percentChange24h;
        this.marketCapUsd = marketCapUsd;
        this.source = source;
        this.externalId = externalId;
        this.lastUpdated = lastUpdated;
        this.fetchedAt = fetchedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPriceUsd() {
        return priceUsd;
    }

    public void setPriceUsd(BigDecimal priceUsd) {
        this.priceUsd = priceUsd;
    }

    public BigDecimal getVolume24h() {
        return volume24h;
    }

    public void setVolume24h(BigDecimal volume24h) {
        this.volume24h = volume24h;
    }

    public BigDecimal getPercentChange24h() {
        return percentChange24h;
    }

    public void setPercentChange24h(BigDecimal percentChange24h) {
        this.percentChange24h = percentChange24h;
    }

    public BigDecimal getMarketCapUsd() {
        return marketCapUsd;
    }

    public void setMarketCapUsd(BigDecimal marketCapUsd) {
        this.marketCapUsd = marketCapUsd;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketTicker that = (MarketTicker) o;
        return Objects.equals(symbol, that.symbol) && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, source);
    }
}
