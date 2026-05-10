package com.aethertrack.core.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A codebase, prompt workflow, or product that consumes one or more AI services. */
@Entity
@Table(name = "projects")
public class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotBlank
  @Size(max = 100)
  @Column(nullable = false)
  private String name;

  @Size(max = 500)
  private String description;

  @ManyToMany(mappedBy = "projects")
  private List<AiService> services = new ArrayList<>();

  @Column(nullable = false)
  private boolean active = true;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  // ── Constructors ──────────────────────────────────────────────────────────

  protected Project() {}

  public Project(String name) {
    this.name = name;
  }

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
  public List<AiService> getServices() { return services; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
