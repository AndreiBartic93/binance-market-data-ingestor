package org.ingestor.dto.tradingpair;

import jakarta.validation.constraints.PositiveOrZero;
import org.ingestor.entity.enums.TradingPairStatus;

import java.math.BigDecimal;

public record UpdateTradingPairRequest(

        TradingPairStatus status,

        @PositiveOrZero
        Integer pricePrecision,

        @PositiveOrZero
        Integer quantityPrecision,

        BigDecimal minQuantity,

        BigDecimal maxQuantity,

        Boolean active
) {
}
