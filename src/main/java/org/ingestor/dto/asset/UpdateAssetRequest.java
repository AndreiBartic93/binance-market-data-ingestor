package org.ingestor.dto.asset;

import jakarta.validation.constraints.Size;
import org.ingestor.entity.enums.AssetType;

public record UpdateAssetRequest (
        @Size(max = 100)
        String name,

        AssetType assetType,

        Boolean active
) {
}
