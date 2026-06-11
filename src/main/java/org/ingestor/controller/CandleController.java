package org.ingestor.controller;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.candle.CandleResponse;
import org.ingestor.service.CandleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/candles")
@RequiredArgsConstructor
public class CandleController {

    private final CandleService candleService;

    @GetMapping
    public List<CandleResponse> findAll(
            @RequestParam(required = false) Long marketDataSubscriptionId,
            @RequestParam(required = false) Long tradingPairId,
            @RequestParam(required = false) Long timeframeId,
            @RequestParam(required = false) Long ingestionRunId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime fromTime,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime toTime
    ) {
        return candleService.findAll(
                marketDataSubscriptionId,
                tradingPairId,
                timeframeId,
                ingestionRunId,
                fromTime,
                toTime
        );
    }

    @GetMapping("/{id}")
    public CandleResponse findById(@PathVariable Long id) {
        return candleService.findById(id);
    }
}
