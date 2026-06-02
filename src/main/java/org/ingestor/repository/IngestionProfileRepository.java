package org.ingestor.repository;

import org.ingestor.entity.IngestionProfile;
import org.ingestor.entity.enums.IngestionMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngestionProfileRepository extends JpaRepository<IngestionProfile, Long> {

    Optional<IngestionProfile> findByName(String name);

    boolean existsByName(String name);

    List<IngestionProfile> findByIngestionMethod(IngestionMethod ingestionMethod);

    List<IngestionProfile> findByActiveTrue();

}
