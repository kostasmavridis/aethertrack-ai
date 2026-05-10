package com.aethertrack.core.domain;

import com.aethertrack.core.domain.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Application user — holds credentials and role for JWT-based auth.
 */
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank
  @Size(max = 60)
  @Column(nullable = false, unique = true)
  private String username;

  @Email
  @NotBlank
  @Size(max = 150)
  @Column(nullable = false, unique = true)
  private String email;

  /** BCrypt-hashed password. */
  @NotBlank
  @Column(nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role = UserRole.USER;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  // ── Constructors ──────────────────────────────────────────────────────────

  protected User() {}

  public User(String username, String email, String passwordHash) {
    this.username     = username;
    this.email        = email;
    this.passwordHash = passwordHash;
  }

  @PreUpdate
  void onUpdate() { this.updatedAt = Instant.now(); }

  // ── Getters / Setters ─────────────────────────────────────────────────────

  public UUID    getId()           { return id; }
  public String  getUsername()     { return username; }
  public void    setUsername(String u) { this.username = u; }
  public String  getEmail()        { return email; }
  public void    setEmail(String e) { this.email = e; }
  public String  getPasswordHash() { return passwordHash; }
  public void    setPasswordHash(String h) { this.passwordHash = h; }
  public UserRole getRole()        { return role; }
  public void    setRole(UserRole r) { this.role = r; }
  public boolean isEnabled()       { return enabled; }
  public void    setEnabled(boolean e) { this.enabled = e; }
  public Instant getCreatedAt()    { return createdAt; }
  public Instant getUpdatedAt()    { return updatedAt; }
}
