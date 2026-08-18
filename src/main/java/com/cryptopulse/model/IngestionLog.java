package com.cryptopulse.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "ingestion_logs",
    indexes = {
        @Index(name = "idx_ingestion_started", columnList = "started_at"),
        @Index(name = "idx_ingestion_status", columnList = "status")
    }
)
public class IngestionLog {

    public enum Status {
        SUCCESS,
        FAILED,
        RATE_LIMITED,
        DEGRADED_FALLBACK
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    @Column(name = "source_used", nullable = false, length = 50)
    private String sourceUsed;

    @Column(name = "fetched_count", nullable = false)
    private int fetchedCount;

    @Column(name = "new_count", nullable = false)
    private int newCount;

    @Column(name = "duplicate_count", nullable = false)
    private int duplicateCount;

    @Column(name = "anomaly_count", nullable = false)
    private int anomalyCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "execution_duration_ms", nullable = false)
    private long executionDurationMs;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    public IngestionLog() {}

    public IngestionLog(Status status, String sourceUsed, int fetchedCount, int newCount,
                        int duplicateCount, int anomalyCount, String errorMessage,
                        long executionDurationMs, Instant startedAt, Instant completedAt) {
        this.status = status;
        this.sourceUsed = sourceUsed;
        this.fetchedCount = fetchedCount;
        this.newCount = newCount;
        this.duplicateCount = duplicateCount;
        this.anomalyCount = anomalyCount;
        this.errorMessage = errorMessage;
        this.executionDurationMs = executionDurationMs;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getSourceUsed() {
        return sourceUsed;
    }

    public void setSourceUsed(String sourceUsed) {
        this.sourceUsed = sourceUsed;
    }

    public int getFetchedCount() {
        return fetchedCount;
    }

    public void setFetchedCount(int fetchedCount) {
        this.fetchedCount = fetchedCount;
    }

    public int getNewCount() {
        return newCount;
    }

    public void setNewCount(int newCount) {
        this.newCount = newCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public int getAnomalyCount() {
        return anomalyCount;
    }

    public void setAnomalyCount(int anomalyCount) {
        this.anomalyCount = anomalyCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
