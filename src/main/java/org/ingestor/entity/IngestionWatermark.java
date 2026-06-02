package org.ingestor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "ingestion_watermarks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ingestion_watermarks_subscription",
                        columnNames = "market_data_subscription_id"
                )
        }
)
public class IngestionWatermark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one relation:
     * one market data subscription has one ingestion watermark.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_data_subscription_id", nullable = false)
    private MarketDataSubscription marketDataSubscription;

    /**
     * Last successfully ingested candle open time.
     */
    @Column(name = "last_successful_open_time")
    private OffsetDateTime lastSuccessfulOpenTime;

    /**
     * Last successfully ingested candle close time.
     */
    @Column(name = "last_successful_close_time")
    private OffsetDateTime lastSuccessfulCloseTime;

    /**
     * Will later reference ingestion_runs.id.
     * For now it stays as a nullable Long because ingestion_runs does not exist yet.
     */
    @Column(name = "last_ingestion_run_id")
    private Long lastIngestionRunId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
