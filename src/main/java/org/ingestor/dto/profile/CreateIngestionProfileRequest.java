package org.ingestor.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.ingestor.entity.enums.IngestionMethod;

public record CreateIngestionProfileRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @NotNull
        IngestionMethod ingestionMethod,

        String description
) {
}
