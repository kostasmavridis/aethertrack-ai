package com.aethertrack.api.controller;

import com.aethertrack.api.dto.*;
import com.aethertrack.api.security.JwtTokenProvider;
import com.aethertrack.core.domain.User;
import com.aethertrack.core.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 *
 * <ul>
 *   <li>{@code POST /api/auth/register} — create a new user account</li>
 *   <li>{@code POST /api/auth/login}    — exchange credentials for tokens</li>
 *   <li>{@code POST /api/auth/refresh}  — exchange a refresh token for a new access token</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registration, login, and token refresh")
public class AuthController {

  private final AuthenticationManager authManager;
  private final JwtTokenProvider      jwtProvider;
  private final UserRepository        userRepository;
  private final PasswordEncoder       passwordEncoder;

  public AuthController(
      AuthenticationManager authManager,
      JwtTokenProvider jwtProvider,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    this.authManager     = authManager;
    this.jwtProvider     = jwtProvider;
    this.userRepository  = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  // ── Register ──────────────────────────────────────────────────────────────

  @PostMapping("/register")
  @Operation(summary = "Register a new user account")
  public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
    if (userRepository.existsByUsername(request.username())) {
      throw new IllegalArgumentException("Username already taken: " + request.username());
    }
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email already registered: " + request.email());
    }

    User user = new User(
        request.username(),
        request.email(),
        passwordEncoder.encode(request.password()));
    userRepository.save(user);

    return ResponseEntity.ok(issueTokens(request.username(), user.getRole().name()));
  }

  // ── Login ─────────────────────────────────────────────────────────────────

  @PostMapping("/login")
  @Operation(summary = "Authenticate and receive JWT tokens")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
    Authentication auth = authManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.username(), request.password()));

    String role = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .findFirst()
        .orElse("ROLE_USER")
        .replace("ROLE_", "");

    return ResponseEntity.ok(issueTokens(request.username(), role));
  }

  // ── Refresh ───────────────────────────────────────────────────────────────

  @PostMapping("/refresh")
  @Operation(summary = "Exchange a refresh token for a new access token")
  public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
    String token = request.refreshToken();

    if (!jwtProvider.validateToken(token) || !jwtProvider.isRefreshToken(token)) {
      throw new IllegalArgumentException("Invalid or expired refresh token");
    }

    String username = jwtProvider.extractUsername(token);
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

    return ResponseEntity.ok(issueTokens(username, user.getRole().name()));
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private AuthResponse issueTokens(String username, String role) {
    String access  = jwtProvider.generateAccessToken(username, role);
    String refresh = jwtProvider.generateRefreshToken(username);
    return new AuthResponse(access, refresh, jwtProvider.extractExpiry(access), username, role);
  }
}
