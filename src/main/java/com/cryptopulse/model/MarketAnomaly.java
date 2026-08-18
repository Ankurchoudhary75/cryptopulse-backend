package com.cryptopulse.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "market_anomalies",
    indexes = {
        @Index(name = "idx_anomaly_symbol", columnList = "symbol"),
        @Index(name = "idx_anomaly_detected", columnList = "detected_at"),
        @Index(name = "idx_anomaly_severity", columnList = "severity")
    }
)
public class MarketAnomaly {

    public enum AnomalyType {
        SURGE,
        CRASH,
        HIGH_VOLATILITY
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "anomaly_type", nullable = false, length = 30)
    private AnomalyType anomalyType;

    @Column(name = "price_usd", nullable = false, precision = 24, scale = 8)
    private BigDecimal priceUsd;

    @Column(name = "percent_change", nullable = false, precision = 8, scale = 4)
    private BigDecimal percentChange;

    @Column(name = "trigger_threshold", nullable = false, precision = 8, scale = 4)
    private BigDecimal triggerThreshold;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(nullable = false, length = 50)
    private String source;

    public MarketAnomaly() {}

    public MarketAnomaly(String symbol, AnomalyType anomalyType, BigDecimal priceUsd,
                         BigDecimal percentChange, BigDecimal triggerThreshold,
                         Instant detectedAt, Severity severity, String source) {
        this.symbol = symbol;
        this.anomalyType = anomalyType;
        this.priceUsd = priceUsd;
        this.percentChange = percentChange;
        this.triggerThreshold = triggerThreshold;
        this.detectedAt = detectedAt;
        this.severity = severity;
        this.source = source;
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

    public AnomalyType getAnomalyType() {
        return anomalyType;
    }

    public void setAnomalyType(AnomalyType anomalyType) {
        this.anomalyType = anomalyType;
    }

    public BigDecimal getPriceUsd() {
        return priceUsd;
    }

    public void setPriceUsd(BigDecimal priceUsd) {
        this.priceUsd = priceUsd;
    }

    public BigDecimal getPercentChange() {
        return percentChange;
    }

    public void setPercentChange(BigDecimal percentChange) {
        this.percentChange = percentChange;
    }

    public BigDecimal getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(BigDecimal triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
