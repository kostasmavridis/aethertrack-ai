package com.aethertrack.api.dto;

import java.time.Instant;

/** JWT response returned after successful login or token refresh. */
public record AuthResponse(
    String token,
    String username,
    String role,
    Instant expiresAt
) {}
