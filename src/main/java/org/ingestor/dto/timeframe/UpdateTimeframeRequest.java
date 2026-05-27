package org.ingestor.dto.timeframe;

import jakarta.validation.constraints.Positive;

public record UpdateTimeframeRequest(

        @Positive
        Integer durationMinutes,

        Boolean active
) {
}
