package org.ingestor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ingestor.dto.historicalimport.ImportHistoricalCandlesRequest;
import org.ingestor.dto.historicalimport.ImportHistoricalCandlesResponse;
import org.ingestor.service.HistoricalCandleImportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingestion/historical")
@RequiredArgsConstructor
public class HistoricalCandleImportController {

    private final HistoricalCandleImportService historicalCandleImportService;

    @PostMapping("/subscriptions/{subscriptionId}/import")
    @ResponseStatus(HttpStatus.CREATED)
    public ImportHistoricalCandlesResponse importBySubscription(
            @PathVariable Long subscriptionId,
            @Valid @RequestBody(required = false) ImportHistoricalCandlesRequest request
    ) {
        return historicalCandleImportService.importBySubscription(
                subscriptionId,
                request
        );
    }
}
