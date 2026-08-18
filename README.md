# CryptoPulse

CryptoPulse is an enterprise-grade Spring Boot backend service that ingests, normalizes, validates, deduplicates, and analyzes real-time cryptocurrency and DeFi market data from multiple public sources into PostgreSQL, featuring an automated price anomaly engine, Server-Sent Events (SSE) live streaming, and interactive OpenAPI documentation.

---

## Live API Endpoints & Interactive Documentation

- **Base Service Metadata**: `GET /`
- **OpenAPI 3.0 / Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Health Check & Actuator**: `GET /actuator/health`
- **Market Tickers API**: `GET /api/v1/tickers`
- **Single Ticker Detail**: `GET /api/v1/tickers/symbol/{symbol}`
- **Market Anomalies Feed**: `GET /api/v1/anomalies`
- **Real-Time SSE Anomaly Stream**: `GET /api/v1/anomalies/stream`
- **Market Intelligence Analytics**: `GET /api/v1/analytics/market-summary`
- **Ingestion Audit Trail**: `GET /api/v1/ingestion/status`
- **Provider Health Metrics**: `GET /api/v1/ingestion/providers`
- **Manual Ingestion Trigger**: `POST /api/v1/ingestion/run`

---

## 1. Overview & Key Innovations

CryptoPulse addresses the operational challenges of building resilient data pipelines for public financial and telemetry APIs:
- **Resilient Multi-Source Fetching**: Priority-ranked adapter registry (`CoinGeckoAdapter` primary, `CoinCapAdapter` secondary failover). If the primary provider experiences rate limits (429/5xx) or timeouts, the orchestrator dynamically fails over to the secondary source while updating operational health metrics.
- **Automated Price Anomaly Engine**: Evaluates incoming market data against surge and crash thresholds (e.g. >5% movement), classifying anomalies by severity (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).
- **Real-Time Event Streaming (SSE)**: Publishes `MarketAnomalyEvent` signals via Spring's `ApplicationEventPublisher` and streams live push notifications to connected web dashboard clients via Server-Sent Events (`/api/v1/anomalies/stream`).
- **Database-Backed Rate Limiting**: Cooldown tracking stored in PostgreSQL (`ingestion_logs` table) so rate limits persist across container restarts.
- **Single-Flight Concurrency Guard**: Thread-safe `AtomicBoolean` lock prevents duplicate parallel pipeline executions.
- **Composite Deduplication**: Database-level `UNIQUE(symbol, source)` constraints backed by application pre-checks guarantee clean, duplicate-free persistence.

---

## 2. Tech Stack

- **Java**: Java 17 LTS / Java 21 compatible.
- **Framework**: Spring Boot `3.4.3` (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-actuator`).
- **API Documentation**: SpringDoc OpenAPI 3.0 / Swagger UI (`springdoc-openapi-starter-webmvc-ui`).
- **Database**: PostgreSQL (Production) / In-Memory H2 (Isolated Unit & Integration Tests).
- **Schema Management**: Flyway Versioned Migrations (`V1__create_schema.sql`, `V2__create_indexes.sql`).
- **Build Tool**: Apache Maven Wrapper (`./mvnw`).
- **Containerization**: Multi-stage Dockerfile (`maven:3.9-eclipse-temurin-17` & `eclipse-temurin:17-jre-alpine`).
- **CI/CD & Scheduler**: GitHub Actions (`.github/workflows/ci.yml`).

---

## 3. Architecture & Data Flow

```text
               GitHub Actions / Scheduler / Client
                                │
                        ~hourly schedule
                                │
                                ▼
                   POST /api/v1/ingestion/run
                                │
                                ▼
                  IngestionOrchestrator
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
 DB Cooldown Check         Atomic Lock       Multi-Source Registry
  (ingestion_logs)       (Single-Flight)     ┌──────────┴──────────┐
        │                       │            ▼                     ▼
        └───────────────────────┼───► Primary Adapter     Secondary Adapter
                                │     (CoinGecko)            (CoinCap)
                                │            │                     │
                                │            └──────────┬──────────┘
                                │                       │
                                ▼                       ▼
                           Parse → Normalize → Validate
                                │
                                ▼
                           Deduplicate
                     (UNIQUE(symbol, source))
                                │
                                ▼
                       Anomaly Detector
                                │
             ┌──────────────────┴──────────────────┐
             ▼                                     ▼
     Database Storage                     Spring Event Bus
  (Neon / PostgreSQL)               (MarketAnomalyEvent)
             │                                     │
             ▼                                     ▼
      REST Query API                       Real-Time SSE Stream
  (Tickers/Analytics)                 (/api/v1/anomalies/stream)
```

---

## 4. API Documentation & Examples

### Service Metadata
```bash
curl -i http://localhost:8080/
```

### List & Filter Tickers
```bash
# List first 20 tickers ordered by market cap
curl -i "http://localhost:8080/api/v1/tickers"

# Search for Bitcoin
curl -i "http://localhost:8080/api/v1/tickers?keyword=bitcoin"

# Filter by source adapter
curl -i "http://localhost:8080/api/v1/tickers?source=coingecko"
```

### Market Analytics & Top Gainers
```bash
curl -i "http://localhost:8080/api/v1/analytics/market-summary"
```

### Real-Time Anomaly Stream (SSE)
```bash
curl -N "http://localhost:8080/api/v1/anomalies/stream"
```

### Trigger Ingestion
```bash
# Respects cooldown window
curl -X POST "http://localhost:8080/api/v1/ingestion/run"

# Force ingestion run
curl -X POST "http://localhost:8080/api/v1/ingestion/run?force=true"
```

---

## 5. Local Development & Verification

### Build & Run Tests
```bash
./mvnw test
```

### Run Locally
```bash
./mvnw spring-boot:run
```

### Build & Run Docker Container
```bash
docker build -t cryptopulse-backend .
docker run -p 8080:8080 cryptopulse-backend
```
