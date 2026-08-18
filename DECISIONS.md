# DECISIONS: CryptoPulse Architecture & Engineering Choices

## 1. Multi-Source Adapter Strategy with Dynamic Failover

Standard data pipelines typically depend on a single public source, which creates a single point of failure when upstream endpoints experience rate limiting (429 HTTP status), network timeouts, or service outages.

In **CryptoPulse**, we implemented the **Multi-Source Adapter Pattern** using `MarketSourceAdapter`:
- **Primary Provider (`CoinGeckoAdapter`)**: High-fidelity market cap and volume data. Priority rank = 1.
- **Secondary Provider (`CoinCapAdapter`)**: Failover source automatically invoked by `IngestionOrchestrator` if CoinGecko is throttled or unreachable. Priority rank = 2.

### Failover & Provider Health Tracking:
Each provider's operational health is monitored via `ProviderHealth`. Successes reset consecutive failure counters; consecutive failures degrade provider health status to `DEGRADED` or `UNHEALTHY`. When the primary provider fails, `IngestionOrchestrator` seamlessly shifts execution to the secondary provider while recording a `DEGRADED_FALLBACK` audit log in PostgreSQL.

---

## 2. Statistical Anomaly Engine & Real-Time Event Architecture

Rather than acting merely as a CRUD data mirror, CryptoPulse features an **Automated Anomaly Detection Engine**:
- **Rule Evaluation**: Fresh incoming market tickers are compared against preset surge/crash thresholds (`surgeThresholdPercent`, `crashThresholdPercent`).
- **Severity Mapping**: Anomalies are dynamically classified into `LOW`, `MEDIUM`, `HIGH`, or `CRITICAL` severity based on percentage deviation magnitude.
- **Event Decoupling**: Detected anomalies trigger a Spring `MarketAnomalyEvent`.
- **Server-Sent Events (SSE)**: `AnomalyEventListener` broadcasts events live to all connected HTTP clients via `GET /api/v1/anomalies/stream`. Clients receive real-time push alerts without needing to poll the backend.

---

## 3. Database Cooldown, Concurrency Control & Single-Flight Locks

- **Single-Flight Guard**: An `AtomicBoolean` lock ensures that only one ingestion run can execute concurrently on single-instance runtimes, rejecting duplicate triggers.
- **Database Cooldown Persistence**: Minimum cooldown windows (15 minutes) are enforced by checking `ingestion_logs` in PostgreSQL. This guarantees rate-limit compliance across container restarts, container sleeps, or platform redeployments.

---

## 4. Database Migrations & Multi-Tier Persistence

- **Flyway Migrations**: Schema evolution is strictly managed via SQL migration scripts (`V1__create_schema.sql`, `V2__create_indexes.sql`).
- **Composite Unique Constraints**: Database-level `UNIQUE(symbol, source)` constraints back application-level pre-checks, ensuring complete idempotency and zero duplicate listings.
- **High-Performance Composite Indexes**: Dedicated DB indexes on `(symbol)`, `(source)`, `(last_updated)`, `(market_cap_usd DESC)`, and `(detected_at DESC)` accelerate pagination, search filters, and aggregate market analytics.

---

## 5. Verification & Test Automation Evidence

The entire system was verified using automated unit and integration tests (`./mvnw test`):
- **21 Passing Tests**:
  - Normalization scaling, text trimming, and ISO timestamp parsing (`MarketDataNormalizerTest`).
  - Required field bounds and non-zero price validation (`MarketDataValidatorTest`).
  - Surge/crash anomaly rules and severity classification (`AnomalyDetectorTest`).
  - Primary source mapping and error handling (`CoinGeckoAdapterTest`).
  - Failover source parsing (`CoinCapAdapterTest`).
  - Dynamic multi-source failover and fallback status logging (`IngestionOrchestratorTest`).
  - Controller endpoints and SSE stream subscriptions (`MarketTickerControllerTest`, `MarketAnomalyControllerTest`).
  - JPA persistence, filtering, and aggregate queries (`MarketTickerRepositoryTest`).
