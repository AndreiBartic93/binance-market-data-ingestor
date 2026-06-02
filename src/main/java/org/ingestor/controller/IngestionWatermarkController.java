package org.ingestor.controller;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.watermark.IngestionWatermarkResponse;
import org.ingestor.dto.watermark.UpdateIngestionWatermarkRequest;
import org.ingestor.service.IngestionWatermarkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingestion-watermarks")
@RequiredArgsConstructor
public class IngestionWatermarkController {

    private final IngestionWatermarkService watermarkService;

    @PostMapping("/subscription/{subscriptionId}")
    public IngestionWatermarkResponse createForSubscriptionIfMissing(
            @PathVariable Long subscriptionId
    ) {
        return watermarkService.createForSubscriptionIfMissing(subscriptionId);
    }

    @GetMapping
    public List<IngestionWatermarkResponse> findAll() {
        return watermarkService.findAll();
    }

    @GetMapping("/{id}")
    public IngestionWatermarkResponse findById(@PathVariable Long id) {
        return watermarkService.findById(id);
    }

    @GetMapping("/subscription/{subscriptionId}")
    public IngestionWatermarkResponse findBySubscriptionId(
            @PathVariable Long subscriptionId
    ) {
        return watermarkService.findBySubscriptionId(subscriptionId);
    }

    @PatchMapping("/subscription/{subscriptionId}")
    public IngestionWatermarkResponse updateBySubscriptionId(
            @PathVariable Long subscriptionId,
            @RequestBody UpdateIngestionWatermarkRequest request
    ) {
        return watermarkService.updateBySubscriptionId(subscriptionId, request);
    }
}
