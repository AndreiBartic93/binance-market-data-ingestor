package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.ingestor.client.BinanceMarketDataClient;
import org.ingestor.dto.binance.BinanceKlineResponse;
import org.ingestor.dto.historicalimport.ImportHistoricalCandlesRequest;
import org.ingestor.dto.historicalimport.ImportHistoricalCandlesResponse;
import org.ingestor.entity.*;
import org.ingestor.entity.enums.CandleSource;
import org.ingestor.entity.enums.IngestionMethod;
import org.ingestor.entity.enums.IngestionRunStatus;
import org.ingestor.exception.ResourceNotFoundException;
import org.ingestor.repository.CandleRepository;
import org.ingestor.repository.IngestionProfileSubscriptionRepository;
import org.ingestor.repository.IngestionRunRepository;
import org.ingestor.repository.IngestionWatermarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistoricalCandleImportService {

    private static final int DEFAULT_LIMIT = 1000;

    private final BinanceMarketDataClient binanceMarketDataClient;
    private final IngestionProfileSubscriptionRepository profileSubscriptionRepository;
    private final IngestionRunRepository ingestionRunRepository;
    private final IngestionWatermarkRepository watermarkRepository;
    private final CandleRepository candleRepository;

    @Transactional
    public ImportHistoricalCandlesResponse importBySubscription(
            Long subscriptionId,
            ImportHistoricalCandlesRequest request
    ) {
        ImportHistoricalCandlesRequest resolvedRequest = request == null
                ? ImportHistoricalCandlesRequest.empty()
                : request;

        IngestionProfileSubscription link = profileSubscriptionRepository
                .findActiveBySubscriptionIdAndIngestionMethod(
                        subscriptionId,
                        IngestionMethod.REST_HISTORICAL
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active REST_HISTORICAL profile link found for subscription id: " + subscriptionId
                ));

        IngestionProfile profile = link.getIngestionProfile();
        MarketDataSubscription subscription = link.getMarketDataSubscription();

        validateSubscription(subscription);

        OffsetDateTime fromTime = resolveFromTime(subscription, resolvedRequest);
        OffsetDateTime toTime = resolveToTime(resolvedRequest);
        int limit = resolveLimit(resolvedRequest.limit());

        validateTimeRange(fromTime, toTime);

        IngestionRun run = startRun(
                profile,
                subscription,
                fromTime,
                toTime,
                limit,
                resolvedRequest.metadata()
        );

        try {
            List<BinanceKlineResponse> klines = binanceMarketDataClient.getKlines(
                    subscription.getTradingPair().getSymbol(),
                    subscription.getTimeframe().getCode(),
                    fromTime,
                    toTime,
                    limit
            );

            CandleImportStats stats = persistCandles(
                    klines,
                    subscription,
                    run
            );

            boolean watermarkUpdated = completeRunAndUpdateWatermark(
                    run,
                    subscription,
                    limit,
                    klines.size(),
                    stats
            );

            String message = watermarkUpdated
                    ? "Historical import finished and watermark was updated successfully."
                    : "Historical import finished, but watermark was not updated because no closed candle was processed.";

            return toResponse(
                    run,
                    subscription,
                    stats.firstImportedOpenTime(),
                    stats.lastImportedOpenTime(),
                    watermarkUpdated,
                    message
            );

        } catch (Exception exception) {
            failRun(run, exception);

            return toResponse(
                    run,
                    subscription,
                    null,
                    null,
                    false,
                    "Historical import failed: " + exception.getMessage()
            );
        }
    }

    private void validateSubscription(MarketDataSubscription subscription) {
        if (!subscription.isActive()) {
            throw new IllegalArgumentException("Market data subscription is inactive.");
        }

        if (!subscription.isCollectHistorical()) {
            throw new IllegalArgumentException(
                    "Market data subscription is not configured for historical collection."
            );
        }
    }

    private OffsetDateTime resolveFromTime(
            MarketDataSubscription subscription,
            ImportHistoricalCandlesRequest request
    ) {
        if (request.fromTime() != null) {
            return request.fromTime();
        }

        return watermarkRepository.findByMarketDataSubscriptionId(subscription.getId())
                .map(watermark -> resolveFromWatermark(subscription, watermark))
                .orElseThrow(() -> new IllegalArgumentException(
                        "fromTime is required because no ingestion watermark exists for subscription id: "
                                + subscription.getId()
                ));
    }

    private OffsetDateTime resolveFromWatermark(
            MarketDataSubscription subscription,
            IngestionWatermark watermark
    ) {
        if (watermark.getLastSuccessfulCloseTime() != null) {
            return watermark.getLastSuccessfulCloseTime().plus(Duration.ofMillis(1));
        }

        if (watermark.getLastSuccessfulOpenTime() != null) {
            return watermark.getLastSuccessfulOpenTime()
                    .plusMinutes(subscription.getTimeframe().getDurationMinutes());
        }

        throw new IllegalArgumentException(
                "fromTime is required because ingestion watermark is empty for subscription id: "
                        + subscription.getId()
        );
    }

    private OffsetDateTime resolveToTime(ImportHistoricalCandlesRequest request) {
        if (request.toTime() != null) {
            return request.toTime();
        }

        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }

        if (limit < 1 || limit > 1000) {
            throw new IllegalArgumentException("Limit must be between 1 and 1000.");
        }

        return limit;
    }

    private void validateTimeRange(
            OffsetDateTime fromTime,
            OffsetDateTime toTime
    ) {
        if (!fromTime.isBefore(toTime)) {
            throw new IllegalArgumentException("fromTime must be before toTime.");
        }
    }

    private IngestionRun startRun(
            IngestionProfile profile,
            MarketDataSubscription subscription,
            OffsetDateTime fromTime,
            OffsetDateTime toTime,
            int limit,
            Map<String, Object> requestMetadata
    ) {
        Map<String, Object> metadata = new HashMap<>();

        if (requestMetadata != null) {
            metadata.putAll(requestMetadata);
        }

        metadata.put("trigger", "manual-historical-import");
        metadata.put("source", "BR-024");
        metadata.put("binanceLimit", limit);
        metadata.put("watermarkUpdated", false);

        IngestionRun run = IngestionRun.builder()
                .ingestionProfile(profile)
                .marketDataSubscription(subscription)
                .status(IngestionRunStatus.STARTED)
                .startedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .fromTime(fromTime)
                .toTime(toTime)
                .recordsRequested((long) limit)
                .recordsReceived(0L)
                .recordsInserted(0L)
                .recordsSkipped(0L)
                .recordsFailed(0L)
                .messagesReceived(0L)
                .messagesProcessed(0L)
                .messagesFailed(0L)
                .metadata(metadata)
                .build();

        return ingestionRunRepository.save(run);
    }

    private CandleImportStats persistCandles(
            List<BinanceKlineResponse> klines,
            MarketDataSubscription subscription,
            IngestionRun run
    ) {
        long inserted = 0L;
        long skipped = 0L;

        OffsetDateTime firstImportedOpenTime = null;
        OffsetDateTime lastImportedOpenTime = null;

        OffsetDateTime lastSuccessfulOpenTime = null;
        OffsetDateTime lastSuccessfulCloseTime = null;

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        for (BinanceKlineResponse kline : klines) {
            if (!isClosed(kline, now)) {
                skipped++;
                continue;
            }

            if (firstImportedOpenTime == null || kline.openTime().isBefore(firstImportedOpenTime)) {
                firstImportedOpenTime = kline.openTime();
            }

            if (lastImportedOpenTime == null || kline.openTime().isAfter(lastImportedOpenTime)) {
                lastImportedOpenTime = kline.openTime();
            }

            if (lastSuccessfulOpenTime == null || kline.openTime().isAfter(lastSuccessfulOpenTime)) {
                lastSuccessfulOpenTime = kline.openTime();
                lastSuccessfulCloseTime = kline.closeTime();
            }

            boolean alreadyExists = candleRepository.existsByTradingPairIdAndTimeframeIdAndOpenTime(
                    subscription.getTradingPair().getId(),
                    subscription.getTimeframe().getId(),
                    kline.openTime()
            );

            if (alreadyExists) {
                skipped++;
                continue;
            }

            Candle candle = Candle.builder()
                    .marketDataSubscription(subscription)
                    .tradingPair(subscription.getTradingPair())
                    .timeframe(subscription.getTimeframe())
                    .ingestionRun(run)
                    .openTime(kline.openTime())
                    .closeTime(kline.closeTime())
                    .openPrice(kline.openPrice())
                    .highPrice(kline.highPrice())
                    .lowPrice(kline.lowPrice())
                    .closePrice(kline.closePrice())
                    .volume(kline.volume())
                    .quoteVolume(kline.quoteVolume())
                    .numberOfTrades(kline.numberOfTrades())
                    .takerBuyBaseVolume(kline.takerBuyBaseVolume())
                    .takerBuyQuoteVolume(kline.takerBuyQuoteVolume())
                    .source(CandleSource.REST_HISTORICAL)
                    .closed(true)
                    .build();

            candleRepository.save(candle);
            inserted++;
        }

        return new CandleImportStats(
                inserted,
                skipped,
                0L,
                firstImportedOpenTime,
                lastImportedOpenTime,
                lastSuccessfulOpenTime,
                lastSuccessfulCloseTime
        );
    }

    private boolean isClosed(
            BinanceKlineResponse kline,
            OffsetDateTime now
    ) {
        return !kline.closeTime().isAfter(now);
    }

    private boolean completeRunAndUpdateWatermark(
            IngestionRun run,
            MarketDataSubscription subscription,
            int requestedLimit,
            int receivedCount,
            CandleImportStats stats
    ) {
        run.setStatus(IngestionRunStatus.SUCCESS);
        run.setFinishedAt(OffsetDateTime.now(ZoneOffset.UTC));

        run.setRecordsRequested((long) requestedLimit);
        run.setRecordsReceived((long) receivedCount);
        run.setRecordsInserted(stats.inserted());
        run.setRecordsSkipped(stats.skipped());
        run.setRecordsFailed(stats.failed());

        boolean watermarkUpdated = updateWatermarkAfterSuccess(
                subscription,
                run,
                stats
        );

        Map<String, Object> metadata = new HashMap<>();

        if (run.getMetadata() != null) {
            metadata.putAll(run.getMetadata());
        }

        metadata.put("firstImportedOpenTime", stats.firstImportedOpenTime());
        metadata.put("lastImportedOpenTime", stats.lastImportedOpenTime());
        metadata.put("lastSuccessfulOpenTime", stats.lastSuccessfulOpenTime());
        metadata.put("lastSuccessfulCloseTime", stats.lastSuccessfulCloseTime());
        metadata.put("watermarkUpdated", watermarkUpdated);

        run.setMetadata(metadata);

        ingestionRunRepository.save(run);

        return watermarkUpdated;
    }

    private boolean updateWatermarkAfterSuccess(
            MarketDataSubscription subscription,
            IngestionRun run,
            CandleImportStats stats
    ) {
        if (stats.lastSuccessfulOpenTime() == null || stats.lastSuccessfulCloseTime() == null) {
            return false;
        }

        IngestionWatermark watermark = watermarkRepository
                .findByMarketDataSubscriptionId(subscription.getId())
                .orElseGet(() -> IngestionWatermark.builder()
                        .marketDataSubscription(subscription)
                        .build()
                );

        watermark.setLastSuccessfulOpenTime(stats.lastSuccessfulOpenTime());
        watermark.setLastSuccessfulCloseTime(stats.lastSuccessfulCloseTime());
        watermark.setLastIngestionRunId(run.getId());

        watermarkRepository.save(watermark);

        return true;
    }

    private void failRun(
            IngestionRun run,
            Exception exception
    ) {
        run.setStatus(IngestionRunStatus.FAILED);
        run.setFinishedAt(OffsetDateTime.now(ZoneOffset.UTC));
        run.setErrorMessage(exception.getMessage());

        Map<String, Object> metadata = new HashMap<>();

        if (run.getMetadata() != null) {
            metadata.putAll(run.getMetadata());
        }

        metadata.put("errorType", exception.getClass().getSimpleName());
        metadata.put("watermarkUpdated", false);

        run.setMetadata(metadata);

        ingestionRunRepository.save(run);
    }

    private ImportHistoricalCandlesResponse toResponse(
            IngestionRun run,
            MarketDataSubscription subscription,
            OffsetDateTime firstImportedOpenTime,
            OffsetDateTime lastImportedOpenTime,
            boolean watermarkUpdated,
            String message
    ) {
        return new ImportHistoricalCandlesResponse(
                run.getId(),
                run.getBatchId(),
                run.getStatus(),

                subscription.getId(),
                subscription.getTradingPair().getSymbol(),
                subscription.getTimeframe().getCode(),

                run.getFromTime(),
                run.getToTime(),

                run.getRecordsRequested(),
                run.getRecordsReceived(),
                run.getRecordsInserted(),
                run.getRecordsSkipped(),
                run.getRecordsFailed(),

                firstImportedOpenTime,
                lastImportedOpenTime,

                watermarkUpdated,

                message,

                run.getStartedAt(),
                run.getFinishedAt()
        );
    }

    private record CandleImportStats(
            Long inserted,
            Long skipped,
            Long failed,
            OffsetDateTime firstImportedOpenTime,
            OffsetDateTime lastImportedOpenTime,
            OffsetDateTime lastSuccessfulOpenTime,
            OffsetDateTime lastSuccessfulCloseTime
    ) {
    }
}