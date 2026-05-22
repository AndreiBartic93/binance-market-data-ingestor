package org.ingestor.dto.asset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.ingestor.entity.AssetType;

public record CreateAssetRequest (

    @NotBlank
    @Size(max = 20)
    String symbol,

    @Size(max = 100)
    String name,

    @NotNull
    AssetType assetType
) {
}