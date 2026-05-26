package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.tradingpair.CreateTradingPairRequest;
import org.ingestor.dto.tradingpair.TradingPairResponse;
import org.ingestor.dto.tradingpair.UpdateTradingPairRequest;
import org.ingestor.entity.Asset;
import org.ingestor.entity.Exchange;
import org.ingestor.entity.TradingPair;
import org.ingestor.entity.enums.TradingPairStatus;
import org.ingestor.exception.ResourceNotFoundException;
import org.ingestor.repository.AssetRepository;
import org.ingestor.repository.ExchangeRepository;
import org.ingestor.repository.TradingPairRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradingPairService {

    private final TradingPairRepository tradingPairRepository;
    private final ExchangeRepository exchangeRepository;
    private final AssetRepository assetRepository;

    @Transactional
    public TradingPairResponse create(CreateTradingPairRequest request) {

        Exchange exchange = exchangeRepository.findById(request.exchangeId())
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found with id: " + request.exchangeId()));

        Asset baseAsset = assetRepository.findById(request.baseAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Base asset not found with id: " + request.baseAssetId()));

        Asset quoteAsset = assetRepository.findById(request.quoteAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Quote asset not found with id: " + request.quoteAssetId()));

        if (baseAsset.getId().equals(quoteAsset.getId())) {
            throw new DataIntegrityViolationException("Base asset and quote asset must be different.");
        }

        String normalizedSymbol = normalizeSymbol(request.symbol());

        if (tradingPairRepository.existsByExchangeIdAndSymbol(exchange.getId(), normalizedSymbol)) {
            throw new DataIntegrityViolationException("Trading pair already exists for exchange and symbol.");
        }

        TradingPair tradingPair = TradingPair.builder()
                .exchange(exchange)
                .baseAsset(baseAsset)
                .quoteAsset(quoteAsset)
                .symbol(normalizedSymbol)
                .status(TradingPairStatus.ACTIVE)
                .pricePrecision(request.pricePrecision())
                .quantityPrecision(request.quantityPrecision())
                .minQuantity(request.minQuantity())
                .maxQuantity(request.maxQuantity())
                .active(true)
                .build();

        TradingPair savedTradingPair = tradingPairRepository.save(tradingPair);

        return toResponse(savedTradingPair);
    }

    @Transactional(readOnly = true)
    public List<TradingPairResponse> findAll(Long exchangeId, boolean activeOnly) {
        List<TradingPair> tradingPairs;

        if (Boolean.TRUE.equals(activeOnly)) {
            tradingPairs = tradingPairRepository.findByActiveTrue();
        } else if (exchangeId != null) {
            tradingPairs = tradingPairRepository.findByExchangeId(exchangeId);
        } else {
            tradingPairs = tradingPairRepository.findAll();
        }

        return tradingPairs.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TradingPairResponse findById(Long id) {
        TradingPair tradingPair = getTradingPairById(id);
        return toResponse(tradingPair);
    }

    @Transactional(readOnly = true)
    public TradingPairResponse findByExchangeAndSymbol(Long exchangeId, String symbol) {
        TradingPair tradingPair = tradingPairRepository
                .findByExchangeIdAndSymbol(exchangeId, normalizeSymbol(symbol))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trading pair not found with exchange id " + exchangeId + " and symbol: " + symbol
                ));

        return toResponse(tradingPair);
    }

    @Transactional
    public TradingPairResponse update(Long id, UpdateTradingPairRequest request) {
        TradingPair tradingPair = getTradingPairById(id);

        if (request.status() != null) {
            tradingPair.setStatus(request.status());
        }

        if (request.pricePrecision() != null) {
            tradingPair.setPricePrecision(request.pricePrecision());
        }

        if (request.quantityPrecision() != null) {
            tradingPair.setQuantityPrecision(request.quantityPrecision());
        }

        if (request.minQuantity() != null) {
            tradingPair.setMinQuantity(request.minQuantity());
        }

        if (request.maxQuantity() != null) {
            tradingPair.setMaxQuantity(request.maxQuantity());
        }

        if (request.active() != null) {
            tradingPair.setActive(request.active());
        }

        TradingPair savedTradingPair = tradingPairRepository.save(tradingPair);

        return toResponse(savedTradingPair);
    }

    @Transactional
    public TradingPairResponse activate(Long id) {
        TradingPair tradingPair = getTradingPairById(id);
        tradingPair.setActive(true);
        tradingPair.setStatus(TradingPairStatus.ACTIVE);

        return toResponse(tradingPairRepository.save(tradingPair));
    }

    @Transactional
    public TradingPairResponse deactivate(Long id) {
        TradingPair tradingPair = getTradingPairById(id);
        tradingPair.setActive(false);
        tradingPair.setStatus(TradingPairStatus.INACTIVE);

        return toResponse(tradingPairRepository.save(tradingPair));
    }

    private TradingPair getTradingPairById(Long id) {
        return tradingPairRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trading pair not found with id: " + id));
    }

    private TradingPairResponse toResponse(TradingPair tradingPair) {
        return new TradingPairResponse(
                tradingPair.getId(),

                tradingPair.getExchange().getId(),
                tradingPair.getExchange().getCode(),

                tradingPair.getBaseAsset().getId(),
                tradingPair.getBaseAsset().getSymbol(),

                tradingPair.getQuoteAsset().getId(),
                tradingPair.getQuoteAsset().getSymbol(),

                tradingPair.getSymbol(),
                tradingPair.getStatus(),

                tradingPair.getPricePrecision(),
                tradingPair.getQuantityPrecision(),

                tradingPair.getMinQuantity(),
                tradingPair.getMaxQuantity(),

                tradingPair.isActive(),

                tradingPair.getCreatedAt(),
                tradingPair.getUpdatedAt()
        );
    }

    private String normalizeSymbol(String symbol) {
        return symbol.trim().toUpperCase();
    }
}
