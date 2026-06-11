package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.candle.CandleResponse;
import org.ingestor.entity.Candle;
import org.ingestor.exception.ResourceNotFoundException;
import org.ingestor.repository.CandleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandleService {

    private final CandleRepository candleRepository;

    @Transactional(readOnly = true)
    public List<CandleResponse> findAll(
            Long marketDataSubscriptionId,
            Long tradingPairId,
            Long timeframeId,
            Long ingestionRunId,
            OffsetDateTime fromTime,
            OffsetDateTime toTime
    ) {
        List<Candle> candles;

        if (marketDataSubscriptionId != null) {
            candles = candleRepository.findByMarketDataSubscriptionIdOrderByOpenTimeAsc(
                    marketDataSubscriptionId
            );
        } else if (ingestionRunId != null) {
            candles = candleRepository.findByIngestionRunIdOrderByOpenTimeAsc(
                    ingestionRunId
            );
        } else if (tradingPairId != null && timeframeId != null && fromTime != null && toTime != null) {
            candles = candleRepository.findByTradingPairIdAndTimeframeIdAndOpenTimeBetweenOrderByOpenTimeAsc(
                    tradingPairId,
                    timeframeId,
                    fromTime,
                    toTime
            );
        } else if (tradingPairId != null && timeframeId != null) {
            candles = candleRepository.findByTradingPairIdAndTimeframeIdOrderByOpenTimeAsc(
                    tradingPairId,
                    timeframeId
            );
        } else {
            candles = candleRepository.findAll();
        }

        return candles.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CandleResponse findById(Long id) {
        Candle candle = candleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candle not found with id: " + id
                ));

        return toResponse(candle);
    }

    private CandleResponse toResponse(Candle candle) {
        return new CandleResponse(
                candle.getId(),

                candle.getMarketDataSubscription().getId(),

                candle.getTradingPair().getId(),
                candle.getTradingPair().getSymbol(),

                candle.getTimeframe().getId(),
                candle.getTimeframe().getCode(),

                candle.getIngestionRun() == null ? null : candle.getIngestionRun().getId(),

                candle.getOpenTime(),
                candle.getCloseTime(),

                candle.getOpenPrice(),
                candle.getHighPrice(),
                candle.getLowPrice(),
                candle.getClosePrice(),

                candle.getVolume(),
                candle.getQuoteVolume(),

                candle.getNumberOfTrades(),

                candle.getTakerBuyBaseVolume(),
                candle.getTakerBuyQuoteVolume(),

                candle.getSource(),
                candle.isClosed(),

                candle.getCreatedAt()
        );
    }
}
