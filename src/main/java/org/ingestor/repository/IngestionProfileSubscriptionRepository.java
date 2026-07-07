package org.ingestor.repository;

import org.ingestor.entity.IngestionProfileSubscription;
import org.ingestor.entity.enums.IngestionMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
        SELECT link
        FROM IngestionProfileSubscription link
        JOIN FETCH link.ingestionProfile profile
        JOIN FETCH link.marketDataSubscription subscription
        JOIN FETCH subscription.tradingPair
        JOIN FETCH subscription.timeframe
        WHERE subscription.id = :marketDataSubscriptionId
          AND link.active = true
          AND profile.active = true
          AND profile.ingestionMethod = :ingestionMethod
        """)
    Optional<IngestionProfileSubscription> findActiveBySubscriptionIdAndIngestionMethod(
            @Param("marketDataSubscriptionId") Long marketDataSubscriptionId,
            @Param("ingestionMethod") IngestionMethod ingestionMethod
    );
}
