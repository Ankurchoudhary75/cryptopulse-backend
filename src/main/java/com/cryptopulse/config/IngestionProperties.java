package com.cryptopulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cryptopulse.ingestion")
public class IngestionProperties {

    private int cooldownMinutes = 15;
    private double surgeThresholdPercent = 5.0;
    private double crashThresholdPercent = -5.0;
    private int connectionTimeoutMs = 5000;
    private int readTimeoutMs = 8000;
    private int maxRetries = 3;
    private long backoffInitialIntervalMs = 1000;

    public int getCooldownMinutes() {
        return cooldownMinutes;
    }

    public void setCooldownMinutes(int cooldownMinutes) {
        this.cooldownMinutes = cooldownMinutes;
    }

    public double getSurgeThresholdPercent() {
        return surgeThresholdPercent;
    }

    public void setSurgeThresholdPercent(double surgeThresholdPercent) {
        this.surgeThresholdPercent = surgeThresholdPercent;
    }

    public double getCrashThresholdPercent() {
        return crashThresholdPercent;
    }

    public void setCrashThresholdPercent(double crashThresholdPercent) {
        this.crashThresholdPercent = crashThresholdPercent;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getBackoffInitialIntervalMs() {
        return backoffInitialIntervalMs;
    }

    public void setBackoffInitialIntervalMs(long backoffInitialIntervalMs) {
        this.backoffInitialIntervalMs = backoffInitialIntervalMs;
    }
}
