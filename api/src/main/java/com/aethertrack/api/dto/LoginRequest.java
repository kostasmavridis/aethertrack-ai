package com.aethertrack.api.dto;

/** Credentials payload for POST /api/auth/login. */
public record LoginRequest(String username, String password) {}
