package org.ingestor.client;

import org.ingestor.dto.binance.BinanceKlineResponse;

import java.time.OffsetDateTime;
import java.util.List;

public interface BinanceMarketDataClient {

    List<BinanceKlineResponse> getKlines(
            String symbol,
            String interval,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            Integer limit
    );
}
