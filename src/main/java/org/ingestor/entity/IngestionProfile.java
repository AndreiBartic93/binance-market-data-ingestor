package org.ingestor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ingestor.entity.enums.IngestionMethod;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "ingestion_profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ingestion_profiles_name",
                        columnNames = "name"
                )
        }
)
public class IngestionProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Examples:
     * - Binance REST Historical Import
     * - Binance Scheduled 4h Sync
     * - Binance WebSocket Live 1m
     */
    @Column(name = "name", nullable = false, length = 150, unique = true)
    private String name;

    /**
     * Defines how this profile collects data.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ingestion_method", nullable = false, length = 50)
    private IngestionMethod ingestionMethod;

    /**
     * Optional explanation of what this profile is used for.
     */
    @Column(name = "description")
    private String description;

    /**
     * Defines if this profile persists market data.
     *
     * REST_HISTORICAL -> true
     * SCHEDULED_SYNC  -> true
     * WEBSOCKET_LIVE  -> false
     */
    @Column(name = "stores_market_data", nullable = false)
    private boolean storesMarketData;

    /**
     * Allows disabling a profile without deleting it.
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
        this.storesMarketData = resolveStoresMarketData(this.ingestionMethod);
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
        this.storesMarketData = resolveStoresMarketData(this.ingestionMethod);
    }

    private boolean resolveStoresMarketData(IngestionMethod ingestionMethod) {
        return ingestionMethod != IngestionMethod.WEBSOCKET_LIVE;
    }
}
