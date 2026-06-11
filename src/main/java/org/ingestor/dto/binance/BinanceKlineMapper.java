package org.ingestor.dto.binance;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class BinanceKlineMapper {

    private static final int EXPECTED_MIN_SIZE = 11;

    private BinanceKlineMapper() {
    }

    public static BinanceKlineResponse fromRawKline(List<Object> rawKline) {
        if (rawKline == null || rawKline.size() < EXPECTED_MIN_SIZE) {
            throw new IllegalArgumentException("Invalid Binance kline response.");
        }

        return new BinanceKlineResponse(
                toOffsetDateTime(rawKline.get(0)),

                toBigDecimal(rawKline.get(1)),
                toBigDecimal(rawKline.get(2)),
                toBigDecimal(rawKline.get(3)),
                toBigDecimal(rawKline.get(4)),

                toBigDecimal(rawKline.get(5)),

                toOffsetDateTime(rawKline.get(6)),

                toBigDecimal(rawKline.get(7)),

                toLong(rawKline.get(8)),

                toBigDecimal(rawKline.get(9)),
                toBigDecimal(rawKline.get(10))
        );
    }

    private static OffsetDateTime toOffsetDateTime(Object value) {
        return Instant.ofEpochMilli(toLong(value))
                .atOffset(ZoneOffset.UTC);
    }

    private static BigDecimal toBigDecimal(Object value) {
        return new BigDecimal(String.valueOf(value));
    }

    private static Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.valueOf(String.valueOf(value));
    }
}
