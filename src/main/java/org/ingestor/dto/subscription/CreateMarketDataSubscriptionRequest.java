package org.ingestor.dto.subscription;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMarketDataSubscriptionRequest (
        @NotNull
        Long tradingPairId,

        @NotNull
        Long timeframeId,

        Boolean collectHistorical,

        Boolean collectScheduled,

        Boolean collectLive,

        @Size(max = 150)
        String websocketStreamName
) {

}
