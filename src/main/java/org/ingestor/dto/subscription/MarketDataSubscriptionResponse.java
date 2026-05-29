package org.ingestor.dto.subscription;

import java.time.OffsetDateTime;

public record MarketDataSubscriptionResponse(
        Long id,

        Long tradingPairId,
        String tradingPairSymbol,

        Long timeframeId,
        String timeframeCode,

        boolean collectHistorical,
        boolean collectScheduled,
        boolean collectLive,

        String websocketStreamName,

        boolean active,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
