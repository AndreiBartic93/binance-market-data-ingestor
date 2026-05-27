package org.ingestor.repository;

import org.ingestor.entity.Timeframe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimeframeRepository extends JpaRepository<Timeframe, Long> {

    Optional<Timeframe> findByCode(String code);

    boolean existsByCode(String code);

    List<Timeframe> findByActiveTrue();

    List<Timeframe> findAllByOrderByDurationMinutesAsc();
}
