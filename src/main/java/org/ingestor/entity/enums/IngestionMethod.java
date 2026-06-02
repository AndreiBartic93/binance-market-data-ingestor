package org.ingestor.entity.enums;

/**
 * Defines how market data is collected.
 *
 * REST_HISTORICAL:
 * Used for importing historical candles through Binance REST API.
 * Stores market data in the database.
 *
 * SCHEDULED_SYNC:
 * Used for periodically syncing new candles.
 * Stores market data in the database.
 *
 * WEBSOCKET_LIVE:
 * Used for processing live Binance WebSocket messages.
 * Does not store raw market data in the database.
 */
public enum IngestionMethod {
    REST_HISTORICAL,
    SCHEDULED_SYNC,
    WEBSOCKET_LIVE
}
