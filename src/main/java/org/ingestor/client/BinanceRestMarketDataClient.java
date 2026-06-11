package org.ingestor.client;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.binance.BinanceKlineResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BinanceRestMarketDataClient implements BinanceMarketDataClient {

    private static final int DEFAULT_LIMIT = 500;
    private static final int MAX_LIMIT = 1000;

    private final RestClient binanceRestClient;

    @Override
    public List<BinanceKlineResponse> getKlines(String symbol, String interval, OffsetDateTime startTime, OffsetDateTime endTime, Integer limit) {
        return List.of();
    }
}
