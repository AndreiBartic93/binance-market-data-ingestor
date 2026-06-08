package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.ingestionrun.CompleteIngestionRunRequest;
import org.ingestor.dto.ingestionrun.CreateIngestionRunRequest;
import org.ingestor.dto.ingestionrun.FailIngestionRunRequest;
import org.ingestor.dto.ingestionrun.IngestionRunResponse;
import org.ingestor.entity.IngestionProfile;
import org.ingestor.entity.IngestionProfileSubscription;
import org.ingestor.entity.IngestionRun;
import org.ingestor.entity.MarketDataSubscription;
import org.ingestor.entity.enums.IngestionRunStatus;
import org.ingestor.exception.ResourceNotFoundException;
import org.ingestor.repository.IngestionProfileRepository;
import org.ingestor.repository.IngestionProfileSubscriptionRepository;
import org.ingestor.repository.IngestionRunRepository;
import org.ingestor.repository.MarketDataSubscriptionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngestionRunService {

    private final IngestionRunRepository ingestionRunRepository;
    private final IngestionProfileRepository ingestionProfileRepository;
    private final MarketDataSubscriptionRepository marketDataSubscriptionRepository;
    private final IngestionProfileSubscriptionRepository profileSubscriptionRepository;

    @Transactional
    public IngestionRunResponse start(CreateIngestionRunRequest request) {
        IngestionProfile profile = ingestionProfileRepository.findById(request.ingestionProfileId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingestion profile not found with id: " + request.ingestionProfileId()
                ));

        MarketDataSubscription subscription = marketDataSubscriptionRepository
                .findById(request.marketDataSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Market data subscription not found with id: " + request.marketDataSubscriptionId()
                ));

        validateProfileSubscriptionLink(profile, subscription);

        String batchId = request.batchId() == null || request.batchId().isBlank()
                ? UUID.randomUUID().toString()
                : request.batchId();

        IngestionRun run = IngestionRun.builder()
                .ingestionProfile(profile)
                .marketDataSubscription(subscription)
                .batchId(batchId)
                .status(IngestionRunStatus.STARTED)
                .startedAt(OffsetDateTime.now())
                .fromTime(request.fromTime())
                .toTime(request.toTime())
                .metadata(request.metadata())
                .build();

        return toResponse(ingestionRunRepository.save(run));
    }

    @Transactional(readOnly = true)
    public List<IngestionRunResponse> findAll(
            IngestionRunStatus status,
            Long ingestionProfileId,
            Long marketDataSubscriptionId,
            String batchId
    ) {
        return ingestionRunRepository
                .search(status, ingestionProfileId, marketDataSubscriptionId, batchId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IngestionRunResponse findById(Long id) {
        return toResponse(getRunById(id));
    }

    @Transactional(readOnly = true)
    public List<IngestionRunResponse> findByBatchId(String batchId) {
        return ingestionRunRepository.findByBatchIdOrderByStartedAtDesc(batchId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public IngestionRunResponse complete(Long id, CompleteIngestionRunRequest request) {
        IngestionRun run = getRunById(id);

        IngestionRunStatus finalStatus = request.status() == null
                ? IngestionRunStatus.SUCCESS
                : request.status();

        if (finalStatus != IngestionRunStatus.SUCCESS
                && finalStatus != IngestionRunStatus.PARTIAL
                && finalStatus != IngestionRunStatus.STOPPED) {
            throw new DataIntegrityViolationException(
                    "Completion status must be SUCCESS, PARTIAL, or STOPPED."
            );
        }

        run.setStatus(finalStatus);
        run.setFinishedAt(OffsetDateTime.now());

        if (request.recordsRequested() != null) {
            run.setRecordsRequested(request.recordsRequested());
        }

        if (request.recordsReceived() != null) {
            run.setRecordsReceived(request.recordsReceived());
        }

        if (request.recordsInserted() != null) {
            run.setRecordsInserted(request.recordsInserted());
        }

        if (request.recordsSkipped() != null) {
            run.setRecordsSkipped(request.recordsSkipped());
        }

        if (request.recordsFailed() != null) {
            run.setRecordsFailed(request.recordsFailed());
        }

        if (request.messagesReceived() != null) {
            run.setMessagesReceived(request.messagesReceived());
        }

        if (request.messagesProcessed() != null) {
            run.setMessagesProcessed(request.messagesProcessed());
        }

        if (request.messagesFailed() != null) {
            run.setMessagesFailed(request.messagesFailed());
        }

        if (request.metadata() != null) {
            run.setMetadata(request.metadata());
        }

        return toResponse(ingestionRunRepository.save(run));
    }

    @Transactional
    public IngestionRunResponse fail(Long id, FailIngestionRunRequest request) {
        IngestionRun run = getRunById(id);

        run.setStatus(IngestionRunStatus.FAILED);
        run.setFinishedAt(OffsetDateTime.now());
        run.setErrorMessage(request.errorMessage());

        if (request.recordsFailed() != null) {
            run.setRecordsFailed(request.recordsFailed());
        }

        if (request.messagesFailed() != null) {
            run.setMessagesFailed(request.messagesFailed());
        }

        if (request.metadata() != null) {
            run.setMetadata(request.metadata());
        }

        return toResponse(ingestionRunRepository.save(run));
    }

    @Transactional
    public IngestionRunResponse stop(Long id) {
        IngestionRun run = getRunById(id);

        run.setStatus(IngestionRunStatus.STOPPED);
        run.setFinishedAt(OffsetDateTime.now());

        return toResponse(ingestionRunRepository.save(run));
    }

    private IngestionRun getRunById(Long id) {
        return ingestionRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingestion run not found with id: " + id
                ));
    }

    private void validateProfileSubscriptionLink(
            IngestionProfile profile,
            MarketDataSubscription subscription
    ) {
        IngestionProfileSubscription link = profileSubscriptionRepository
                .findByIngestionProfileIdAndMarketDataSubscriptionId(
                        profile.getId(),
                        subscription.getId()
                )
                .orElseThrow(() -> new DataIntegrityViolationException(
                        "Ingestion profile is not linked to this market data subscription."
                ));

        if (!link.isActive()) {
            throw new DataIntegrityViolationException(
                    "Ingestion profile subscription link is inactive."
            );
        }
    }





    private IngestionRunResponse toResponse(IngestionRun run) {
        IngestionProfile profile = run.getIngestionProfile();
        MarketDataSubscription subscription = run.getMarketDataSubscription();

        return new IngestionRunResponse(
                run.getId(),

                profile.getId(),
                profile.getName(),
                profile.getIngestionMethod(),

                subscription.getId(),
                subscription.getTradingPair().getSymbol(),
                subscription.getTimeframe().getCode(),

                run.getBatchId(),
                run.getStatus(),

                run.getStartedAt(),
                run.getFinishedAt(),

                run.getFromTime(),
                run.getToTime(),

                run.getRecordsRequested(),
                run.getRecordsReceived(),
                run.getRecordsInserted(),
                run.getRecordsSkipped(),
                run.getRecordsFailed(),

                run.getMessagesReceived(),
                run.getMessagesProcessed(),
                run.getMessagesFailed(),

                run.getErrorMessage(),
                run.getMetadata(),

                run.getCreatedAt(),
                run.getUpdatedAt()
        );
    }
}
