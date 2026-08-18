-- V1: Core Schema for CryptoPulse Data Pipeline & Anomaly Engine

CREATE TABLE market_tickers (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    price_usd NUMERIC(24, 8) NOT NULL,
    volume_24h NUMERIC(24, 4),
    percent_change_24h NUMERIC(8, 4),
    market_cap_usd NUMERIC(24, 4),
    source VARCHAR(50) NOT NULL,
    external_id VARCHAR(100),
    last_updated TIMESTAMP WITH TIME ZONE NOT NULL,
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_symbol_source UNIQUE (symbol, source)
);

CREATE TABLE market_anomalies (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL,
    anomaly_type VARCHAR(30) NOT NULL,
    price_usd NUMERIC(24, 8) NOT NULL,
    percent_change NUMERIC(8, 4) NOT NULL,
    trigger_threshold NUMERIC(8, 4) NOT NULL,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    severity VARCHAR(20) NOT NULL,
    source VARCHAR(50) NOT NULL
);

CREATE TABLE ingestion_logs (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(30) NOT NULL,
    source_used VARCHAR(50) NOT NULL,
    fetched_count INT NOT NULL,
    new_count INT NOT NULL,
    duplicate_count INT NOT NULL,
    anomaly_count INT NOT NULL,
    error_message VARCHAR(1000),
    execution_duration_ms BIGINT NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE provider_health (
    id BIGSERIAL PRIMARY KEY,
    provider_name VARCHAR(50) NOT NULL CONSTRAINT uk_provider_name UNIQUE,
    status VARCHAR(20) NOT NULL,
    consecutive_failures INT NOT NULL DEFAULT 0,
    total_requests BIGINT NOT NULL DEFAULT 0,
    failed_requests BIGINT NOT NULL DEFAULT 0,
    last_success_at TIMESTAMP WITH TIME ZONE,
    last_error_at TIMESTAMP WITH TIME ZONE,
    last_error_message VARCHAR(500)
);
