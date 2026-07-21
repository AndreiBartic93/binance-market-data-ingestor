package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.ingestor.client.BinanceMarketDataClient;
import org.ingestor.client.ScheduledIngestionProperties;
import org.ingestor.dto.binance.BinanceKlineResponse;
import org.ingestor.dto.scheduledsync.ScheduledIngestionSyncResponse;
import org.ingestor.dto.scheduledsync.ScheduledSubscriptionSyncResult;
import org.ingestor.entity.*;
import org.ingestor.entity.enums.CandleSource;
import org.ingestor.entity.enums.IngestionMethod;
import org.ingestor.entity.enums.IngestionRunStatus;
import org.ingestor.repository.CandleRepository;
import org.ingestor.repository.IngestionProfileSubscriptionRepository;
import org.ingestor.repository.IngestionRunRepository;
import org.ingestor.repository.IngestionWatermarkRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduledIngestionSyncService {

    private final BinanceMarketDataClient binanceMarketDataClient;
    private final ScheduledIngestionProperties scheduledIngestionProperties;

    private final IngestionProfileSubscriptionRepository profileSubscriptionRepository;
    private final IngestionWatermarkRepository watermarkRepository;
    private final IngestionRunRepository ingestionRunRepository;
    private final CandleRepository candleRepository;

    public ScheduledIngestionSyncResponse syncAllScheduledSubscriptions(String trigger) {
        OffsetDateTime startedAt = OffsetDateTime.now(ZoneOffset.UTC);
        String batchId = UUID.randomUUID().toString();

        List<IngestionProfileSubscription> links = profileSubscriptionRepository
                .findActiveScheduledLinks(IngestionMethod.SCHEDULED_SYNC);

        List<ScheduledSubscriptionSyncResult> results = new ArrayList<>();

        links.stream()
                .limit(scheduledIngestionProperties.getMaxSubscriptionsPerRun())
                .forEach(link -> results.add(syncOneLink(link, batchId, trigger)));

        OffsetDateTime finishedAt = OffsetDateTime.now(ZoneOffset.UTC);

        int successCount = (int) results.stream()
                .filter(result -> result.status() == IngestionRunStatus.SUCCESS)
                .count();

        int failedCount = (int) results.stream()
                .filter(result -> result.status() == IngestionRunStatus.FAILED)
                .count();

        int skippedCount = (int) results.stream()
                .filter(result -> result.status() == IngestionRunStatus.STOPPED)
                .count();

        return new ScheduledIngestionSyncResponse(
                batchId,
                trigger,
                startedAt,
                finishedAt,
                links.size(),
                results.size(),
                successCount,
                failedCount,
                skippedCount,
                results
        );
    }

    private ScheduledSubscriptionSyncResult syncOneLink(
            IngestionProfileSubscription link,
            String batchId,
            String trigger
    ) {
        IngestionProfile profile = link.getIngestionProfile();
        MarketDataSubscription subscription = link.getMarketDataSubscription();

        OffsetDateTime toTime = OffsetDateTime.now(ZoneOffset.UTC);

        OffsetDateTime fromTime;
        try {
            fromTime = resolveFromTime(subscription);
        } catch (IllegalArgumentException exception) {
            return skippedResult(
                    link,
                    batchId,
                    subscription,
                    toTime,
                    exception.getMessage()
            );
        }

        if (!fromTime.isBefore(toTime)) {
            return skippedResult(
                    link,
                    batchId,
                    subscription,
                    toTime,
                    "No sync needed. fromTime is not before toTime."
            );
        }

        int limit = scheduledIngestionProperties.getLimit();

        IngestionRun run = startRun(
                profile,
                subscription,
                batchId,
                trigger,
                fromTime,
                toTime,
                limit
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
                    ? "Scheduled sync finished and watermark was updated."
                    : "Scheduled sync finished, but no closed candle was processed.";

            return toResult(
                    link,
                    run,
                    subscription,
                    stats,
                    watermarkUpdated,
                    message
            );

        } catch (Exception exception) {
            failRun(run, exception);

            return toResult(
                    link,
                    run,
                    subscription,
                    CandleImportStats.empty(),
                    false,
                    "Scheduled sync failed: " + exception.getMessage()
            );
        }
    }

    private OffsetDateTime resolveFromTime(MarketDataSubscription subscription) {
        IngestionWatermark watermark = watermarkRepository
                .findByMarketDataSubscriptionId(subscription.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No watermark exists for subscription id: " + subscription.getId()
                ));

        if (watermark.getLastSuccessfulCloseTime() != null) {
            return watermark.getLastSuccessfulCloseTime().plus(Duration.ofMillis(1));
        }

        if (watermark.getLastSuccessfulOpenTime() != null) {
            return watermark.getLastSuccessfulOpenTime()
                    .plusMinutes(subscription.getTimeframe().getDurationMinutes());
        }

        throw new IllegalArgumentException(
                "Watermark is empty for subscription id: "
                        + subscription.getId()
                        + ". Run a historical import first."
        );
    }

    private IngestionRun startRun(
            IngestionProfile profile,
            MarketDataSubscription subscription,
            String batchId,
            String trigger,
            OffsetDateTime fromTime,
            OffsetDateTime toTime,
            int limit
    ) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("trigger", trigger);
        metadata.put("source", "BR-025");
        metadata.put("binanceLimit", limit);
        metadata.put("watermarkUpdated", false);

        IngestionRun run = IngestionRun.builder()
                .ingestionProfile(profile)
                .marketDataSubscription(subscription)
                .batchId(batchId)
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
        long failed = 0L;

        OffsetDateTime lastSuccessfulOpenTime = null;
        OffsetDateTime lastSuccessfulCloseTime = null;

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        for (BinanceKlineResponse kline : klines) {
            if (!isClosed(kline, now)) {
                skipped++;
                continue;
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

            try {
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
                        .source(CandleSource.SCHEDULED_SYNC)
                        .closed(true)
                        .build();

                candleRepository.save(candle);
                inserted++;

            } catch (DataIntegrityViolationException exception) {
                skipped++;
            } catch (Exception exception) {
                failed++;
            }
        }

        return new CandleImportStats(
                inserted,
                skipped,
                failed,
                lastSuccessfulOpenTime,
                lastSuccessfulCloseTime
        );
    }

    private boolean completeRunAndUpdateWatermark(
            IngestionRun run,
            MarketDataSubscription subscription,
            int requestedLimit,
            int receivedCount,
            CandleImportStats stats
    ) {
        IngestionRunStatus finalStatus = stats.failed() > 0
                ? IngestionRunStatus.PARTIAL
                : IngestionRunStatus.SUCCESS;

        run.setStatus(finalStatus);
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
                        .build());

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

    private boolean isClosed(
            BinanceKlineResponse kline,
            OffsetDateTime now
    ) {
        return !kline.closeTime().isAfter(now);
    }

    private ScheduledSubscriptionSyncResult skippedResult(
            IngestionProfileSubscription link,
            String batchId,
            MarketDataSubscription subscription,
            OffsetDateTime toTime,
            String message
    ) {
        return new ScheduledSubscriptionSyncResult(
                link.getId(),

                null,
                batchId,
                IngestionRunStatus.STOPPED,

                subscription.getId(),
                subscription.getTradingPair().getSymbol(),
                subscription.getTimeframe().getCode(),

                null,
                toTime,

                0L,
                0L,
                0L,
                0L,
                0L,

                null,
                null,

                false,

                message
        );
    }

    private ScheduledSubscriptionSyncResult toResult(
            IngestionProfileSubscription link,
            IngestionRun run,
            MarketDataSubscription subscription,
            CandleImportStats stats,
            boolean watermarkUpdated,
            String message
    ) {
        return new ScheduledSubscriptionSyncResult(
                link.getId(),

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

                stats.lastSuccessfulOpenTime(),
                stats.lastSuccessfulCloseTime(),

                watermarkUpdated,

                message
        );
    }

    private record CandleImportStats(
            Long inserted,
            Long skipped,
            Long failed,
            OffsetDateTime lastSuccessfulOpenTime,
            OffsetDateTime lastSuccessfulCloseTime
    ) {

        private static CandleImportStats empty() {
            return new CandleImportStats(
                    0L,
                    0L,
                    0L,
                    null,
                    null
            );
        }
    }
}