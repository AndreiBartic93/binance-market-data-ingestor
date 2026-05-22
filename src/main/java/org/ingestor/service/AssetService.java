package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.sql.Update;
import org.ingestor.dto.asset.AssetResponse;
import org.ingestor.dto.asset.CreateAssetRequest;
import org.ingestor.dto.asset.UpdateAssetRequest;
import org.ingestor.entity.Asset;
import org.ingestor.entity.AssetType;
import org.ingestor.exception.ResourceNotFoundException;
import org.ingestor.repository.AssetRepository;
import org.ingestor.util.UpdateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    @Transactional
    public AssetResponse create(CreateAssetRequest createAssetRequest) {
        Asset asset = Asset.builder()
                .symbol(normalizeSymbol(createAssetRequest.symbol()))
                .name(createAssetRequest.name())
                .assetType(createAssetRequest.assetType())
                .active(true)
                .build();

        Asset savedAsset = assetRepository.save(asset);
        return toResponse(savedAsset);
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> findAll(AssetType assetType, Boolean activeOnly) {
        List<Asset> assets;

        if (Boolean.TRUE.equals(activeOnly)) {
            assets = assetRepository.findByActiveTrue();
        } else if (assetType != null) {
            assets = assetRepository.findByAssetType(assetType);
        } else {
            assets = assetRepository.findAll();
        }

        return assets.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssetResponse findById(Long id) {
        Asset asset = getAssetById(id);
        return toResponse(asset);
    }

    @Transactional(readOnly = true)
    public AssetResponse findBySymbol(String symbol) {
        Asset asset = assetRepository.findBySymbol(normalizeSymbol(symbol))
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with symbol: " + symbol));

        return toResponse(asset);
    }

    @Transactional
    public AssetResponse update(Long id, UpdateAssetRequest updateAssetRequest) {
        Asset asset = getAssetById(id);

        UpdateUtils.setIfNotNull(updateAssetRequest.name(), asset::setName);
        UpdateUtils.setIfNotNull(updateAssetRequest.assetType(), asset::setAssetType);
        UpdateUtils.setIfNotNull(updateAssetRequest.active(), asset::setActive);

        Asset savedAsset = assetRepository.save(asset);
        return toResponse(savedAsset);
    }

    @Transactional
    public AssetResponse activate(Long id) {
        Asset asset = getAssetById(id);
        asset.setActive(true);

        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public AssetResponse deactivate(Long id) {
        Asset asset = getAssetById(id);
        asset.setActive(false);

        return toResponse(assetRepository.save(asset));
    }

    private Asset getAssetById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));
    }


    private AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getSymbol(),
                asset.getName(),
                asset.getAssetType(),
                asset.isActive(),
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }

    private String normalizeSymbol(String symbol) {
        return symbol.trim().toUpperCase();
    }
}
