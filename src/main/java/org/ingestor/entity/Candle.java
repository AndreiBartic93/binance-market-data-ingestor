package org.ingestor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ingestor.entity.enums.CandleSource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "candles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_candles_pair_timeframe_open_time",
                        columnNames = {"trading_pair_id", "timeframe_id", "open_time"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_candles_subscription_open_time",
                        columnList = "market_data_subscription_id, open_time"
                ),
                @Index(
                        name = "idx_candles_open_time",
                        columnList = "open_time"
                )
        }
)
public class Candle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Business subscription this candle belongs to.
     * Example: BTCUSDT + 4h.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_data_subscription_id", nullable = false)
    private MarketDataSubscription marketDataSubscription;

    /**
     * Trading pair stored redundantly for easier querying.
     * Example: BTCUSDT.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trading_pair_id", nullable = false)
    private TradingPair tradingPair;

    /**
     * Timeframe stored redundantly for easier querying.
     * Example: 1m, 1h, 4h.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "timeframe_id", nullable = false)
    private Timeframe timeframe;

    /**
     * Ingestion run that inserted this candle.
     * Nullable because some candles may be inserted before run tracking
     * or by maintenance scripts.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingestion_run_id")
    private IngestionRun ingestionRun;

    @Column(name = "open_time", nullable = false)
    private OffsetDateTime openTime;

    @Column(name = "close_time", nullable = false)
    private OffsetDateTime closeTime;

    @Column(name = "open_price", nullable = false, precision = 38, scale = 18)
    private BigDecimal openPrice;

    @Column(name = "high_price", nullable = false, precision = 38, scale = 18)
    private BigDecimal highPrice;

    @Column(name = "low_price", nullable = false, precision = 38, scale = 18)
    private BigDecimal lowPrice;

    @Column(name = "close_price", nullable = false, precision = 38, scale = 18)
    private BigDecimal closePrice;

    @Column(name = "volume", nullable = false, precision = 38, scale = 18)
    private BigDecimal volume;

    @Column(name = "quote_volume", precision = 38, scale = 18)
    private BigDecimal quoteVolume;

    @Column(name = "number_of_trades")
    private Long numberOfTrades;

    @Column(name = "taker_buy_base_volume", precision = 38, scale = 18)
    private BigDecimal takerBuyBaseVolume;

    @Column(name = "taker_buy_quote_volume", precision = 38, scale = 18)
    private BigDecimal takerBuyQuoteVolume;

    /**
     * Source that persisted this candle.
     * Allowed values: REST_HISTORICAL, SCHEDULED_SYNC.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private CandleSource source;

    /**
     * Binance can return an open candle for the current interval.
     * For persisted historical/scheduled data we usually store closed candles.
     */
    @Column(name = "is_closed", nullable = false)
    private boolean closed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();

        if (!this.closed) {
            this.closed = true;
        }
    }
}
