package com.aethertrack.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Handles JWT token generation, validation, and claim extraction.
 */
@Component
public class JwtTokenProvider {

  private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

  private final SecretKey signingKey;
  private final long      expirationMs;
  private final long      refreshExpirationMs;

  public JwtTokenProvider(
      @Value("${aethertrack.jwt.secret}") String secret,
      @Value("${aethertrack.jwt.expiration-ms:86400000}") long expirationMs,
      @Value("${aethertrack.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {

    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
      throw new IllegalArgumentException("JWT secret must be at least 32 characters (256-bit)");
    }
    this.signingKey          = Keys.hmacShaKeyFor(keyBytes);
    this.expirationMs        = expirationMs;
    this.refreshExpirationMs = refreshExpirationMs;
  }

  // ── Token generation ─────────────────────────────────────────────────────

  public String generateAccessToken(String username, String role) {
    return buildToken(username, role, expirationMs, "access");
  }

  public String generateRefreshToken(String username) {
    return buildToken(username, null, refreshExpirationMs, "refresh");
  }

  private String buildToken(String subject, String role, long ttlMs, String tokenType) {
    Date now     = new Date();
    Date expiry  = new Date(now.getTime() + ttlMs);

    JwtBuilder builder = Jwts.builder()
        .subject(subject)
        .issuedAt(now)
        .expiration(expiry)
        .claim("type", tokenType)
        .signWith(signingKey);

    if (role != null) {
      builder.claim("role", role);
    }
    return builder.compact();
  }

  // ── Validation ────────────────────────────────────────────────────────────

  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (ExpiredJwtException e) {
      log.debug("JWT expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.warn("JWT unsupported: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.warn("JWT malformed: {}", e.getMessage());
    } catch (SecurityException e) {
      log.warn("JWT signature invalid: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.warn("JWT claims empty: {}", e.getMessage());
    }
    return false;
  }

  public boolean isRefreshToken(String token) {
    try {
      return "refresh".equals(parseClaims(token).get("type", String.class));
    } catch (Exception e) {
      return false;
    }
  }

  // ── Claim extraction ──────────────────────────────────────────────────────

  public String extractUsername(String token) {
    return parseClaims(token).getSubject();
  }

  public String extractRole(String token) {
    return parseClaims(token).get("role", String.class);
  }

  public Date extractExpiry(String token) {
    return parseClaims(token).getExpiration();
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
