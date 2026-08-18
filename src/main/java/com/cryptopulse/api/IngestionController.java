package com.cryptopulse.api;

import com.cryptopulse.model.IngestionLog;
import com.cryptopulse.model.ProviderHealth;
import com.cryptopulse.pipeline.IngestionOrchestrator;
import com.cryptopulse.repository.IngestionLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Ingestion Pipeline", description = "Trigger ingestion, inspect audit logs, and monitor provider health metrics")
@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {

    private final IngestionOrchestrator orchestrator;
    private final IngestionLogRepository logRepository;

    public IngestionController(IngestionOrchestrator orchestrator,
                                IngestionLogRepository logRepository) {
        this.orchestrator = orchestrator;
        this.logRepository = logRepository;
    }

    @Operation(summary = "Trigger Ingestion Run", description = "Executes the multi-source ingestion pipeline. Honors database rate-limit cooldown unless 'force=true'.")
    @PostMapping("/run")
    public ResponseEntity<IngestionLog> triggerIngestion(
            @RequestParam(defaultValue = "false") boolean force
    ) {
        IngestionLog log = orchestrator.triggerIngestion(force);
        return ResponseEntity.ok(log);
    }

    @Operation(summary = "Ingestion Audit Status", description = "Retrieve paginated history of ingestion pipeline runs.")
    @GetMapping("/status")
    public ResponseEntity<Page<IngestionLog>> getIngestionStatus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int pageSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "completedAt"));
        return ResponseEntity.ok(logRepository.findAll(pageRequest));
    }

    @Operation(summary = "Provider Health Metrics", description = "Retrieve operational health metrics, failure rates, and circuit-breaker status per API source provider.")
    @GetMapping("/providers")
    public ResponseEntity<List<ProviderHealth>> getProviderHealth() {
        return ResponseEntity.ok(orchestrator.getProviderHealthList());
    }
}
