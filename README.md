Binance Market Data Ingestion is a backend service designed to provide a simple, controlled, and extensible mechanism for collecting, ingesting and storing market data from Binance.

The application focuses on retrieving historical and current candlestick data through the Binance REST API, storing it in a relational database without duplicates, and tracking the ingestion process through execution runs, statuses, and watermarks.

For live data, the service can consume Binance WebSocket streams, but it does not persist raw live market data in the database.

This service does not execute trades, generate trading signals, or perform backtesting. Its responsibility is strictly limited to market data ingestion, synchronization, and storage.

Implementation steps:

BR-001 Initialize backend ingestion service
BR-002 Add Docker support for local development
BR-003 Add database migration support

BR-004 Create exchange catalog
BR-005 Create asset catalog
BR-006 Create trading pair catalog
BR-007 Create timeframe catalog
BR-008 Seed Binance market reference dataw

BR-009 Allow market data subscriptions
BR-011 Add ingestion watermark for each subscription

BR-012 Create ingestion profiles
BR-014 Link profiles to market subscriptions

BR-015 Track ingestion executions
BR-018 Create candle storage
BR-019 Prevent duplicate candles

BR-021 Add Binance REST client
BR-022 Import historical candles by subscription
BR-024 Update watermark after successful import