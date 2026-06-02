package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.watermark.IngestionWatermarkResponse;
import org.ingestor.dto.watermark.UpdateIngestionWatermarkRequest;
import org.ingestor.entity.IngestionWatermark;
import org.ingestor.entity.MarketDataSubscription;
import org.ingestor.exception.ResourceNotFoundException;
import org.ingestor.repository.IngestionWatermarkRepository;
import org.ingestor.repository.MarketDataSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestionWatermarkService {

    private final IngestionWatermarkRepository watermarkRepository;
    private final MarketDataSubscriptionRepository subscriptionRepository;

    @Transactional
    public IngestionWatermarkResponse createForSubscriptionIfMissing(Long subscriptionId) {
        MarketDataSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Market data subscription not found with id: " + subscriptionId
                ));

        IngestionWatermark watermark = watermarkRepository
                .findByMarketDataSubscriptionId(subscriptionId)
                .orElseGet(() -> watermarkRepository.save(
                        IngestionWatermark.builder()
                                .marketDataSubscription(subscription)
                                .build()
                ));

        return toResponse(watermark);
    }

    @Transactional(readOnly = true)
    public List<IngestionWatermarkResponse> findAll() {
        return watermarkRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IngestionWatermarkResponse findById(Long id) {
        IngestionWatermark watermark = watermarkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingestion watermark not found with id: " + id
                ));

        return toResponse(watermark);
    }

    @Transactional(readOnly = true)
    public IngestionWatermarkResponse findBySubscriptionId(Long subscriptionId) {
        IngestionWatermark watermark = watermarkRepository
                .findByMarketDataSubscriptionId(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingestion watermark not found for subscription id: " + subscriptionId
                ));

        return toResponse(watermark);
    }

    @Transactional
    public IngestionWatermarkResponse updateBySubscriptionId(
            Long subscriptionId,
            UpdateIngestionWatermarkRequest request
    ) {
        IngestionWatermark watermark = watermarkRepository
                .findByMarketDataSubscriptionId(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingestion watermark not found for subscription id: " + subscriptionId
                ));

        if (request.lastSuccessfulOpenTime() != null) {
            watermark.setLastSuccessfulOpenTime(request.lastSuccessfulOpenTime());
        }

        if (request.lastSuccessfulCloseTime() != null) {
            watermark.setLastSuccessfulCloseTime(request.lastSuccessfulCloseTime());
        }

        if (request.lastIngestionRunId() != null) {
            watermark.setLastIngestionRunId(request.lastIngestionRunId());
        }

        return toResponse(watermarkRepository.save(watermark));
    }

    private IngestionWatermarkResponse toResponse(IngestionWatermark watermark) {
        MarketDataSubscription subscription = watermark.getMarketDataSubscription();

        return new IngestionWatermarkResponse(
                watermark.getId(),

                subscription.getId(),
                subscription.getTradingPair().getSymbol(),
                subscription.getTimeframe().getCode(),

                watermark.getLastSuccessfulOpenTime(),
                watermark.getLastSuccessfulCloseTime(),

                watermark.getLastIngestionRunId(),

                watermark.getCreatedAt(),
                watermark.getUpdatedAt()
        );
    }
}
