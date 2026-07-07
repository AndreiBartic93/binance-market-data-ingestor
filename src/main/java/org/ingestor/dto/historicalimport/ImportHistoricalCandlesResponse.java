package org.ingestor.dto.historicalimport;

import org.ingestor.entity.enums.IngestionRunStatus;

import java.time.OffsetDateTime;

public record ImportHistoricalCandlesResponse(
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

        OffsetDateTime firstImportedOpenTime,
        OffsetDateTime lastImportedOpenTime,

        boolean watermarkUpdated,

        String message,

        OffsetDateTime startedAt,
        OffsetDateTime finishedAt
) {
}
