package org.ingestor.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ingestor.client.ScheduledIngestionProperties;
import org.ingestor.service.ScheduledIngestionSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledIngestionScheduler {

    private final ScheduledIngestionProperties properties;
    private final ScheduledIngestionSyncService scheduledIngestionSyncService;

    @Scheduled(
            initialDelayString = "${ingestion.scheduled.initial-delay-ms:30000}",
            fixedDelayString = "${ingestion.scheduled.fixed-delay-ms:300000}"
    )
    public void runScheduledSync() {
        if (!properties.isEnabled()) {
            log.debug("Scheduled ingestion sync is disabled.");
            return;
        }

        log.info("Starting scheduled ingestion sync.");

        var response = scheduledIngestionSyncService.syncAllScheduledSubscriptions("scheduler");

        log.info(
                "Scheduled ingestion sync finished. batchId={}, linksFound={}, processed={}, success={}, failed={}, skipped={}",
                response.batchId(),
                response.linksFound(),
                response.processedCount(),
                response.successCount(),
                response.failedCount(),
                response.skippedCount()
        );
    }
}