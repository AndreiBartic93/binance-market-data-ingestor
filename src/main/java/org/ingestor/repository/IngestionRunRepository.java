package org.ingestor.repository;

import org.ingestor.entity.IngestionRun;
import org.ingestor.entity.enums.IngestionRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IngestionRunRepository extends JpaRepository<IngestionRun, Long> {

    @Query("""
            SELECT run
            FROM IngestionRun run
            WHERE (:status IS NULL OR run.status = :status)
              AND (:ingestionProfileId IS NULL OR run.ingestionProfile.id = :ingestionProfileId)
              AND (:marketDataSubscriptionId IS NULL OR run.marketDataSubscription.id = :marketDataSubscriptionId)
              AND (:batchId IS NULL OR run.batchId = :batchId)
            ORDER BY run.startedAt DESC
            """)
    List<IngestionRun> search(
            @Param("status") IngestionRunStatus status,
            @Param("ingestionProfileId") Long ingestionProfileId,
            @Param("marketDataSubscriptionId") Long marketDataSubscriptionId,
            @Param("batchId") String batchId
    );

    List<IngestionRun> findByBatchIdOrderByStartedAtDesc(String batchId);
}
