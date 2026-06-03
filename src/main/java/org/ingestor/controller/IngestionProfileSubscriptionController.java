package org.ingestor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ingestor.dto.profilesubscription.CreateIngestionProfileSubscriptionRequest;
import org.ingestor.dto.profilesubscription.IngestionProfileSubscriptionResponse;
import org.ingestor.dto.profilesubscription.UpdateIngestionProfileSubscriptionRequest;
import org.ingestor.service.IngestionProfileSubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingestion-profile-subscriptions")
@RequiredArgsConstructor
public class IngestionProfileSubscriptionController {

    private final IngestionProfileSubscriptionService profileSubscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngestionProfileSubscriptionResponse create(
            @Valid @RequestBody CreateIngestionProfileSubscriptionRequest request
    ) {
        return profileSubscriptionService.create(request);
    }

    @GetMapping
    public List<IngestionProfileSubscriptionResponse> findAll(
            @RequestParam(required = false) Long ingestionProfileId,
            @RequestParam(required = false) Long marketDataSubscriptionId,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly
    ) {
        return profileSubscriptionService.findAll(
                ingestionProfileId,
                marketDataSubscriptionId,
                activeOnly
        );
    }

    @GetMapping("/{id}")
    public IngestionProfileSubscriptionResponse findById(@PathVariable Long id) {
        return profileSubscriptionService.findById(id);
    }

    @GetMapping("/profile/{ingestionProfileId}/subscription/{marketDataSubscriptionId}")
    public IngestionProfileSubscriptionResponse findByProfileAndSubscription(
            @PathVariable Long ingestionProfileId,
            @PathVariable Long marketDataSubscriptionId
    ) {
        return profileSubscriptionService.findByProfileAndSubscription(
                ingestionProfileId,
                marketDataSubscriptionId
        );
    }

    @PatchMapping("/{id}")
    public IngestionProfileSubscriptionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIngestionProfileSubscriptionRequest request
    ) {
        return profileSubscriptionService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    public IngestionProfileSubscriptionResponse activate(@PathVariable Long id) {
        return profileSubscriptionService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public IngestionProfileSubscriptionResponse deactivate(@PathVariable Long id) {
        return profileSubscriptionService.deactivate(id);
    }
}
