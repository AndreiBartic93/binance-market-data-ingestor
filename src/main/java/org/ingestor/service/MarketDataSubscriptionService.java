package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.subscription.CreateMarketDataSubscriptionRequest;
import org.ingestor.dto.subscription.MarketDataSubscriptionResponse;
import org.ingestor.dto.subscription.UpdateMarketDataSubscriptionRequest;
import org.ingestor.entity.MarketDataSubscription;
import org.ingestor.entity.Timeframe;
import org.ingestor.entity.TradingPair;
import org.ingestor.exception.ResourceNotFoundException;
import org.ingestor.repository.MarketDataSubscriptionRepository;
import org.ingestor.repository.TimeframeRepository;
import org.ingestor.repository.TradingPairRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketDataSubscriptionService {

    private final MarketDataSubscriptionRepository subscriptionRepository;
    private final TradingPairRepository tradingPairRepository;
    private final TimeframeRepository timeframeRepository;

    @Transactional
    public MarketDataSubscriptionResponse create(CreateMarketDataSubscriptionRequest request) {
        TradingPair tradingPair = tradingPairRepository.findById(request.tradingPairId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trading pair not found with id: " + request.tradingPairId()
                ));

        Timeframe timeframe = timeframeRepository.findById(request.timeframeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Timeframe not found with id: " + request.timeframeId()
                ));

        if (subscriptionRepository.existsByTradingPairIdAndTimeframeId(tradingPair.getId(), timeframe.getId())) {
            throw new DataIntegrityViolationException(
                    "Market data subscription already exists for trading pair and timeframe."
            );
        }

        boolean collectHistorical = request.collectHistorical() == null || request.collectHistorical();
        boolean collectScheduled = request.collectScheduled() == null || request.collectScheduled();
        boolean collectLive = Boolean.TRUE.equals(request.collectLive());

        String websocketStreamName = resolveWebsocketStreamName(
                request.websocketStreamName(),
                tradingPair.getSymbol(),
                timeframe.getCode(),
                collectLive
        );

        MarketDataSubscription subscription = MarketDataSubscription.builder()
                .tradingPair(tradingPair)
                .timeframe(timeframe)
                .collectHistorical(collectHistorical)
                .collectScheduled(collectScheduled)
                .collectLive(collectLive)
                .websocketStreamName(websocketStreamName)
                .active(true)
                .build();

        MarketDataSubscription savedSubscription = subscriptionRepository.save(subscription);

        return toResponse(savedSubscription);
    }

    @Transactional(readOnly = true)
    public List<MarketDataSubscriptionResponse> findAll(
            Long tradingPairId,
            Long timeframeId,
            Boolean activeOnly,
            String mode
    ) {
        List<MarketDataSubscription> subscriptions;

        if (Boolean.TRUE.equals(activeOnly)) {
            subscriptions = subscriptionRepository.findByActiveTrue();
        } else if (tradingPairId != null) {
            subscriptions = subscriptionRepository.findByTradingPairId(tradingPairId);
        } else if (timeframeId != null) {
            subscriptions = subscriptionRepository.findByTimeframeId(timeframeId);
        } else if ("historical".equalsIgnoreCase(mode)) {
            subscriptions = subscriptionRepository.findByCollectHistoricalTrueAndActiveTrue();
        } else if ("scheduled".equalsIgnoreCase(mode)) {
            subscriptions = subscriptionRepository.findByCollectScheduledTrueAndActiveTrue();
        } else if ("live".equalsIgnoreCase(mode)) {
            subscriptions = subscriptionRepository.findByCollectLiveTrueAndActiveTrue();
        } else {
            subscriptions = subscriptionRepository.findAll();
        }

        return subscriptions.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MarketDataSubscriptionResponse findById(Long id) {
        MarketDataSubscription subscription = getSubscriptionById(id);
        return toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public MarketDataSubscriptionResponse findByTradingPairAndTimeframe(Long tradingPairId, Long timeframeId) {
        MarketDataSubscription subscription = subscriptionRepository
                .findByTradingPairIdAndTimeframeId(tradingPairId, timeframeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Market data subscription not found for trading pair id "
                                + tradingPairId + " and timeframe id " + timeframeId
                ));

        return toResponse(subscription);
    }

    @Transactional
    public MarketDataSubscriptionResponse update(Long id, UpdateMarketDataSubscriptionRequest request) {
        MarketDataSubscription subscription = getSubscriptionById(id);

        if (request.collectHistorical() != null) {
            subscription.setCollectHistorical(request.collectHistorical());
        }

        if (request.collectScheduled() != null) {
            subscription.setCollectScheduled(request.collectScheduled());
        }

        if (request.collectLive() != null) {
            subscription.setCollectLive(request.collectLive());
        }

        if (request.websocketStreamName() != null) {
            subscription.setWebsocketStreamName(normalizeWebsocketStreamName(request.websocketStreamName()));
        } else if (subscription.isCollectLive() && subscription.getWebsocketStreamName() == null) {
            subscription.setWebsocketStreamName(
                    buildKlineStreamName(
                            subscription.getTradingPair().getSymbol(),
                            subscription.getTimeframe().getCode()
                    )
            );
        }

        if (request.active() != null) {
            subscription.setActive(request.active());
        }

        MarketDataSubscription savedSubscription = subscriptionRepository.save(subscription);

        return toResponse(savedSubscription);
    }

    @Transactional
    public MarketDataSubscriptionResponse activate(Long id) {
        MarketDataSubscription subscription = getSubscriptionById(id);
        subscription.setActive(true);

        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    public MarketDataSubscriptionResponse deactivate(Long id) {
        MarketDataSubscription subscription = getSubscriptionById(id);
        subscription.setActive(false);

        return toResponse(subscriptionRepository.save(subscription));
    }

    private MarketDataSubscription getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Market data subscription not found with id: " + id
                ));
    }

    private MarketDataSubscriptionResponse toResponse(MarketDataSubscription subscription) {
        return new MarketDataSubscriptionResponse(
                subscription.getId(),

                subscription.getTradingPair().getId(),
                subscription.getTradingPair().getSymbol(),

                subscription.getTimeframe().getId(),
                subscription.getTimeframe().getCode(),

                subscription.isCollectHistorical(),
                subscription.isCollectScheduled(),
                subscription.isCollectLive(),

                subscription.getWebsocketStreamName(),

                subscription.isActive(),

                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }

    private String resolveWebsocketStreamName(
            String requestedStreamName,
            String tradingPairSymbol,
            String timeframeCode,
            boolean collectLive
    ) {
        if (requestedStreamName != null && !requestedStreamName.isBlank()) {
            return normalizeWebsocketStreamName(requestedStreamName);
        }

        if (collectLive) {
            return buildKlineStreamName(tradingPairSymbol, timeframeCode);
        }

        return null;
    }

    private String buildKlineStreamName(String tradingPairSymbol, String timeframeCode) {
        return tradingPairSymbol.toLowerCase() + "@kline_" + timeframeCode;
    }

    private String normalizeWebsocketStreamName(String streamName) {
        return streamName.trim().toLowerCase();
    }
}