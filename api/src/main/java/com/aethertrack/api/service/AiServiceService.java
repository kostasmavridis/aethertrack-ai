package com.aethertrack.api.service;

import com.aethertrack.core.domain.AiService;
import com.aethertrack.core.domain.enums.PricingTier;
import com.aethertrack.core.repository.AiServiceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for AI provider registry management.
 *
 * <p>Handles CRUD, search, and active filtering for registered AI services.
 */
@Service
@Transactional
public class AiServiceService {

  private final AiServiceRepository aiServiceRepository;

  public AiServiceService(AiServiceRepository aiServiceRepository) {
    this.aiServiceRepository = aiServiceRepository;
  }

  @Transactional(readOnly = true)
  public List<AiService> listActive() {
    return aiServiceRepository.findAllByActiveTrueOrderByNameAsc();
  }

  @Transactional(readOnly = true)
  public List<AiService> listAll() {
    return aiServiceRepository.findAll();
  }

  @Transactional(readOnly = true)
  public AiService getById(UUID id) {
    return aiServiceRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("AI service not found: " + id));
  }

  @Transactional(readOnly = true)
  public List<AiService> search(String query) {
    if (query == null || query.isBlank()) {
      return listActive();
    }
    return aiServiceRepository.searchByNameOrDescription(query.trim());
  }

  public AiService create(
      String name,
      String description,
      String iconUrl,
      String accentColor,
      String apiDocsUrl,
      String metadata,
      PricingTier pricingTier) {

    if (aiServiceRepository.existsByName(name)) {
      throw new IllegalArgumentException("AI service already exists with name: " + name);
    }

    AiService service = new AiService(name, pricingTier == null ? PricingTier.FREE : pricingTier);
    service.setDescription(description);
    service.setIconUrl(iconUrl);
    service.setAccentColor(accentColor);
    service.setApiDocsUrl(apiDocsUrl);
    service.setMetadata(metadata);

    return aiServiceRepository.save(service);
  }

  public AiService update(
      UUID id,
      String name,
      String description,
      String iconUrl,
      String accentColor,
      String apiDocsUrl,
      String metadata,
      PricingTier pricingTier,
      Boolean active) {

    AiService service = getById(id);

    if (name != null && !name.equals(service.getName()) && aiServiceRepository.existsByName(name)) {
      throw new IllegalArgumentException("AI service already exists with name: " + name);
    }

    if (name != null && !name.isBlank()) service.setName(name.trim());
    if (description != null) service.setDescription(description);
    if (iconUrl != null) service.setIconUrl(iconUrl);
    if (accentColor != null) service.setAccentColor(accentColor);
    if (apiDocsUrl != null) service.setApiDocsUrl(apiDocsUrl);
    if (metadata != null) service.setMetadata(metadata);
    if (pricingTier != null) service.setPricingTier(pricingTier);
    if (active != null) service.setActive(active);

    return aiServiceRepository.save(service);
  }

  public void delete(UUID id) {
    if (!aiServiceRepository.existsById(id)) {
      throw new EntityNotFoundException("AI service not found: " + id);
    }
    aiServiceRepository.deleteById(id);
  }
}
