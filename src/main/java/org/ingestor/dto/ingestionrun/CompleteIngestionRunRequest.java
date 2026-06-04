package org.ingestor.dto.ingestionrun;

import org.ingestor.entity.enums.IngestionRunStatus;

import java.util.Map;

public record CompleteIngestionRunRequest(
        IngestionRunStatus status,

        Long recordsRequested,
        Long recordsReceived,
        Long recordsInserted,
        Long recordsSkipped,
        Long recordsFailed,

        Long messagesReceived,
        Long messagesProcessed,
        Long messagesFailed,

        Map<String, Object> metadata
) {
}
