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
        name = "market_data_subscriptions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_data_subscriptions_pair_timeframe",
                        columnNames = {"trading_pair_id", "timeframe_id"}
                )
        }
)
public class MarketDataSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Trading pair selected for ingestion.
     * Example: BTCUSDT.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trading_pair_id", nullable = false)
    private TradingPair tradingPair;

    /**
     * Timeframe selected for ingestion.
     * Example: 1m, 1h, 4h.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "timeframe_id", nullable = false)
    private Timeframe timeframe;

    /**
     * Enables REST historical ingestion for this pair + timeframe.
     */
    @Column(name = "collect_historical", nullable = false)
    private boolean collectHistorical;

    /**
     * Enables scheduled synchronization for this pair + timeframe.
     */
    @Column(name = "collect_scheduled", nullable = false)
    private boolean collectScheduled;

    /**
     * Enables live WebSocket processing for this pair + timeframe.
     * Live data will not be persisted in the candles table.
     */
    @Column(name = "collect_live", nullable = false)
    private boolean collectLive;

    /**
     * Binance WebSocket stream name.
     * Example: btcusdt@kline_1m.
     */
    @Column(name = "websocket_stream_name", length = 150)
    private String websocketStreamName;

    /**
     * Allows disabling a subscription without deleting it.
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

        if (!this.collectHistorical && !this.collectScheduled && !this.collectLive) {
            this.collectHistorical = true;
            this.collectScheduled = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
