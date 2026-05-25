package org.ingestor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ingestor.entity.enums.TradingPairStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "trading_pairs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trading_pairs_exchange_symbol",
                        columnNames = {"exchange_id", "symbol"}
                )
        }
)
public class TradingPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Exchange where this trading pair exists.
     * Example: Binance
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exchange_id", nullable = false)
    private Exchange exchange;

    /**
     * Base asset of the pair.
     * Example: BTC in BTCUSDT.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "base_asset_id", nullable = false)
    private Asset baseAsset;

    /**
     * Quote asset of the pair.
     * Example: USDT in BTCUSDT
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_asset_id", nullable = false)
    private Asset quoteAsset;

    @Column(name = "symbol", nullable = false, length = 30)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TradingPairStatus status;

    @Column(name = "price_precision")
    private Integer pricePrecision;

    @Column(name = "quantity_precision")
    private Integer qualityPrecision;

    @Column(name = "min_quantity", precision = 38, scale = 18)
    private BigDecimal minQuantity;

    @Column(name = "max_quantity", precision = 38, scale = 18)
    private BigDecimal maxQuantity;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
        if (this.status == null) {
            this.status = TradingPairStatus.ACTIVE;
        }
        this.active = true;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
