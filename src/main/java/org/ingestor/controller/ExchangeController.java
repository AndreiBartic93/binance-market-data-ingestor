package org.ingestor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ingestor.dto.exchange.UpdateExchangeRequest;
import org.ingestor.dto.exchange.CreateExchangeRequest;
import org.ingestor.dto.exchange.ExchangeResponse;
import org.ingestor.service.ExchangeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExchangeResponse create(@Valid @RequestBody CreateExchangeRequest request) {
        return exchangeService.create(request);
    }

    @GetMapping
    public List<ExchangeResponse> findAll() {
        return exchangeService.findAll();
    }

    @GetMapping("/{id}")
    public ExchangeResponse findById(@PathVariable Long id) {
        return exchangeService.findById(id);
    }

    @PatchMapping("/{id}")
    public ExchangeResponse update(@PathVariable Long id, @Valid @RequestBody UpdateExchangeRequest request) {
        return exchangeService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    public ExchangeResponse activate(@PathVariable Long id) {
        return exchangeService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public ExchangeResponse deactivate(@PathVariable Long id) {
        return exchangeService.deactivate(id);
    }
}
