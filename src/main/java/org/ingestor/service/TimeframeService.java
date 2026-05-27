package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.timeframe.CreateTimeframeRequest;
import org.ingestor.dto.timeframe.TimeframeResponse;
import org.ingestor.dto.timeframe.UpdateTimeframeRequest;
import org.ingestor.entity.Timeframe;
import org.ingestor.exception.ResourceNotFoundException;
import org.ingestor.repository.TimeframeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeframeService {

    private final TimeframeRepository timeframeRepository;

    @Transactional
    public TimeframeResponse create(CreateTimeframeRequest request) {
        String normalizedCode = normalizeCode(request.code());

        if (timeframeRepository.existsByCode(normalizedCode)) {
            throw new DataIntegrityViolationException("Timeframe already exists with code: " + normalizedCode);
        }

        Timeframe timeframe = Timeframe.builder()
                .code(normalizedCode)
                .durationMinutes(request.durationMinutes())
                .active(true)
                .build();

        Timeframe savedTimeframe = timeframeRepository.save(timeframe);

        return toResponse(savedTimeframe);
    }

    @Transactional(readOnly = true)
    public List<TimeframeResponse> findAll(Boolean activeOnly) {
        List<Timeframe> timeframes = Boolean.TRUE.equals(activeOnly)
                ? timeframeRepository.findByActiveTrue()
                : timeframeRepository.findAllByOrderByDurationMinutesAsc();

        return timeframes.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TimeframeResponse findById(Long id) {
        Timeframe timeframe = getTimeframeById(id);
        return toResponse(timeframe);
    }

    @Transactional(readOnly = true)
    public TimeframeResponse findByCode(String code) {
        Timeframe timeframe = timeframeRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new ResourceNotFoundException("Timeframe not found with code: " + code));

        return toResponse(timeframe);
    }

    @Transactional
    public TimeframeResponse update(Long id, UpdateTimeframeRequest request) {
        Timeframe timeframe = getTimeframeById(id);

        if (request.durationMinutes() != null) {
            timeframe.setDurationMinutes(request.durationMinutes());
        }

        if (request.active() != null) {
            timeframe.setActive(request.active());
        }

        Timeframe savedTimeframe = timeframeRepository.save(timeframe);

        return toResponse(savedTimeframe);
    }

    @Transactional
    public TimeframeResponse activate(Long id) {
        Timeframe timeframe = getTimeframeById(id);
        timeframe.setActive(true);

        return toResponse(timeframeRepository.save(timeframe));
    }

    @Transactional
    public TimeframeResponse deactivate(Long id) {
        Timeframe timeframe = getTimeframeById(id);
        timeframe.setActive(false);

        return toResponse(timeframeRepository.save(timeframe));
    }

    private Timeframe getTimeframeById(Long id) {
        return timeframeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timeframe not found with id: " + id));
    }

    private TimeframeResponse toResponse(Timeframe timeframe) {
        return new TimeframeResponse(
                timeframe.getId(),
                timeframe.getCode(),
                timeframe.getDurationMinutes(),
                timeframe.isActive(),
                timeframe.getCreatedAt(),
                timeframe.getUpdatedAt()
        );
    }

    private String normalizeCode(String code) {
        return code.trim().toLowerCase();
    }
}
