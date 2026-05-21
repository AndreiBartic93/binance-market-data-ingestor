package org.ingestor.dto.exchange;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateExchangeRequest (
    @NotBlank
    @Size(max=50)
    String code,

    @NotBlank
    @Size(max=100)
    String name,

    @Size(max = 255)
    String restBaseUrl,

    @Size(max = 255)
    String websocketBaseUrl
) {
}
