package com.cryptopulse.api;

import com.cryptopulse.exception.ResourceNotFoundException;
import com.cryptopulse.model.MarketTicker;
import com.cryptopulse.repository.MarketTickerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Market Tickers", description = "Query, search, and retrieve ingested crypto market pricing tickers")
@RestController
@RequestMapping("/api/v1/tickers")
public class MarketTickerController {

    private final MarketTickerRepository tickerRepository;

    public MarketTickerController(MarketTickerRepository tickerRepository) {
        this.tickerRepository = tickerRepository;
    }

    @Operation(summary = "List & Filter Market Tickers", description = "Search tickers by keyword (matches symbol/name) or source provider with pagination.")
    @GetMapping
    public ResponseEntity<Page<MarketTicker>> getTickers(
            @Parameter(description = "Keyword to filter by symbol or name") @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by source adapter name (e.g., coingecko, coincap)") @RequestParam(required = false) String source,
            @Parameter(description = "Zero-indexed page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size
    ) {
        int pageSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "marketCapUsd"));
        Page<MarketTicker> result = tickerRepository.findWithFilters(keyword, source, pageRequest);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get Ticker by ID", description = "Retrieve full market ticker details by internal primary key ID.")
    @GetMapping("/{id}")
    public ResponseEntity<MarketTicker> getTickerById(@PathVariable Long id) {
        MarketTicker ticker = tickerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Market ticker not found with ID: " + id));
        return ResponseEntity.ok(ticker);
    }

    @Operation(summary = "Get Ticker by Symbol", description = "Retrieve market ticker details by ticker symbol (e.g., BTC, ETH).")
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<MarketTicker> getTickerBySymbol(@PathVariable String symbol) {
        MarketTicker ticker = tickerRepository.findFirstBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Market ticker not found with symbol: " + symbol));
        return ResponseEntity.ok(ticker);
    }
}
