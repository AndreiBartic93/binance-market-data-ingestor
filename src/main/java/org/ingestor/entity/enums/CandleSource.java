package org.ingestor.entity.enums;

/**
 * Defines which ingestion method persisted the candle.
 *
 * WEBSOCKET_LIVE is intentionally not present here because
 * live WebSocket data is not persisted in the candles table.
 */
public enum CandleSource {
    REST_HISTORICAL,
    SCHEDULED_SYNC
}
