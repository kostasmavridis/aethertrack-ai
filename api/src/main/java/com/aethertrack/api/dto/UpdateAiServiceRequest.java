package com.aethertrack.api.dto;

import com.aethertrack.core.domain.enums.PricingTier;

/** Request payload for updating an AI service. */
public record UpdateAiServiceRequest(
    String name,
    String description,
    String iconUrl,
    String accentColor,
    String apiDocsUrl,
    String metadata,
    PricingTier pricingTier,
    Boolean active
) {}
