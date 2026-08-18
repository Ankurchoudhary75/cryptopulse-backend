package com.cryptopulse.api;

import com.cryptopulse.model.MarketTicker;
import com.cryptopulse.repository.MarketAnomalyRepository;
import com.cryptopulse.repository.MarketTickerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Tag(name = "Analytics & Market Intelligence", description = "Analytical aggregates, top gainers/losers, and anomaly rate metrics")
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final MarketTickerRepository tickerRepository;
    private final MarketAnomalyRepository anomalyRepository;

    public AnalyticsController(MarketTickerRepository tickerRepository,
                               MarketAnomalyRepository anomalyRepository) {
        this.tickerRepository = tickerRepository;
        this.anomalyRepository = anomalyRepository;
    }

    @Operation(summary = "Market Summary Analytics", description = "Provides macro market metrics, total cap, top gainers, top losers, and recent anomaly count.")
    @GetMapping("/market-summary")
    public ResponseEntity<Map<String, Object>> getMarketSummary() {
        BigDecimal totalCap = tickerRepository.getTotalMarketCap();
        long totalTickers = tickerRepository.count();
        List<MarketTicker> topGainers = tickerRepository.findTop10ByOrderByPercentChange24hDesc();
        List<MarketTicker> topLosers = tickerRepository.findTop10ByOrderByPercentChange24hAsc();

        Instant last24h = Instant.now().minus(24, ChronoUnit.HOURS);
        long anomalyCount24h = anomalyRepository.countByDetectedAtAfter(last24h);

        return ResponseEntity.ok(Map.of(
                "totalMarketCapUsd", totalCap != null ? totalCap : BigDecimal.ZERO,
                "totalTrackedAssets", totalTickers,
                "anomaliesLast24h", anomalyCount24h,
                "topGainers", topGainers,
                "topLosers", topLosers,
                "generatedAt", Instant.now()
        ));
    }
}
