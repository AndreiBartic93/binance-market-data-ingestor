package org.ingestor.dto.profilesubscription;

import org.ingestor.entity.enums.IngestionMethod;

import java.time.OffsetDateTime;

public record IngestionProfileSubscriptionResponse(
        Long id,

        Long ingestionProfileId,
        String ingestionProfileName,
        IngestionMethod ingestionMethod,

        Long marketDataSubscriptionId,
        String tradingPairSymbol,
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
