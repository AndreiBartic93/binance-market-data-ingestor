package org.ingestor.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ingestion.scheduled")
public class ScheduledIngestionProperties {

    /**
     * Enables/disables scheduled ingestion.
     */
    private boolean enabled = true;

    /**
     * Delay between two scheduler executions.
     * Default: 5 minutes.
     */
    private long fixedDelayMs = 300000;

    /**
     * Delay after application startup before first execution.
     * Default: 30 seconds.
     */
    private long initialDelayMs = 30000;

    /**
     * Binance max limit is 1000.
     */
    private int limit = 1000;

    /**
     * Safety limit to avoid processing too many subscriptions in one scheduler tick.
     */
    private int maxSubscriptionsPerRun = 50;
}