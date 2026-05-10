package com.aethertrack.api.dto;

/** Payload for POST /api/auth/refresh. */
public record RefreshTokenRequest(String refreshToken) {}
