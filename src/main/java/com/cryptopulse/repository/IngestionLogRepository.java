package com.cryptopulse.repository;

import com.cryptopulse.model.IngestionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngestionLogRepository extends JpaRepository<IngestionLog, Long> {

    Optional<IngestionLog> findFirstByOrderByCompletedAtDesc();

    @Query("SELECT l FROM IngestionLog l WHERE l.status = 'SUCCESS' OR l.status = 'DEGRADED_FALLBACK' ORDER BY l.completedAt DESC")
    Optional<IngestionLog> findLatestSuccessfulRun();
}
