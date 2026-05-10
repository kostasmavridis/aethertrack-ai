package com.aethertrack.api.dto;

/** Payload for new user registration. */
public record RegisterRequest(String email, String username, String password) {}
