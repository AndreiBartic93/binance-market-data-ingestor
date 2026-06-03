package org.ingestor.repository;

import org.ingestor.entity.IngestionProfileSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngestionProfileSubscriptionRepository extends JpaRepository<IngestionProfileSubscription, Long> {
    Optional<IngestionProfileSubscription> findByIngestionProfileIdAndMarketDataSubscriptionId(
            Long ingestionProfileId,
            Long marketDataSubscriptionId
    );
    boolean existsByIngestionProfileIdAndMarketDataSubscriptionId(
            Long ingestionProfileId,
            Long marketDataSubscriptionId
    );
    List<IngestionProfileSubscription> findByIngestionProfileId(Long ingestionProfileId);

    List<IngestionProfileSubscription> findByMarketDataSubscriptionId(Long marketDataSubscriptionId);

    List<IngestionProfileSubscription> findByActiveTrue();
}
