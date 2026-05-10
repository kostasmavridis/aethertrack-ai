package com.aethertrack.api.dto;

/** Request payload for updating a profile. */
public record UpdateProfileRequest(
    String displayName,
    String apiKey,
    String tag,
    Boolean active
) {}
