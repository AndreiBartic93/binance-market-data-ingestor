package org.ingestor.dto.profile;

import org.ingestor.entity.enums.IngestionMethod;

import java.time.OffsetDateTime;

public record IngestionProfileResponse(
        Long id,
        String name,
        IngestionMethod ingestionMethod,
        String description,
        boolean storesMarketData,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
