package org.ingestor.dto.timeframe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTimeframeRequest(
        @NotBlank
        @Size(max = 10)
        String code,

        @NotNull
        @Positive
        Integer durationMinutes
) {
}
