package org.ingestor.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ingestor.dto.asset.AssetResponse;
import org.ingestor.dto.asset.CreateAssetRequest;
import org.ingestor.dto.asset.UpdateAssetRequest;
import org.ingestor.entity.enums.AssetType;
import org.ingestor.service.AssetService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse create(@Valid @RequestBody CreateAssetRequest createAssetRequest) {
        return assetService.create(createAssetRequest);
    }

    @GetMapping
    public List<AssetResponse> findAll(
            @RequestParam(required = false) AssetType assetType,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly
            ) {
        return assetService.findAll(assetType, activeOnly);
    }

    @GetMapping("/{id}")
    public AssetResponse findById(@PathVariable Long id) {
        return assetService.findById(id);
    }

    @GetMapping("/symbol/{symbol}")
    public AssetResponse findBySymbol(@PathVariable String symbol) {
        return assetService.findBySymbol(symbol);
    }

    @PatchMapping("/{id}")
    public AssetResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssetRequest updateAssetRequest) {
        return assetService.update(id, updateAssetRequest);
    }

    @PatchMapping("/{id}/activate")
    public AssetResponse activate(@PathVariable Long id) {
        return assetService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public AssetResponse deactivate(@PathVariable Long id) {
        return assetService.deactivate(id);
    }
}
