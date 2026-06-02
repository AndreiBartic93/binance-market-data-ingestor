package org.ingestor.repository;

import org.ingestor.entity.IngestionWatermark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngestionWatermarkRepository extends JpaRepository<IngestionWatermark, Long> {
    Optional<IngestionWatermark> findByMarketDataSubscriptionId(Long marketDataSubscriptionId);

    boolean existsByMarketDataSubscriptionId(Long marketDataSubscriptionId);
}
