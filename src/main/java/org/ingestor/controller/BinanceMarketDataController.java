package org.ingestor.controller;

import lombok.RequiredArgsConstructor;
import org.ingestor.client.BinanceMarketDataClient;
import org.ingestor.dto.binance.BinanceKlineResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/binance/market-data")
@RequiredArgsConstructor
public class BinanceMarketDataController {
    private final BinanceMarketDataClient binanceMarketDataClient;

    @GetMapping("/klines")
    public List<BinanceKlineResponse> getKlines(
            @RequestParam String symbol,
            @RequestParam String interval,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startTime,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime endTime,

            @RequestParam(required = false, defaultValue = "500")
            Integer limit
    ) {
        return binanceMarketDataClient.getKlines(
                symbol,
                interval,
                startTime,
                endTime,
                limit
        );
    }
}
