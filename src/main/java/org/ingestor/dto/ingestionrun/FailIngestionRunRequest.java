package org.ingestor.dto.ingestionrun;

import java.util.Map;

public record FailIngestionRunRequest(
        String errorMessage,

        Long recordsFailed,

        Long messagesFailed,

        Map<String, Object> metadata
) {
}
