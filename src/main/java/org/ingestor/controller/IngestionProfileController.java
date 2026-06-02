package org.ingestor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ingestor.dto.profile.CreateIngestionProfileRequest;
import org.ingestor.dto.profile.IngestionProfileResponse;
import org.ingestor.dto.profile.UpdateIngestionProfileRequest;
import org.ingestor.entity.enums.IngestionMethod;
import org.ingestor.service.IngestionProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingestion-profiles")
@RequiredArgsConstructor
public class IngestionProfileController {

    private final IngestionProfileService ingestionProfileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngestionProfileResponse create(
            @Valid @RequestBody CreateIngestionProfileRequest request
    ) {
        return ingestionProfileService.create(request);
    }

    @GetMapping
    public List<IngestionProfileResponse> findAll(
            @RequestParam(required = false) IngestionMethod ingestionMethod,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly
    ) {
        return ingestionProfileService.findAll(ingestionMethod, activeOnly);
    }

    @GetMapping("/{id}")
    public IngestionProfileResponse findById(@PathVariable Long id) {
        return ingestionProfileService.findById(id);
    }

    @GetMapping("/name/{name}")
    public IngestionProfileResponse findByName(@PathVariable String name) {
        return ingestionProfileService.findByName(name);
    }

    @PatchMapping("/{id}")
    public IngestionProfileResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIngestionProfileRequest request
    ) {
        return ingestionProfileService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    public IngestionProfileResponse activate(@PathVariable Long id) {
        return ingestionProfileService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public IngestionProfileResponse deactivate(@PathVariable Long id) {
        return ingestionProfileService.deactivate(id);
    }
}