package com.aethertrack.api.dto;

import com.aethertrack.core.domain.enums.PricingTier;

/** Request payload for creating an AI service. */
public record CreateAiServiceRequest(
    String name,
    String description,
    String iconUrl,
    String accentColor,
    String apiDocsUrl,
    String metadata,
    PricingTier pricingTier
) {}
