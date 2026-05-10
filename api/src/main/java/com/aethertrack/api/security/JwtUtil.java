package com.aethertrack.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Stateless JWT utility — generates, validates, and extracts claims.
 *
 * <p>Uses HMAC-SHA256 with the secret from {@code aethertrack.jwt.secret}.
 */
@Component
public class JwtUtil {

  private final SecretKey signingKey;
  private final long expirationMs;

  public JwtUtil(
      @Value("${aethertrack.jwt.secret}") String secret,
      @Value("${aethertrack.jwt.expiration-ms}") long expirationMs) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  // ── Token generation ────────────────────────────────────────────────────

  public String generateToken(String username, String role, UUID userId) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(username)
        .claim("role", role)
        .claim("userId", userId.toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(expirationMs)))
        .signWith(signingKey)
        .compact();
  }

  // ── Validation ──────────────────────────────────────────────────────────

  public boolean isValid(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  // ── Claims extraction ───────────────────────────────────────────────────

  public String extractUsername(String token) {
    return parseClaims(token).getSubject();
  }

  public String extractRole(String token) {
    return parseClaims(token).get("role", String.class);
  }

  public UUID extractUserId(String token) {
    return UUID.fromString(parseClaims(token).get("userId", String.class));
  }

  public Instant extractExpiry(String token) {
    return parseClaims(token).getExpiration().toInstant();
  }

  // ── Private helpers ─────────────────────────────────────────────────────

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
