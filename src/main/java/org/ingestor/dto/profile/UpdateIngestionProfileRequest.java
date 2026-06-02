package org.ingestor.dto.profile;

import jakarta.validation.constraints.Size;

public record UpdateIngestionProfileRequest(
        @Size(max = 150)
        String name,

        String description,

        Boolean active
) {
}
