package org.ingestor.dto.ingestionrun;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.Map;

public record CreateIngestionRunRequest(
        @NotNull
        Long ingestionProfileId,

        @NotNull
        Long marketDataSubscriptionId,

        String batchId,

        OffsetDateTime fromTime,

        OffsetDateTime toTime,

        Map<String, Object> metadata
) {
}
