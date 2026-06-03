package org.ingestor.dto.profilesubscription;

import jakarta.validation.constraints.NotNull;

public record CreateIngestionProfileSubscriptionRequest (
        @NotNull
        Long ingestionProfileId,

        @NotNull
        Long marketDataSubscriptionId
) {

}
