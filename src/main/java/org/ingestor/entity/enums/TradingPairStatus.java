package org.ingestor.entity.enums;

/**
 * Represents the status of a trading pair on the exchange.
 *
 * ACTIVE   -> Pair can be used for ingestion.
 * INACTIVE -> Pair exists but should not be used.
 * DELISTED -> Pair was removed from the exchange.
 */
public enum TradingPairStatus {
    ACTIVE,
    INACTIVE,
    DELISTED
}
