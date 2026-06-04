package org.ingestor.dto.ingestionrun;

import org.ingestor.entity.enums.IngestionMethod;
import org.ingestor.entity.enums.IngestionRunStatus;

import java.time.OffsetDateTime;
import java.util.Map;

public record IngestionRunResponse(
        Long id,

        Long ingestionProfileId,
        String ingestionProfileName,
        IngestionMethod ingestionMethod,

        Long marketDataSubscriptionId,
        String tradingPairSymbol,
        String timeframeCode,

        String batchId,
        IngestionRunStatus status,

        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,

        OffsetDateTime fromTime,
        OffsetDateTime toTime,

        Long recordsRequested,
        Long recordsReceived,
        Long recordsInserted,
        Long recordsSkipped,
        Long recordsFailed,

        Long messagesReceived,
        Long messagesProcessed,
        Long messagesFailed,

        String errorMessage,
        Map<String, Object> metadata,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
