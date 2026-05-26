package org.ingestor.repository;


import org.ingestor.entity.TradingPair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TradingPairRepository extends JpaRepository<TradingPair, Long> {
    Optional<TradingPair> findByExchangeIdAndSymbol(Long exchangeId, String symbol);

    boolean existsByExchangeIdAndSymbol(Long exchangeId, String symbol);

    List<TradingPair> findByExchangeId(Long exchangeId);

    List<TradingPair> findByActiveTrue();
}
