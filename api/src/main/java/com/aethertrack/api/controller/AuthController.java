package com.aethertrack.api.controller;

import com.aethertrack.api.dto.AuthResponse;
import com.aethertrack.api.dto.LoginRequest;
import com.aethertrack.api.dto.RegisterRequest;
import com.aethertrack.api.security.JwtUtil;
import com.aethertrack.core.domain.AppUser;
import com.aethertrack.core.repository.AppUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Authentication endpoints.
 *
 * <p>Public routes:
 * <ul>
 *   <li>{@code POST /api/auth/register} — create a new account</li>
 *   <li>{@code POST /api/auth/login}    — obtain a JWT</li>
 *   <li>{@code POST /api/auth/refresh}  — exchange a valid JWT for a fresh one</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register, login, and refresh JWT tokens")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final AppUserRepository appUserRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthController(
      AuthenticationManager authenticationManager,
      AppUserRepository appUserRepository,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil) {
    this.authenticationManager = authenticationManager;
    this.appUserRepository = appUserRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  // ── Register ────────────────────────────────────────────────────────────

  @PostMapping("/register")
  @Operation(summary = "Register a new user account")
  public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
    if (appUserRepository.existsByUsername(request.username())) {
      throw new IllegalArgumentException("Username already taken: " + request.username());
    }
    if (appUserRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email already registered: " + request.email());
    }

    AppUser user = new AppUser(
        request.email(),
        request.username(),
        passwordEncoder.encode(request.password()));
    AppUser saved = appUserRepository.save(user);

    String token = jwtUtil.generateToken(
        saved.getUsername(), saved.getRole().name(), saved.getId());

    return ResponseEntity
        .created(URI.create("/api/auth/me"))
        .body(new AuthResponse(
            token,
            saved.getUsername(),
            saved.getRole().name(),
            jwtUtil.extractExpiry(token)));
  }

  // ── Login ───────────────────────────────────────────────────────────────

  @PostMapping("/login")
  @Operation(summary = "Authenticate and obtain a JWT")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.username(), request.password()));

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    AppUser user = appUserRepository.findByUsername(userDetails.getUsername())
        .orElseThrow();

    String token = jwtUtil.generateToken(
        user.getUsername(), user.getRole().name(), user.getId());

    return ResponseEntity.ok(new AuthResponse(
        token,
        user.getUsername(),
        user.getRole().name(),
        jwtUtil.extractExpiry(token)));
  }

  // ── Refresh ─────────────────────────────────────────────────────────────

  @PostMapping("/refresh")
  @Operation(summary = "Exchange a valid (non-expired) JWT for a fresh one")
  public ResponseEntity<AuthResponse> refresh(
      @RequestHeader("Authorization") String authHeader) {

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Missing or malformed Authorization header");
    }
    String oldToken = authHeader.substring(7);
    if (!jwtUtil.isValid(oldToken)) {
      throw new IllegalArgumentException("Token is invalid or expired");
    }

    String username = jwtUtil.extractUsername(oldToken);
    AppUser user = appUserRepository.findByUsername(username).orElseThrow();

    String newToken = jwtUtil.generateToken(
        user.getUsername(), user.getRole().name(), user.getId());

    return ResponseEntity.ok(new AuthResponse(
        newToken,
        user.getUsername(),
        user.getRole().name(),
        jwtUtil.extractExpiry(newToken)));
  }
}
