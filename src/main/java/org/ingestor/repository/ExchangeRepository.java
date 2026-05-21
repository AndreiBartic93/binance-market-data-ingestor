package org.ingestor.repository;

import org.ingestor.entity.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

    Optional<Exchange> findByCode(String code);
    boolean existsByCode(String code);
}
