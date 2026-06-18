package org.ingestor.client;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.binance.BinanceKlineMapper;
import org.ingestor.dto.binance.BinanceKlineResponse;
import org.ingestor.exception.BinanceApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BinanceRestMarketDataClient implements BinanceMarketDataClient {

    private static final int DEFAULT_LIMIT = 500;
    private static final int MAX_LIMIT = 1000;

    private final RestClient binanceRestClient;

    @Override
    public List<BinanceKlineResponse> getKlines(
            String symbol,
            String interval,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            Integer limit
    ) {
        String normalizedSymbol = normalizeSymbol(symbol);
        String normalizedInterval = normalizeInterval(interval);
        int resolvedLimit = resolveLimit(limit);

        try {
            List<List<Object>> rawResponse = binanceRestClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/api/v3/klines")
                                .queryParam("symbol", normalizedSymbol)
                                .queryParam("interval", normalizedInterval)
                                .queryParam("limit", resolvedLimit);

                        if (startTime != null) {
                            builder.queryParam("startTime", startTime.toInstant().toEpochMilli());
                        }

                        if (endTime != null) {
                            builder.queryParam("endTime", endTime.toInstant().toEpochMilli());
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (rawResponse == null) {
                return List.of();
            }

            return rawResponse.stream()
                    .map(BinanceKlineMapper::fromRawKline)
                    .toList();

        } catch (RestClientResponseException exception) {
            throw new BinanceApiException(
                    "Binance API returned error. Status: "
                            + exception.getStatusCode()
                            + ", body: "
                            + exception.getResponseBodyAsString(),
                    exception
            );
        } catch (RestClientException exception) {
            throw new BinanceApiException(
                    "Could not call Binance API: " + exception.getMessage(),
                    exception
            );
        }
    }

    private String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol must not be blank.");
        }

        return symbol.trim().toUpperCase();
    }

    private String normalizeInterval(String interval) {
        if (interval == null || interval.isBlank()) {
            throw new IllegalArgumentException("Interval must not be blank.");
        }

        return interval.trim();
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }

        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Limit must be between 1 and 1000.");
        }

        return limit;
    }
}
