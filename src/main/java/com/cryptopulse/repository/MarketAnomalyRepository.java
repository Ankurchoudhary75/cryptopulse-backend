package com.cryptopulse.repository;

import com.cryptopulse.model.MarketAnomaly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MarketAnomalyRepository extends JpaRepository<MarketAnomaly, Long> {

    Page<MarketAnomaly> findBySymbol(String symbol, Pageable pageable);

    Page<MarketAnomaly> findBySeverity(MarketAnomaly.Severity severity, Pageable pageable);

    List<MarketAnomaly> findTop20ByOrderByDetectedAtDesc();

    long countByDetectedAtAfter(Instant timestamp);
}
