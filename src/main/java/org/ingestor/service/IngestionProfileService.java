package org.ingestor.service;

import lombok.RequiredArgsConstructor;
import org.ingestor.dto.profile.CreateIngestionProfileRequest;
import org.ingestor.dto.profile.IngestionProfileResponse;
import org.ingestor.dto.profile.UpdateIngestionProfileRequest;
import org.ingestor.entity.IngestionProfile;
import org.ingestor.entity.enums.IngestionMethod;
import org.ingestor.exception.ResourceNotFoundException;
import org.ingestor.repository.IngestionProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestionProfileService {

    private final IngestionProfileRepository ingestionProfileRepository;

    @Transactional
    public IngestionProfileResponse create(CreateIngestionProfileRequest request) {
        String normalizedName = normalizeName(request.name());

        if (ingestionProfileRepository.existsByName(normalizedName)) {
            throw new DataIntegrityViolationException(
                    "Ingestion profile already exists with name: " + normalizedName
            );
        }

        IngestionProfile profile = IngestionProfile.builder()
                .name(normalizedName)
                .ingestionMethod(request.ingestionMethod())
                .description(request.description())
                .storesMarketData(resolveStoresMarketData(request.ingestionMethod()))
                .active(true)
                .build();

        return toResponse(ingestionProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public List<IngestionProfileResponse> findAll(
            IngestionMethod ingestionMethod,
            Boolean activeOnly
    ) {
        List<IngestionProfile> profiles;

        if (Boolean.TRUE.equals(activeOnly)) {
            profiles = ingestionProfileRepository.findByActiveTrue();
        } else if (ingestionMethod != null) {
            profiles = ingestionProfileRepository.findByIngestionMethod(ingestionMethod);
        } else {
            profiles = ingestionProfileRepository.findAll();
        }

        return profiles.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IngestionProfileResponse findById(Long id) {
        IngestionProfile profile = getProfileById(id);
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public IngestionProfileResponse findByName(String name) {
        IngestionProfile profile = ingestionProfileRepository.findByName(normalizeName(name))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingestion profile not found with name: " + name
                ));

        return toResponse(profile);
    }

    @Transactional
    public IngestionProfileResponse update(Long id, UpdateIngestionProfileRequest request) {
        IngestionProfile profile = getProfileById(id);

        if (request.name() != null) {
            String normalizedName = normalizeName(request.name());

            if (!profile.getName().equals(normalizedName)
                    && ingestionProfileRepository.existsByName(normalizedName)) {
                throw new DataIntegrityViolationException(
                        "Ingestion profile already exists with name: " + normalizedName
                );
            }

            profile.setName(normalizedName);
        }

        if (request.description() != null) {
            profile.setDescription(request.description());
        }

        if (request.active() != null) {
            profile.setActive(request.active());
        }

        profile.setStoresMarketData(resolveStoresMarketData(profile.getIngestionMethod()));

        return toResponse(ingestionProfileRepository.save(profile));
    }

    @Transactional
    public IngestionProfileResponse activate(Long id) {
        IngestionProfile profile = getProfileById(id);
        profile.setActive(true);

        return toResponse(ingestionProfileRepository.save(profile));
    }

    @Transactional
    public IngestionProfileResponse deactivate(Long id) {
        IngestionProfile profile = getProfileById(id);
        profile.setActive(false);

        return toResponse(ingestionProfileRepository.save(profile));
    }

    private IngestionProfile getProfileById(Long id) {
        return ingestionProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingestion profile not found with id: " + id
                ));
    }

    private IngestionProfileResponse toResponse(IngestionProfile profile) {
        return new IngestionProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getIngestionMethod(),
                profile.getDescription(),
                profile.isStoresMarketData(),
                profile.isActive(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private boolean resolveStoresMarketData(IngestionMethod ingestionMethod) {
        return ingestionMethod != IngestionMethod.WEBSOCKET_LIVE;
    }

    private String normalizeName(String name) {
        return name.trim();
    }
}