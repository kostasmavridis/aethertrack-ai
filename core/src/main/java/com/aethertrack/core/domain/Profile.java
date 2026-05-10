package com.aethertrack.core.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * An API key account / identity within a service.
 * One profile maps 1-to-many TokenCycles.
 */
@Entity
@Table(name = "profiles")
public class Profile {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank
  @Size(max = 100)
  @Column(nullable = false)
  private String displayName;

  /**
   * Encrypted API key. Stored via Jasypt at the service layer;
   * this field holds the jasypt-encrypted ciphertext.
   */
  @Column(nullable = false)
  private String encryptedApiKey;

  /** Purpose tag: "work", "personal", "experiment", etc. */
  @Size(max = 50)
  private String tag;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "service_id", nullable = false)
  private AiService service;

  @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TokenCycle> tokenCycles = new ArrayList<>();

  @Column(nullable = false)
  private boolean active = true;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  // ── Constructors ──────────────────────────────────────────────────────────

  protected Profile() {}

  public Profile(String displayName, String encryptedApiKey, AiService service) {
    this.displayName = displayName;
    this.encryptedApiKey = encryptedApiKey;
    this.service = service;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }

  // ── Getters / Setters ─────────────────────────────────────────────────────

  public UUID getId() { return id; }
  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }
  public String getEncryptedApiKey() { return encryptedApiKey; }
  public void setEncryptedApiKey(String encryptedApiKey) { this.encryptedApiKey = encryptedApiKey; }
  public String getTag() { return tag; }
  public void setTag(String tag) { this.tag = tag; }
  public AiService getService() { return service; }
  public void setService(AiService service) { this.service = service; }
  public List<TokenCycle> getTokenCycles() { return tokenCycles; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
