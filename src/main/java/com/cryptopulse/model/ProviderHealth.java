package com.cryptopulse.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "provider_health",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_provider_name", columnNames = {"provider_name"})
    }
)
public class ProviderHealth {

    public enum HealthStatus {
        HEALTHY,
        DEGRADED,
        UNHEALTHY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HealthStatus status;

    @Column(name = "consecutive_failures", nullable = false)
    private int consecutiveFailures;

    @Column(name = "total_requests", nullable = false)
    private long totalRequests;

    @Column(name = "failed_requests", nullable = false)
    private long failedRequests;

    @Column(name = "last_success_at")
    private Instant lastSuccessAt;

    @Column(name = "last_error_at")
    private Instant lastErrorAt;

    @Column(name = "last_error_message", length = 500)
    private String lastErrorMessage;

    public ProviderHealth() {}

    public ProviderHealth(String providerName, HealthStatus status) {
        this.providerName = providerName;
        this.status = status;
        this.consecutiveFailures = 0;
        this.totalRequests = 0;
        this.failedRequests = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public HealthStatus getStatus() {
        return status;
    }

    public void setStatus(HealthStatus status) {
        this.status = status;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public void setConsecutiveFailures(int consecutiveFailures) {
        this.consecutiveFailures = consecutiveFailures;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getFailedRequests() {
        return failedRequests;
    }

    public void setFailedRequests(long failedRequests) {
        this.failedRequests = failedRequests;
    }

    public Instant getLastSuccessAt() {
        return lastSuccessAt;
    }

    public void setLastSuccessAt(Instant lastSuccessAt) {
        this.lastSuccessAt = lastSuccessAt;
    }

    public Instant getLastErrorAt() {
        return lastErrorAt;
    }

    public void setLastErrorAt(Instant lastErrorAt) {
        this.lastErrorAt = lastErrorAt;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }

    public void recordSuccess() {
        this.totalRequests++;
        this.consecutiveFailures = 0;
        this.status = HealthStatus.HEALTHY;
        this.lastSuccessAt = Instant.now();
    }

    public void recordFailure(String errorMessage) {
        this.totalRequests++;
        this.failedRequests++;
        this.consecutiveFailures++;
        this.lastErrorAt = Instant.now();
        this.lastErrorMessage = errorMessage;
        if (this.consecutiveFailures >= 3) {
            this.status = HealthStatus.UNHEALTHY;
        } else {
            this.status = HealthStatus.DEGRADED;
        }
    }
}
