package org.ingestor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ingestor.dto.ingestionrun.CompleteIngestionRunRequest;
import org.ingestor.dto.ingestionrun.CreateIngestionRunRequest;
import org.ingestor.dto.ingestionrun.FailIngestionRunRequest;
import org.ingestor.dto.ingestionrun.IngestionRunResponse;
import org.ingestor.entity.enums.IngestionRunStatus;
import org.ingestor.service.IngestionRunService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingestion-runs")
@RequiredArgsConstructor
public class IngestionRunController {

    private final IngestionRunService ingestionRunService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngestionRunResponse start(
            @Valid @RequestBody CreateIngestionRunRequest request
    ) {
        return ingestionRunService.start(request);
    }

    @GetMapping
    public List<IngestionRunResponse> findAll(
            @RequestParam(required = false) IngestionRunStatus status,
            @RequestParam(required = false) Long ingestionProfileId,
            @RequestParam(required = false) Long marketDataSubscriptionId,
            @RequestParam(required = false) String batchId
    ) {
        return ingestionRunService.findAll(
                status,
                ingestionProfileId,
                marketDataSubscriptionId,
                batchId
        );
    }

    @GetMapping("/{id}")
    public IngestionRunResponse findById(@PathVariable Long id) {
        return ingestionRunService.findById(id);
    }

    @GetMapping("/batch/{batchId}")
    public List<IngestionRunResponse> findByBatchId(@PathVariable String batchId) {
        return ingestionRunService.findByBatchId(batchId);
    }

    @PatchMapping("/{id}/complete")
    public IngestionRunResponse complete(
            @PathVariable Long id,
            @RequestBody CompleteIngestionRunRequest request
    ) {
        return ingestionRunService.complete(id, request);
    }

    @PatchMapping("/{id}/fail")
    public IngestionRunResponse fail(
            @PathVariable Long id,
            @RequestBody FailIngestionRunRequest request
    ) {
        return ingestionRunService.fail(id, request);
    }

    @PatchMapping("/{id}/stop")
    public IngestionRunResponse stop(@PathVariable Long id) {
        return ingestionRunService.stop(id);
    }
}
