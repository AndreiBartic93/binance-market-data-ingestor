package org.ingestor.dto.scheduledsync;

import org.ingestor.entity.enums.IngestionRunStatus;

import java.time.OffsetDateTime;

public record ScheduledSubscriptionSyncResult(
        Long profileSubscriptionLinkId,

        Long ingestionRunId,
        String batchId,
        IngestionRunStatus status,

        Long marketDataSubscriptionId,
        String tradingPairSymbol,
        String timeframeCode,

        OffsetDateTime fromTime,
        OffsetDateTime toTime,

        Long recordsRequested,
        Long recordsReceived,
        Long recordsInserted,
        Long recordsSkipped,
        Long recordsFailed,

        OffsetDateTime lastSuccessfulOpenTime,
        OffsetDateTime lastSuccessfulCloseTime,

        boolean watermarkUpdated,

        String message
) {
}