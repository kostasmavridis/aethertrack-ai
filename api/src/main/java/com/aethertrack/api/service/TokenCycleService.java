package com.aethertrack.api.service;

import com.aethertrack.core.domain.Profile;
import com.aethertrack.core.domain.TokenCycle;
import com.aethertrack.core.domain.UsageLog;
import com.aethertrack.core.domain.enums.RenewalScheduleType;
import com.aethertrack.core.domain.enums.TokenCycleState;
import com.aethertrack.core.event.TokenCycleExhaustedEvent;
import com.aethertrack.core.event.TokenCycleRenewedEvent;
import com.aethertrack.core.repository.ProfileRepository;
import com.aethertrack.core.repository.TokenCycleRepository;
import com.aethertrack.core.repository.UsageLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Application service for token cycle management.
 *
 * <p>Responsible for usage tracking, manual renewal, and domain event publication.
 */
@Service
@Transactional
public class TokenCycleService {

  private final TokenCycleRepository tokenCycleRepository;
  private final ProfileRepository profileRepository;
  private final UsageLogRepository usageLogRepository;
  private final ApplicationEventPublisher eventPublisher;

  public TokenCycleService(
      TokenCycleRepository tokenCycleRepository,
      ProfileRepository profileRepository,
      UsageLogRepository usageLogRepository,
      ApplicationEventPublisher eventPublisher) {
    this.tokenCycleRepository = tokenCycleRepository;
    this.profileRepository = profileRepository;
    this.usageLogRepository = usageLogRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional(readOnly = true)
  public List<TokenCycle> list(UUID profileId, TokenCycleState state) {
    if (profileId != null && state != null) {
      return tokenCycleRepository.findAllByProfileIdAndState(profileId, state);
    }
    if (profileId != null) {
      return tokenCycleRepository.findAllByProfileId(profileId);
    }
    if (state != null) {
      return tokenCycleRepository.findAllByState(state);
    }
    return tokenCycleRepository.findAll();
  }

  @Transactional(readOnly = true)
  public TokenCycle getById(UUID id) {
    return tokenCycleRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Token cycle not found: " + id));
  }

  public TokenCycle create(
      UUID profileId,
      Long tokenLimit,
      RenewalScheduleType renewalScheduleType,
      String renewalCron,
      String renewalTimezone,
      Instant cycleExpiresAt) {

    Profile profile = profileRepository.findById(profileId)
        .orElseThrow(() -> new EntityNotFoundException("Profile not found: " + profileId));

    TokenCycle cycle = new TokenCycle(profile, tokenLimit, renewalScheduleType);
    cycle.setRenewalCron(renewalCron);
    cycle.setRenewalTimezone(renewalTimezone == null || renewalTimezone.isBlank() ? "UTC" : renewalTimezone);
    cycle.setCycleExpiresAt(cycleExpiresAt);

    return tokenCycleRepository.save(cycle);
  }

  public TokenCycle recordUsage(UUID cycleId, long tokensConsumed) {
    if (tokensConsumed <= 0) {
      throw new IllegalArgumentException("tokensConsumed must be greater than zero");
    }

    TokenCycle cycle = getById(cycleId);
    TokenCycleState previousState = cycle.getState();

    cycle.recordUsage(tokensConsumed);
    TokenCycle saved = tokenCycleRepository.save(cycle);

    UsageLog log = new UsageLog(saved, tokensConsumed);
    usageLogRepository.save(log);

    if (previousState != TokenCycleState.EXHAUSTED && saved.getState() == TokenCycleState.EXHAUSTED) {
      eventPublisher.publishEvent(new TokenCycleExhaustedEvent(this, saved));
    }

    return saved;
  }

  public TokenCycle manualRenew(UUID cycleId) {
    TokenCycle cycle = getById(cycleId);

    if (cycle.getState() == TokenCycleState.EXHAUSTED) {
      cycle.beginRenewal();
    }
    if (cycle.getState() != TokenCycleState.RENEWING) {
      throw new IllegalStateException("Cycle must be EXHAUSTED or RENEWING before renewal can complete.");
    }

    cycle.completeRenewal();
    TokenCycle saved = tokenCycleRepository.save(cycle);
    eventPublisher.publishEvent(new TokenCycleRenewedEvent(this, saved));
    return saved;
  }
}
