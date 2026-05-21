package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.UpdateExchangeRequest;
import org.ingestor.dto.exchange.CreateExchangeRequest;
import org.ingestor.dto.exchange.ExchangeResponse;
import org.ingestor.entity.Exchange;
import org.ingestor.exception.ResourceNotFoundException;
import org.ingestor.repository.ExchangeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final ExchangeRepository exchangeRepository;

    @Transactional
    public ExchangeResponse create(CreateExchangeRequest request) {
        Exchange exchange = Exchange.builder()
                .code(normalizeCode(request.code()))
                .name(request.name())
                .restBaseUrl(request.restBaseUrl())
                .websocketBaseUrl(request.websocketBaseUrl())
                .active(true)
                .build();

        Exchange createdExchange = exchangeRepository.save(exchange);

        return toResponse(createdExchange);
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> findAll() {
        return exchangeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExchangeResponse findById(Long id) {
        Exchange exchange = getExchangeById(id);
        return toResponse(exchange);
    }

    @Transactional
    public ExchangeResponse update(Long id, UpdateExchangeRequest request) {
        Exchange exchange = getExchangeById(id);

        setIfNotNull(request.name(), exchange::setName);
        setIfNotNull(request.restBaseUrl(), exchange::setRestBaseUrl);
        setIfNotNull(request.webSocketBaseUrl(), exchange::setWebsocketBaseUrl);
        setIfNotNull(request.active(), exchange::setActive);

        return toResponse(exchange);
    }

    @Transactional
    public ExchangeResponse activate(Long id) {
        Exchange exchange = getExchangeById(id);
        exchange.setActive(true);
        return toResponse(exchange);
    }

    @Transactional
    public ExchangeResponse deactivate(Long id) {
        Exchange exchange = getExchangeById(id);
        exchange.setActive(false);
        return toResponse(exchange);
    }

    private <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private Exchange getExchangeById(Long id) {
        return exchangeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found with id: " + id));
    }

    private ExchangeResponse toResponse(Exchange exchange) {
        return new ExchangeResponse(
                exchange.getId(),
                exchange.getCode(),
                exchange.getName(),
                exchange.getRestBaseUrl(),
                exchange.getWebsocketBaseUrl(),
                exchange.isActive(),
                exchange.getCreatedAt(),
                exchange.getUpdatedAt()
        );
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}
