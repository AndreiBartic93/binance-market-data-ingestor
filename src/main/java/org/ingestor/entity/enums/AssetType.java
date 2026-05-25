package org.ingestor.entity.enums;

/**
 * Describes the type of asset stored in the catalog.
 *
 * CRYPTO      -> BTC, ETH, SOL, BNB
 * STABLECOIN  -> USDT, USDC, FDUSD
 * FIAT        -> EUR, USD, GBP
 */
public enum AssetType {
    CRYPTO,
    STABLECOIN,
    FIAT
}