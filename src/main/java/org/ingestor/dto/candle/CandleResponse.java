package org.ingestor.dto.candle;

import org.ingestor.entity.enums.CandleSource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CandleResponse(
        Long id,

        Long marketDataSubscriptionId,

        Long tradingPairId,
        String tradingPairSymbol,

        Long timeframeId,
        String timeframeCode,

        Long ingestionRunId,

        OffsetDateTime openTime,
        OffsetDateTime closeTime,

        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal closePrice,

        BigDecimal volume,
        BigDecimal quoteVolume,

        Long numberOfTrades,

        BigDecimal takerBuyBaseVolume,
        BigDecimal takerBuyQuoteVolume,

        CandleSource source,
        boolean closed,

        OffsetDateTime createdAt
) {
}
