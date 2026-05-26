package org.ingestor.dto.tradingpair;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTradingPairRequest (
        @NotNull
        Long exchangeId,

        @NotNull
        Long baseAssetId,

        @NotNull
        Long quoteAssetId,

        @NotBlank
        @Size(max = 30)
        String symbol,

        @PositiveOrZero
        Integer pricePrecision,

        @PositiveOrZero
        Integer quantityPrecision,

        BigDecimal minQuantity,

        BigDecimal maxQuantity
) {

}

