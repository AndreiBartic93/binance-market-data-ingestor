package org.ingestor.controller;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.scheduledsync.ScheduledIngestionSyncResponse;
import org.ingestor.service.ScheduledIngestionSyncService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingestion/scheduled-sync")
@RequiredArgsConstructor
public class ScheduledIngestionSyncController {

    private final ScheduledIngestionSyncService scheduledIngestionSyncService;

    @PostMapping("/run")
    public ScheduledIngestionSyncResponse runManually() {
        return scheduledIngestionSyncService.syncAllScheduledSubscriptions("manual");
    }
}
