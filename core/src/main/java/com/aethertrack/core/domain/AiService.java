package com.aethertrack.core.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a registered AI provider (e.g., Anthropic Claude, Google Gemini, Cursor).
 * One service may have many Profiles (API key accounts).
 */
@Entity
@Table(name = "ai_services")
public class AiService {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank
  @Size(max = 100)
  @Column(nullable = false, unique = true)
  private String name;

  @Size(max = 500)
  private String description;

  /** URL of the service icon / logo. */
  @Size(max = 500)
  private String iconUrl;

  /** CSS accent color hex, e.g. "#D97706" for Claude amber. */
  @Size(max = 7)
  private String accentColor;

  /** Link to the provider's API documentation. */
  @Size(max = 500)
  private String apiDocsUrl;

  /** Provider-specific metadata as JSON (e.g. rate limit headers, token unit). */
  @Column(columnDefinition = "jsonb")
  private String metadata;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PricingTier pricingTier = PricingTier.FREE;

  @Column(nullable = false)
  private boolean active = true;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Profile> profiles = new ArrayList<>();

  @ManyToMany
  @JoinTable(
      name = "project_service_mappings",
      joinColumns = @JoinColumn(name = "service_id"),
      inverseJoinColumns = @JoinColumn(name = "project_id"))
  private List<Project> projects = new ArrayList<>();

  // ── Constructors ──────────────────────────────────────────────────────────

  protected AiService() {}

  public AiService(String name, PricingTier pricingTier) {
    this.name = name;
    this.pricingTier = pricingTier;
  }

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }

  // ── Getters / Setters ─────────────────────────────────────────────────────

  public UUID getId() { return id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public String getIconUrl() { return iconUrl; }
  public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
  public String getAccentColor() { return accentColor; }
  public void setAccentColor(String accentColor) { this.accentColor = accentColor; }
  public String getApiDocsUrl() { return apiDocsUrl; }
  public void setApiDocsUrl(String apiDocsUrl) { this.apiDocsUrl = apiDocsUrl; }
  public String getMetadata() { return metadata; }
  public void setMetadata(String metadata) { this.metadata = metadata; }
  public PricingTier getPricingTier() { return pricingTier; }
  public void setPricingTier(PricingTier pricingTier) { this.pricingTier = pricingTier; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public List<Profile> getProfiles() { return profiles; }
  public List<Project> getProjects() { return projects; }
}
