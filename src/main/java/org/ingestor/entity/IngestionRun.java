package org.ingestor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.ingestor.entity.enums.IngestionRunStatus;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ingestion_runs")
public class IngestionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Defines how data was collected.
     * Example: REST_HISTORICAL, SCHEDULED_SYNC, WEBSOCKET_LIVE.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingestion_profile_id", nullable = false)
    private IngestionProfile ingestionProfile;

    /**
     * Defines what data was collected.
     * Example: BTCUSDT + 4h.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_data_subscription_id", nullable = false)
    private MarketDataSubscription marketDataSubscription;

    /**
     * Groups multiple runs that were started together.
     * Example:
     * one profile execution can process BTCUSDT, ETHUSDT and SOLUSDT.
     * Each subscription has its own run, but all share the same batchId.
     */
    @Column(name = "batch_id", length = 100)
    private String batchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private IngestionRunStatus status;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "from_time")
    private OffsetDateTime fromTime;

    @Column(name = "to_time")
    private OffsetDateTime toTime;

    @Column(name = "records_requested", nullable = false)
    private Long recordsRequested;

    @Column(name = "records_received", nullable = false)
    private Long recordsReceived;

    @Column(name = "records_inserted", nullable = false)
    private Long recordsInserted;

    @Column(name = "records_skipped", nullable = false)
    private Long recordsSkipped;

    @Column(name = "records_failed", nullable = false)
    private Long recordsFailed;

    @Column(name = "messages_received", nullable = false)
    private Long messagesReceived;

    @Column(name = "messages_processed", nullable = false)
    private Long messagesProcessed;

    @Column(name = "messages_failed", nullable = false)
    private Long messagesFailed;

    @Column(name = "error_message")
    private String errorMessage;

    /**
     * Flexible execution details.
     * Example:
     * {
     *   "source": "manual",
     *   "limit": 1000,
     *   "requestDelayMs": 250
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();

        if (this.startedAt == null) {
            this.startedAt = OffsetDateTime.now();
        }

        if (this.status == null) {
            this.status = IngestionRunStatus.STARTED;
        }

        if (this.batchId == null || this.batchId.isBlank()) {
            this.batchId = UUID.randomUUID().toString();
        }

        this.recordsRequested = zeroIfNull(this.recordsRequested);
        this.recordsReceived = zeroIfNull(this.recordsReceived);
        this.recordsInserted = zeroIfNull(this.recordsInserted);
        this.recordsSkipped = zeroIfNull(this.recordsSkipped);
        this.recordsFailed = zeroIfNull(this.recordsFailed);

        this.messagesReceived = zeroIfNull(this.messagesReceived);
        this.messagesProcessed = zeroIfNull(this.messagesProcessed);
        this.messagesFailed = zeroIfNull(this.messagesFailed);
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    private Long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }
}
