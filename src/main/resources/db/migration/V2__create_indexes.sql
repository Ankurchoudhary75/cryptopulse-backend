-- V2: Performance Indexes for Search, Filtering, and Analytics

CREATE INDEX idx_ticker_symbol ON market_tickers (symbol);
CREATE INDEX idx_ticker_source ON market_tickers (source);
CREATE INDEX idx_ticker_updated ON market_tickers (last_updated);
CREATE INDEX idx_ticker_market_cap ON market_tickers (market_cap_usd DESC);

CREATE INDEX idx_anomaly_symbol ON market_anomalies (symbol);
CREATE INDEX idx_anomaly_detected ON market_anomalies (detected_at DESC);
CREATE INDEX idx_anomaly_severity ON market_anomalies (severity);

CREATE INDEX idx_ingestion_started ON ingestion_logs (started_at DESC);
CREATE INDEX idx_ingestion_status ON ingestion_logs (status);
