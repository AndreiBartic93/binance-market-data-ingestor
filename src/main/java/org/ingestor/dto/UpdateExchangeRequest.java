package org.ingestor.dto;

import jakarta.validation.constraints.Size;

public record UpdateExchangeRequest (

    @Size(max = 100)
    String name,

    @Size(max = 255)
    String restBaseUrl,

    @Size(max = 255)
    String webSocketBaseUrl,

    Boolean active
) {
}