package com.cryptopulse.api;

import com.cryptopulse.event.AnomalyEventListener;
import com.cryptopulse.model.MarketAnomaly;
import com.cryptopulse.repository.MarketAnomalyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "Market Anomalies", description = "Query historical price surge/crash anomalies and connect to real-time SSE stream")
@RestController
@RequestMapping("/api/v1/anomalies")
public class MarketAnomalyController {

    private final MarketAnomalyRepository anomalyRepository;
    private final AnomalyEventListener sseEventListener;

    public MarketAnomalyController(MarketAnomalyRepository anomalyRepository,
                                  AnomalyEventListener sseEventListener) {
        this.anomalyRepository = anomalyRepository;
        this.sseEventListener = sseEventListener;
    }

    @Operation(summary = "List Flagged Anomalies", description = "Retrieve paginated list of historical price surge and crash anomalies ordered by detection date.")
    @GetMapping
    public ResponseEntity<Page<MarketAnomaly>> getAnomalies(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) MarketAnomaly.Severity severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int pageSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "detectedAt"));
        
        Page<MarketAnomaly> result;
        if (symbol != null && !symbol.isBlank()) {
            result = anomalyRepository.findBySymbol(symbol.toUpperCase(), pageRequest);
        } else if (severity != null) {
            result = anomalyRepository.findBySeverity(severity, pageRequest);
        } else {
            result = anomalyRepository.findAll(pageRequest);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Latest Anomalies", description = "Retrieve the 20 most recent market anomalies.")
    @GetMapping("/latest")
    public ResponseEntity<List<MarketAnomaly>> getLatestAnomalies() {
        return ResponseEntity.ok(anomalyRepository.findTop20ByOrderByDetectedAtDesc());
    }

    @Operation(summary = "Real-Time SSE Anomaly Stream", description = "Server-Sent Events (SSE) endpoint emitting live market anomalies as they are flagged during pipeline runs.")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAnomalies() {
        return sseEventListener.registerEmitter();
    }
}
