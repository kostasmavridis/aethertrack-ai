package com.aethertrack.core.domain;

import com.aethertrack.core.domain.enums.RenewalScheduleType;
import com.aethertrack.core.domain.enums.TokenCycleState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tracks a token-quota cycle for a Profile.
 *
 * <p>State machine:
 * ACTIVE → EXHAUSTED → RENEWING → ACTIVE
 */
@Entity
@Table(name = "token_cycles")
public class TokenCycle {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "profile_id", nullable = false)
  private Profile profile;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TokenCycleState state = TokenCycleState.ACTIVE;

  @NotNull
  @Column(nullable = false)
  private Long tokenLimit;

  @Column(nullable = false)
  private Long tokensUsed = 0L;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RenewalScheduleType renewalScheduleType;

  /** Quartz cron expression (if scheduleType = CUSTOM_CRON). */
  private String renewalCron;

  /** Timezone for the renewal schedule, e.g. "Europe/Athens". */
  @Column(nullable = false)
  private String renewalTimezone = "UTC";

  @Column(nullable = false)
  private Instant cycleStartedAt = Instant.now();

  private Instant cycleExpiresAt;

  private Instant lastRenewedAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  @OneToMany(mappedBy = "tokenCycle", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UsageLog> usageLogs = new ArrayList<>();

  // ── Domain behaviours ──────────────────────────────────────────────────────

  /** Record token usage; transitions to EXHAUSTED when limit is hit. */
  public void recordUsage(long tokens) {
    if (state != TokenCycleState.ACTIVE) {
      throw new IllegalStateException("Cannot record usage on a non-ACTIVE cycle.");
    }
    this.tokensUsed += tokens;
    if (this.tokensUsed >= this.tokenLimit) {
      transitionTo(TokenCycleState.EXHAUSTED);
    }
  }

  /** Scheduler calls this to begin the renewing phase. */
  public void beginRenewal() {
    if (state != TokenCycleState.EXHAUSTED) {
      throw new IllegalStateException("Only EXHAUSTED cycles can begin renewal.");
    }
    transitionTo(TokenCycleState.RENEWING);
  }

  /** Called when renewal is confirmed (scheduler or manual override). */
  public void completeRenewal() {
    if (state != TokenCycleState.RENEWING) {
      throw new IllegalStateException("Only RENEWING cycles can complete renewal.");
    }
    this.tokensUsed = 0L;
    this.lastRenewedAt = Instant.now();
    this.cycleStartedAt = Instant.now();
    transitionTo(TokenCycleState.ACTIVE);
  }

  private void transitionTo(TokenCycleState next) {
    this.state = next;
    this.updatedAt = Instant.now();
  }

  // ── Constructors ──────────────────────────────────────────────────────────

  protected TokenCycle() {}

  public TokenCycle(Profile profile, Long tokenLimit, RenewalScheduleType renewalScheduleType) {
    this.profile = profile;
    this.tokenLimit = tokenLimit;
    this.renewalScheduleType = renewalScheduleType;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }

  // ── Getters / Setters ─────────────────────────────────────────────────────

  public UUID getId() { return id; }
  public Profile getProfile() { return profile; }
  public TokenCycleState getState() { return state; }
  public Long getTokenLimit() { return tokenLimit; }
  public void setTokenLimit(Long tokenLimit) { this.tokenLimit = tokenLimit; }
  public Long getTokensUsed() { return tokensUsed; }
  public RenewalScheduleType getRenewalScheduleType() { return renewalScheduleType; }
  public void setRenewalScheduleType(RenewalScheduleType renewalScheduleType) { this.renewalScheduleType = renewalScheduleType; }
  public String getRenewalCron() { return renewalCron; }
  public void setRenewalCron(String renewalCron) { this.renewalCron = renewalCron; }
  public String getRenewalTimezone() { return renewalTimezone; }
  public void setRenewalTimezone(String renewalTimezone) { this.renewalTimezone = renewalTimezone; }
  public Instant getCycleStartedAt() { return cycleStartedAt; }
  public Instant getCycleExpiresAt() { return cycleExpiresAt; }
  public void setCycleExpiresAt(Instant cycleExpiresAt) { this.cycleExpiresAt = cycleExpiresAt; }
  public Instant getLastRenewedAt() { return lastRenewedAt; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public List<UsageLog> getUsageLogs() { return usageLogs; }

  /** Remaining tokens in this cycle. */
  public long getRemainingTokens() {
    return Math.max(0, tokenLimit - tokensUsed);
  }

  /** Usage percentage (0-100). */
  public double getUsagePercent() {
    if (tokenLimit == 0) return 0;
    return (tokensUsed * 100.0) / tokenLimit;
  }
}
