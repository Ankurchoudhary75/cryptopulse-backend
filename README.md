# ⚡ CryptoPulse — Multi-Source Market Intelligence Engine

> **Acdyon Technologies Engineering Challenge Submission — Part 1: Data Ingestion & Extraction System**  
> *Production-Ready Spring Boot 3.4 / Java 17+ Backend Engine with Automated Anomaly Detection, Real-Time SSE Streaming, and Multi-Provider Failover.*

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java Version](https://img.shields.io/badge/java-17%20LTS-orange.svg)]()
[![Framework](https://img.shields.io/badge/framework-Spring%20Boot%203.4.3-blue.svg)]()
[![Database](https://img.shields.io/badge/database-PostgreSQL%20%7C%20H2-blueviolet.svg)]()
[![API Documentation](https://img.shields.io/badge/OpenAPI-Swagger%20UI-green.svg)]()

---

## 🌟 Executive Summary

CryptoPulse is a high-throughput, fault-tolerant backend intelligence pipeline designed to pull, normalize, validate, deduplicate, and analyze real-time market pricing and volume metrics from public endpoints into PostgreSQL.

### Key Architectural Superiorities
- 🔄 **Multi-Source Resilient Adapter Pattern**: Priority-ranked API adapter registry (`CoinGeckoAdapter` primary, `CoinCapAdapter` secondary failover). If the primary provider experiences rate limits (429/5xx) or timeouts, the system dynamically fails over to the secondary source while updating operational health metrics in `ProviderHealth`.
- 🚨 **Automated Price Anomaly Engine**: Evaluates incoming market data against surge and crash thresholds (e.g. >5% movement), classifying anomalies by severity (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).
- 📡 **Real-Time Event Streaming (SSE)**: Publishes `MarketAnomalyEvent` signals via Spring's `ApplicationEventPublisher` and streams live push notifications to connected web dashboard clients via Server-Sent Events (`/api/v1/anomalies/stream`).
- 🛡️ **Database-Backed Rate Limiting**: Cooldown tracking stored directly in PostgreSQL (`ingestion_logs` table) so rate limits survive container restarts and redeployments.
- 🔒 **Single-Flight Concurrency Guard**: Thread-safe `AtomicBoolean` lock prevents duplicate parallel pipeline executions.
- 🎯 **Hard Deduplication**: Composite database-level `UNIQUE(symbol, source)` constraints backed by application pre-checks guarantee clean, duplicate-free persistence.

---

## 📐 Architecture & Data Flow

```text
               GitHub Actions / Cron / External Client
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
                       Deduplicate (UNIQUE)
                                │
                                ▼
                         Anomaly Detector
                                │
             ┌──────────────────┴──────────────────┐
             ▼                                     ▼
      PostgreSQL Storage                  Spring Event Bus
   (Flyway Migrations)              (MarketAnomalyEvent)
             │                                     │
             ▼                                     ▼
      REST Query API                       Real-Time SSE Stream
  (Tickers & Analytics)               (/api/v1/anomalies/stream)
```

---

## 🛠️ Tech Stack & Tooling

| Layer | Technology Choice |
| :--- | :--- |
| **Language Baseline** | Java 17 LTS / Java 21 LTS Compatible |
| **Framework** | Spring Boot `3.4.3` (`starter-web`, `starter-data-jpa`, `starter-actuator`) |
| **API Documentation** | SpringDoc OpenAPI 3.0 / Swagger UI (`springdoc-openapi-starter-webmvc-ui`) |
| **Database & Migrations** | PostgreSQL (Production) / Flyway Migrations (`V1`, `V2`) / H2 (In-Memory Tests) |
| **Build System** | Apache Maven Wrapper (`./mvnw`) |
| **Containerization** | Multi-Stage Dockerfile (`maven:3.9-eclipse-temurin-17` & `eclipse-temurin:17-jre-alpine`) |

---

## 🌐 API Endpoints & Interactive Swagger UI

### Interactive OpenAPI Documentation
- **Swagger UI Dashboard**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI 3.0 JSON Spec**: `http://localhost:8080/v3/api-docs`

### REST API Route Table

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Service metadata & route discovery |
| `GET` | `/actuator/health` | Application & database connection health check |
| `GET` | `/api/v1/tickers` | List & filter market tickers (supports `keyword`, `source`, `page`, `size`) |
| `GET` | `/api/v1/tickers/{id}` | Get single market ticker by internal primary key |
| `GET` | `/api/v1/tickers/symbol/{symbol}` | Get single market ticker detail by asset symbol |
| `GET` | `/api/v1/anomalies` | Retrieve historical price surge & crash anomalies |
| `GET` | `/api/v1/anomalies/latest` | Retrieve 20 most recent market anomalies |
| `GET` | `/api/v1/anomalies/stream` | **Real-time Server-Sent Events (SSE)** anomaly stream |
| `GET` | `/api/v1/analytics/market-summary` | Macro analytics (total market cap, top gainers/losers, 24h anomaly count) |
| `GET` | `/api/v1/ingestion/status` | Ingestion run audit trail |
| `GET` | `/api/v1/ingestion/providers` | Multi-source health & circuit-breaker metrics |
| `POST` | `/api/v1/ingestion/run` | Trigger manual ingestion run (`force=true` overrides cooldown) |

---

## 🧪 Verification & Test Automation

Run the complete unit & integration test suite (`./mvnw test`):

```bash
./mvnw test
```

### Test Coverage Summary:
- **`MarketDataNormalizerTest`**: Validates decimal scaling, string truncation, and timestamp parsing.
- **`MarketDataValidatorTest`**: Enforces non-null symbols, non-zero positive prices, and source bounds.
- **`AnomalyDetectorTest`**: Tests surge (>5%) and crash (<-5%) anomaly detection rules and severity levels (`CRITICAL`, `HIGH`, `MEDIUM`).
- **`CoinGeckoAdapterTest` / `CoinCapAdapterTest`**: Verifies REST template mapping and exception propagation.
- **`IngestionOrchestratorTest`**: Tests dynamic primary-to-secondary failover, single-flight locking, and cooldown status logs.
- **`MarketTickerControllerTest` / `MarketAnomalyControllerTest`**: Tests Spring WebMvc routes, HTTP status codes, and SSE stream endpoints.
- **`MarketTickerRepositoryTest`**: Tests JPA persistence, custom JPQL filters, and aggregate total market cap queries.

Total: **21 Passing Tests (0 Failures, 0 Errors)**.

---

## 🚀 Running Locally & Containerization

### Run Spring Boot Application
```bash
./mvnw spring-boot:run
```

### Run via Docker
```bash
docker build -t cryptopulse-backend .
docker run -p 8080:8080 cryptopulse-backend
```

---

## 📄 Written Explanation (`DECISIONS.md`)

For detailed answers to the challenge design questions (Detection Surface, Ingestion Strategy, Resilience, Ethical Boundaries, Trade-Offs under Time Limits, and AI Usage & Line-by-Line Engineering Verification), see **[`DECISIONS.md`](file:///Users/ankurchoudhary/Downloads/jobharvest-backend-main/DECISIONS.md)**.
