package org.ingestor.dto.asset;

import org.ingestor.entity.enums.AssetType;

import java.time.OffsetDateTime;

public record AssetResponse (
        Long id,
        String symbol,
        String name,
        AssetType assetType,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

