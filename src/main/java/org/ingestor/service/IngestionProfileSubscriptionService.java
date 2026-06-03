package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.profilesubscription.CreateIngestionProfileSubscriptionRequest;
import org.ingestor.dto.profilesubscription.IngestionProfileSubscriptionResponse;
import org.ingestor.dto.profilesubscription.UpdateIngestionProfileSubscriptionRequest;
import org.ingestor.entity.IngestionProfile;
import org.ingestor.entity.IngestionProfileSubscription;
import org.ingestor.entity.MarketDataSubscription;
import org.ingestor.entity.enums.IngestionMethod;
import org.ingestor.exception.ResourceNotFoundException;
import org.ingestor.repository.IngestionProfileRepository;
import org.ingestor.repository.IngestionProfileSubscriptionRepository;
import org.ingestor.repository.MarketDataSubscriptionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestionProfileSubscriptionService {

    private final IngestionProfileSubscriptionRepository profileSubscriptionRepository;
    private final IngestionProfileRepository ingestionProfileRepository;
    private final MarketDataSubscriptionRepository marketDataSubscriptionRepository;

    @Transactional
    public IngestionProfileSubscriptionResponse create(CreateIngestionProfileSubscriptionRequest request) {
        IngestionProfile profile = ingestionProfileRepository.findById(request.ingestionProfileId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingestion Profile Not Found with ID: " + request.ingestionProfileId()
                ));

        MarketDataSubscription subscription = marketDataSubscriptionRepository.findById(request.marketDataSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Market Data Subscription Not Found with ID: " + request.marketDataSubscriptionId()
                ));

        if (profileSubscriptionRepository.existsByIngestionProfileIdAndMarketDataSubscriptionId(
                profile.getId(),
                subscription.getId()
        )) {
            throw new DataIntegrityViolationException(
                    "Ingestion profiles is already linked to this market data subscription"
            );
        }

        validateProfileCompatibility(profile, subscription);

        IngestionProfileSubscription profileSubscription = IngestionProfileSubscription.builder()
                .ingestionProfile(profile)
                .marketDataSubscription(subscription)
                .active(true)
                .build();

        return toResponse(profileSubscriptionRepository.save(profileSubscription));
    }

    @Transactional(readOnly = true)
    public List<IngestionProfileSubscriptionResponse> findAll(
            Long ingestionProfileId,
            Long marketDataSubscriptionId,
            Boolean activeOnly
    ) {
        List<IngestionProfileSubscription> links;

        if (Boolean.TRUE.equals(activeOnly)) {
            links = profileSubscriptionRepository.findByActiveTrue();
        } else if (ingestionProfileId != null) {
            links = profileSubscriptionRepository.findByIngestionProfileId(ingestionProfileId);
        } else if (marketDataSubscriptionId != null) {
            links = profileSubscriptionRepository.findByMarketDataSubscriptionId(marketDataSubscriptionId);
        } else {
            links = profileSubscriptionRepository.findAll();
        }

        return links.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IngestionProfileSubscriptionResponse findById(Long id) {
        IngestionProfileSubscription link = getById(id);
        return toResponse(link);
    }

    @Transactional
    public IngestionProfileSubscriptionResponse activate(Long id) {
        IngestionProfileSubscription link = getById(id);
        link.setActive(true);

        return toResponse(profileSubscriptionRepository.save(link));
    }

    @Transactional
    public IngestionProfileSubscriptionResponse deactivate(Long id) {
        IngestionProfileSubscription link = getById(id);
        link.setActive(false);

        return toResponse(profileSubscriptionRepository.save(link));
    }

    private IngestionProfileSubscription getById(Long id) {
        return profileSubscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingestion profile subscription link not found with id: " + id
                ));
    }

    @Transactional(readOnly = true)
    public IngestionProfileSubscriptionResponse findByProfileAndSubscription(
            Long ingestionProfileId,
            Long marketDataSubscriptionId
    ) {
        IngestionProfileSubscription link = profileSubscriptionRepository
                .findByIngestionProfileIdAndMarketDataSubscriptionId(
                        ingestionProfileId,
                        marketDataSubscriptionId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingestion profile subscription link not found."
                ));

        return toResponse(link);
    }

    @Transactional
    public IngestionProfileSubscriptionResponse update(
            Long id,
            UpdateIngestionProfileSubscriptionRequest request
    ) {
        IngestionProfileSubscription link = getById(id);

        if (request.active() != null) {
            link.setActive(request.active());
        }

        return toResponse(profileSubscriptionRepository.save(link));
    }

    private void validateProfileCompatibility(IngestionProfile profile,
                                              MarketDataSubscription subscription) {
        IngestionMethod method = profile.getIngestionMethod();
        if (method == IngestionMethod.REST_HISTORICAL && !subscription.isCollectHistorical()) {
            throw new DataIntegrityViolationException(
                    "REST_HISTORICAL profile can only be linked to subscriptions with collectHistorical=true."
            );
        }
        if (method == IngestionMethod.SCHEDULED_SYNC && !subscription.isCollectScheduled()) {
            throw new DataIntegrityViolationException(
                    "SCHEDULED_SYNC profile can only be linked to subscriptions with collectScheduled=true."
            );
        }
        if (method == IngestionMethod.WEBSOCKET_LIVE && !subscription.isCollectLive()) {
            throw new DataIntegrityViolationException(
                    "WEBSOCKET_LIVE profile can only be linked to subscriptions with collectLive=true."
            );
        }
    }

    private IngestionProfileSubscriptionResponse toResponse(IngestionProfileSubscription link) {
        IngestionProfile profile = link.getIngestionProfile();
        MarketDataSubscription subscription = link.getMarketDataSubscription();

        return new IngestionProfileSubscriptionResponse(
                link.getId(),

                profile.getId(),
                profile.getName(),
                profile.getIngestionMethod(),

                subscription.getId(),
                subscription.getTradingPair().getSymbol(),
                subscription.getTimeframe().getCode(),

                subscription.isCollectHistorical(),
                subscription.isCollectScheduled(),
                subscription.isCollectLive(),
                subscription.getWebsocketStreamName(),

                link.isActive(),

                link.getCreatedAt(),
                link.getUpdatedAt()
        );
    }
}
