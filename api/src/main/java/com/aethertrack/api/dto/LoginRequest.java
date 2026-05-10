package com.aethertrack.api.dto;

/** Credentials for obtaining a JWT. */
public record LoginRequest(String username, String password) {}
