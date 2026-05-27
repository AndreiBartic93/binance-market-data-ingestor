package org.ingestor.dto.timeframe;

import java.time.OffsetDateTime;

public record TimeframeResponse(
        Long id,
        String code,
        Integer durationMinutes,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
