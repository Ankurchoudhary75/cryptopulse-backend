package com.cryptopulse.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Tag(name = "Root & Discovery", description = "System metadata and route discovery endpoints")
@RestController
public class RootController {

    @Operation(summary = "Get Service Information", description = "Provides application metadata, versioning, operational status, and available API routes.")
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        return ResponseEntity.ok(Map.of(
                "service", "CryptoPulse",
                "description", "Multi-Source Real-Time Market Intelligence & Anomaly Detection Engine",
                "version", "1.0.0",
                "timestamp", Instant.now(),
                "documentation", "/swagger-ui.html",
                "endpoints", Map.of(
                        "GET /", "Service information",
                        "GET /actuator/health", "Health check & DB status",
                        "GET /api/v1/tickers", "List & filter market tickers (keyword, source, page, size)",
                        "GET /api/v1/tickers/symbol/{symbol}", "Get asset detail by symbol",
                        "GET /api/v1/anomalies", "Retrieve flagged price surge/crash anomalies",
                        "GET /api/v1/anomalies/stream", "Real-Time Server-Sent Events (SSE) anomaly stream",
                        "GET /api/v1/analytics/market-summary", "Market intelligence analytics & top gainers",
                        "GET /api/v1/ingestion/status", "Audit trail of pipeline runs",
                        "GET /api/v1/ingestion/providers", "Multi-source API health & circuit-breaker metrics",
                        "POST /api/v1/ingestion/run", "Trigger manual market ingestion"
                )
        ));
    }
}
