package org.ingestor.dto.historicalimport;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.OffsetDateTime;
import java.util.Map;

public record ImportHistoricalCandlesRequest(
        /**
         * Optional start time.
         *
         * If missing, the service will try to continue from the ingestion watermark.
         * If watermark is empty too, the request will fail.
         */
        OffsetDateTime fromTime,

        /**
         * Optional end time.
         *
         * If missing, current UTC time will be used.
         */
        OffsetDateTime toTime,

        /**
         * Binance klines max limit is 1000.
         */
        @Min(1)
        @Max(1000)
        Integer limit,

        /**
         * Optional metadata stored on the ingestion run.
         */
        Map<String, Object> metadata
) {

    public static ImportHistoricalCandlesRequest empty() {
        return new ImportHistoricalCandlesRequest(
                null,
                null,
                null,
                null
        );
    }
}