package org.ingestor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ingestor.entity.enums.AssetType;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "assets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_assets_symbol", columnNames = "symbol")
        }
)
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     *  Asset symbol used by Binance.
     *  Ex: BTC, ETH, USDT, SOL
     */
    @Column(name = "symbol", nullable = false, length = 20, unique = true)
    private String symbol;

    /**
     *  Human-readable asset name.
     *  Examples: Bitcoin, Ethereum, Tether.
     */
    @Column(name = "name", length = 100)
    private String name;

    /**
     * Asset category.
     * Stored as text in DB: CRYPTO, STABLECOIN, FIAT.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 50)
    private AssetType assetType;

    /**
     * Allows disabling an asset without deleting historical references.
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
