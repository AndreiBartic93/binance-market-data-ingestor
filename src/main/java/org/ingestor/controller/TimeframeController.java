package org.ingestor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ingestor.dto.timeframe.CreateTimeframeRequest;
import org.ingestor.dto.timeframe.TimeframeResponse;
import org.ingestor.dto.timeframe.UpdateTimeframeRequest;
import org.ingestor.service.TimeframeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timeframes")
@RequiredArgsConstructor
public class TimeframeController {
    private final TimeframeService timeframeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeframeResponse create(@Valid @RequestBody CreateTimeframeRequest request) {
        return timeframeService.create(request);
    }

    @GetMapping
    public List<TimeframeResponse> findAll(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly
    ) {
        return timeframeService.findAll(activeOnly);
    }

    @GetMapping("/{id}")
    public TimeframeResponse findById(@PathVariable Long id) {
        return timeframeService.findById(id);
    }

    @GetMapping("/code/{code}")
    public TimeframeResponse findByCode(@PathVariable String code) {
        return timeframeService.findByCode(code);
    }

    @PatchMapping("/{id}")
    public TimeframeResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTimeframeRequest request
    ) {
        return timeframeService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    public TimeframeResponse activate(@PathVariable Long id) {
        return timeframeService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public TimeframeResponse deactivate(@PathVariable Long id) {
        return timeframeService.deactivate(id);
    }
}
