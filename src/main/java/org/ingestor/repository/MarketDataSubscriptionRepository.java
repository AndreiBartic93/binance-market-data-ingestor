package org.ingestor.repository;

import org.ingestor.entity.MarketDataSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarketDataSubscriptionRepository extends JpaRepository<MarketDataSubscription, Long> {

    Optional<MarketDataSubscription> findByTradingPairIdAndTimeframeId(Long tradingPairId, Long timeframeId);

    boolean existsByTradingPairIdAndTimeframeId(Long tradingPairId, Long timeframeId);

    List<MarketDataSubscription> findByTradingPairId(Long tradingPairId);

    List<MarketDataSubscription> findByTimeframeId(Long timeframeId);

    List<MarketDataSubscription> findByActiveTrue();

    List<MarketDataSubscription> findByCollectHistoricalTrueAndActiveTrue();

    List<MarketDataSubscription> findByCollectScheduledTrueAndActiveTrue();

    List<MarketDataSubscription> findByCollectLiveTrueAndActiveTrue();
}
