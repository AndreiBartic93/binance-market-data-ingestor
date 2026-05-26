package org.ingestor.dto.tradingpair;

import org.ingestor.entity.enums.TradingPairStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TradingPairResponse(
        Long id,

        Long exchangeId,
        String exchangeCode,

        Long baseAssetId,
        String baseAssetSymbol,

        Long quoteAssetId,
        String quoteAssetSymbol,

        String symbol,
        TradingPairStatus status,

        Integer pricePrecision,
        Integer quantityPrecision,

        BigDecimal minQuantity,
        BigDecimal maxQuantity,

        boolean active,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
