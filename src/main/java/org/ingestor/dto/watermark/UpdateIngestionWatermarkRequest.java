package org.ingestor.dto.watermark;

import java.time.OffsetDateTime;

public record UpdateIngestionWatermarkRequest(
        OffsetDateTime lastSuccessfulOpenTime,
        OffsetDateTime lastSuccessfulCloseTime,
        Long lastIngestionRunId
) {
}
