package org.ingestor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ingestor.dto.subscription.CreateMarketDataSubscriptionRequest;
import org.ingestor.dto.subscription.MarketDataSubscriptionResponse;
import org.ingestor.dto.subscription.UpdateMarketDataSubscriptionRequest;
import org.ingestor.service.MarketDataSubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market-data-subscriptions")
@RequiredArgsConstructor
public class MarketDataSubscriptionController {

    private final MarketDataSubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MarketDataSubscriptionResponse create(
            @Valid @RequestBody CreateMarketDataSubscriptionRequest request
    ) {
        return subscriptionService.create(request);
    }

    @GetMapping
    public List<MarketDataSubscriptionResponse> findAll(
            @RequestParam(required = false) Long tradingPairId,
            @RequestParam(required = false) Long timeframeId,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly,
            @RequestParam(required = false) String mode
    ) {
        return subscriptionService.findAll(tradingPairId, timeframeId, activeOnly, mode);
    }

    @GetMapping("/{id}")
    public MarketDataSubscriptionResponse findById(@PathVariable Long id) {
        return subscriptionService.findById(id);
    }

    @GetMapping("/trading-pair/{tradingPairId}/timeframe/{timeframeId}")
    public MarketDataSubscriptionResponse findByTradingPairAndTimeframe(
            @PathVariable Long tradingPairId,
            @PathVariable Long timeframeId
    ) {
        return subscriptionService.findByTradingPairAndTimeframe(tradingPairId, timeframeId);
    }

    @PatchMapping("/{id}")
    public MarketDataSubscriptionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMarketDataSubscriptionRequest request
    ) {
        return subscriptionService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    public MarketDataSubscriptionResponse activate(@PathVariable Long id) {
        return subscriptionService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public MarketDataSubscriptionResponse deactivate(@PathVariable Long id) {
        return subscriptionService.deactivate(id);
    }
}
