package com.aethertrack.core.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Immutable record of a single token-usage event. */
@Entity
@Table(name = "usage_logs")
public class UsageLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "token_cycle_id", nullable = false)
  private TokenCycle tokenCycle;

  @Column(nullable = false)
  private long tokensConsumed;

  /** Optional reference to the originating project. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id")
  private Project project;

  /** Estimated cost in USD for this usage event. */
  private Double estimatedCostUsd;

  @Column(nullable = false, updatable = false)
  private Instant loggedAt = Instant.now();

  // ── Constructors ──────────────────────────────────────────────────────────

  protected UsageLog() {}

  public UsageLog(TokenCycle tokenCycle, long tokensConsumed) {
    this.tokenCycle = tokenCycle;
    this.tokensConsumed = tokensConsumed;
  }

  // ── Getters ───────────────────────────────────────────────────────────────

  public UUID getId() { return id; }
  public TokenCycle getTokenCycle() { return tokenCycle; }
  public long getTokensConsumed() { return tokensConsumed; }
  public Project getProject() { return project; }
  public void setProject(Project project) { this.project = project; }
  public Double getEstimatedCostUsd() { return estimatedCostUsd; }
  public void setEstimatedCostUsd(Double estimatedCostUsd) { this.estimatedCostUsd = estimatedCostUsd; }
  public Instant getLoggedAt() { return loggedAt; }
}
