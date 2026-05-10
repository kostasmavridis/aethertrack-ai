package com.aethertrack.api.dto;

/** Registration payload for POST /api/auth/register. */
public record RegisterRequest(String username, String email, String password) {}
