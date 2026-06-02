package org.ingestor.dto.watermark;

import java.time.OffsetDateTime;

public record IngestionWatermarkResponse(
        Long id,

        Long marketDataSubscriptionId,
        String tradingPairSymbol,
        String timeframeCode,

        OffsetDateTime lastSuccessfulOpenTime,
        OffsetDateTime lastSuccessfulCloseTime,

        Long lastIngestionRunId,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
