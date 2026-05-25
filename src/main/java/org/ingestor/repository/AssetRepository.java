package org.ingestor.repository;

import org.ingestor.entity.Asset;
import org.ingestor.entity.enums.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findBySymbol(String symbol);

    boolean existsBySymbol(String symbol);

    List<Asset> findByAssetType(AssetType assetType);

    List<Asset> findByActiveTrue();
}
