package org.ingestor.dto.exchange;

import java.time.OffsetDateTime;

public record ExchangeResponse (
        Long id,
        String code,
        String name,
        String restBaseUrl,
        String websocketBaseUrl,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
