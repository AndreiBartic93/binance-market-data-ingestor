package org.ingestor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ingestor.dto.tradingpair.CreateTradingPairRequest;
import org.ingestor.dto.tradingpair.TradingPairResponse;
import org.ingestor.dto.tradingpair.UpdateTradingPairRequest;
import org.ingestor.service.TradingPairService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trading-pairs")
@RequiredArgsConstructor
public class TradingPairController {

    private final TradingPairService tradingPairService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TradingPairResponse create(@Valid @RequestBody CreateTradingPairRequest request) {
        return tradingPairService.create(request);
    }

    @GetMapping
    public List<TradingPairResponse> findAll(
            @RequestParam(required = false) Long exchangeId,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly
    ) {
        return tradingPairService.findAll(exchangeId, activeOnly);
    }

    @GetMapping("/{id}")
    public TradingPairResponse findById(@PathVariable Long id) {
        return tradingPairService.findById(id);
    }

    @GetMapping("/exchange/{exchangeId}/symbol/{symbol}")
    public TradingPairResponse findByExchangeAndSymbol(
            @PathVariable Long exchangeId,
            @PathVariable String symbol) {
        return tradingPairService.findByExchangeAndSymbol(exchangeId, symbol);
    }

    @PatchMapping("/{id}")
    public TradingPairResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTradingPairRequest request) {
        return tradingPairService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    public TradingPairResponse activate(@PathVariable Long id) {
        return tradingPairService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public TradingPairResponse deactivate(@PathVariable Long id) {
        return tradingPairService.deactivate(id);
    }
}
