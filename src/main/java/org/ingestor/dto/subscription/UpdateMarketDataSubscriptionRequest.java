package org.ingestor.dto.subscription;

import jakarta.validation.constraints.Size;

public record UpdateMarketDataSubscriptionRequest(

        Boolean collectHistorical,

        Boolean collectScheduled,

        Boolean collectLive,

        @Size(max = 150)
        String websocketStreamName,

        Boolean active
) {
}
