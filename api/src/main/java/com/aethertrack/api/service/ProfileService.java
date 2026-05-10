package com.aethertrack.api.service;

import com.aethertrack.core.domain.AiService;
import com.aethertrack.core.domain.Profile;
import com.aethertrack.core.repository.AiServiceRepository;
import com.aethertrack.core.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Application service for profile management.
 *
 * <p>Profiles represent API key accounts per AI service. The encryptedApiKey field
 * currently uses a placeholder Base64 strategy and should be replaced with Jasypt/Vault.
 */
@Service
@Transactional
public class ProfileService {

  private final ProfileRepository profileRepository;
  private final AiServiceRepository aiServiceRepository;

  public ProfileService(
      ProfileRepository profileRepository,
      AiServiceRepository aiServiceRepository) {
    this.profileRepository = profileRepository;
    this.aiServiceRepository = aiServiceRepository;
  }

  @Transactional(readOnly = true)
  public List<Profile> listActive(UUID serviceId, String tag) {
    if (serviceId != null && tag != null && !tag.isBlank()) {
      return profileRepository.findAllByServiceIdAndTagAndActiveTrue(serviceId, tag);
    }
    if (serviceId != null) {
      return profileRepository.findAllByServiceIdAndActiveTrue(serviceId);
    }
    if (tag != null && !tag.isBlank()) {
      return profileRepository.findAllByTagAndActiveTrue(tag);
    }
    return profileRepository.findAllByActiveTrueOrderByDisplayNameAsc();
  }

  @Transactional(readOnly = true)
  public Profile getById(UUID id) {
    return profileRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + id));
  }

  public Profile create(UUID serviceId, String displayName, String rawApiKey, String tag) {
    AiService service = aiServiceRepository.findById(serviceId)
        .orElseThrow(() -> new EntityNotFoundException("AI service not found: " + serviceId));

    profileRepository.findByDisplayNameAndServiceId(displayName, serviceId)
        .ifPresent(existing -> {
          throw new IllegalArgumentException(
              "Profile already exists with display name '" + displayName + "' for service " + service.getName());
        });

    Profile profile = new Profile(displayName, encrypt(rawApiKey), service);
    profile.setTag(tag);
    return profileRepository.save(profile);
  }

  public Profile update(UUID id, String displayName, String rawApiKey, String tag, Boolean active) {
    Profile profile = getById(id);

    if (displayName != null && !displayName.isBlank()) {
      profile.setDisplayName(displayName.trim());
    }
    if (rawApiKey != null && !rawApiKey.isBlank()) {
      profile.setEncryptedApiKey(encrypt(rawApiKey));
    }
    if (tag != null) {
      profile.setTag(tag);
    }
    if (active != null) {
      profile.setActive(active);
    }

    return profileRepository.save(profile);
  }

  public void delete(UUID id) {
    if (!profileRepository.existsById(id)) {
      throw new EntityNotFoundException("Profile not found: " + id);
    }
    profileRepository.deleteById(id);
  }

  private String encrypt(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("API key must not be blank");
    }
    return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }
}
