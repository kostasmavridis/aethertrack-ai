package com.aethertrack.api.dto;

import com.aethertrack.core.domain.enums.RenewalScheduleType;

import java.time.Instant;
import java.util.UUID;

/** Request payload for creating a token cycle. */
public record CreateTokenCycleRequest(
    UUID profileId,
    Long tokenLimit,
    RenewalScheduleType renewalScheduleType,
    String renewalCron,
    String renewalTimezone,
    Instant cycleExpiresAt
) {}
