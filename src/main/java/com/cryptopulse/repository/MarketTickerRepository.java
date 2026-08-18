package com.cryptopulse.repository;

import com.cryptopulse.model.MarketTicker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketTickerRepository extends JpaRepository<MarketTicker, Long> {

    Optional<MarketTicker> findBySymbolAndSource(String symbol, String source);

    Optional<MarketTicker> findFirstBySymbol(String symbol);

    boolean existsBySymbolAndSource(String symbol, String source);

    @Query("SELECT t FROM MarketTicker t WHERE " +
           "(:keyword IS NULL OR LOWER(t.symbol) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) AND " +
           "(:source IS NULL OR LOWER(t.source) = LOWER(CAST(:source AS string)))")
    Page<MarketTicker> findWithFilters(@Param("keyword") String keyword,
                                       @Param("source") String source,
                                       Pageable pageable);

    List<MarketTicker> findTop10ByOrderByPercentChange24hDesc();

    List<MarketTicker> findTop10ByOrderByPercentChange24hAsc();

    @Query("SELECT SUM(t.marketCapUsd) FROM MarketTicker t")
    BigDecimal getTotalMarketCap();
}
