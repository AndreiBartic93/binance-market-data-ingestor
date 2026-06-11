package org.ingestor.repository;

import org.ingestor.entity.Candle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CandleRepository extends JpaRepository<Candle, Long> {

    List<Candle> findByMarketDataSubscriptionIdOrderByOpenTimeAsc(Long marketDataSubscriptionId);

    List<Candle> findByTradingPairIdAndTimeframeIdOrderByOpenTimeAsc(
            Long tradingPairId,
            Long timeframeId
    );

    List<Candle> findByTradingPairIdAndTimeframeIdAndOpenTimeBetweenOrderByOpenTimeAsc(
            Long tradingPairId,
            Long timeframeId,
            OffsetDateTime fromTime,
            OffsetDateTime toTime
    );

    List<Candle> findByIngestionRunIdOrderByOpenTimeAsc(Long ingestionRunId);

    Optional<Candle> findByTradingPairIdAndTimeframeIdAndOpenTime(
            Long tradingPairId,
            Long timeframeId,
            OffsetDateTime openTime
    );

    boolean existsByTradingPairIdAndTimeframeIdAndOpenTime(
            Long tradingPairId,
            Long timeframeId,
            OffsetDateTime openTime
    );
}
