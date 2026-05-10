package com.aethertrack.api.dto;

import java.util.Date;

/**
 * JWT token response.
 *
 * @param accessToken   short-lived Bearer token (default 24 h)
 * @param refreshToken  long-lived refresh token (default 7 d)
 * @param tokenType     always "Bearer"
 * @param expiresAt     access token expiry timestamp
 * @param username      the authenticated username
 * @param role          the user's role (USER or ADMIN)
 */
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Date   expiresAt,
    String username,
    String role
) {
  public AuthResponse(String accessToken, String refreshToken, Date expiresAt, String username, String role) {
    this(accessToken, refreshToken, "Bearer", expiresAt, username, role);
  }
}
