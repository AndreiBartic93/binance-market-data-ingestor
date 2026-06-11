package org.ingestor.dto.binance;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BinanceKlineResponse(
        OffsetDateTime openTime,

        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal closePrice,

        BigDecimal volume,

        OffsetDateTime closeTime,

        BigDecimal quoteVolume,

        Long numberOfTrades,

        BigDecimal takerBuyBaseVolume,
        BigDecimal takerBuyQuoteVolume
) {
}
