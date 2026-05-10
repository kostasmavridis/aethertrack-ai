package com.aethertrack.api.dto;

/** Request payload for creating a profile. */
public record CreateProfileRequest(
    java.util.UUID serviceId,
    String displayName,
    String apiKey,
    String tag
) {}
