package org.ingestor.dto.scheduledsync;

import java.time.OffsetDateTime;
import java.util.List;

public record ScheduledIngestionSyncResponse(
        String batchId,
        String trigger,

        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,

        int linksFound,
        int processedCount,
        int successCount,
        int failedCount,
        int skippedCount,

        List<ScheduledSubscriptionSyncResult> results
) {
}