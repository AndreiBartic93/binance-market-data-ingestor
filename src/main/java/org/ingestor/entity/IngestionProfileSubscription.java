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
        name = "ingestion_profile_subscriptions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ingestion_profile_subscriptions_profile_subscription",
                        columnNames = {"ingestion_profile_id", "market_data_subscription_id"}
                )
        }
)
public class IngestionProfileSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Defines how the data is collected.
     * Example: REST_HISTORICAL, SCHEDULED_SYNC, WEBSOCKET_LIVE.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingestion_profile_id", nullable = false)
    private IngestionProfile ingestionProfile;

    /**
     * Defines what data is collected.
     * Ex: BTCUSDT + 4H
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_data_subscription_id", nullable = false)
    private MarketDataSubscription marketDataSubscription;

    /**
     * Allows disabling this link without deleting it.
     */
    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.active = true;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

}
