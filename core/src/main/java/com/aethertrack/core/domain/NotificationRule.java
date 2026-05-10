package com.aethertrack.core.domain;

import com.aethertrack.core.domain.enums.NotificationChannel;
import com.aethertrack.core.domain.enums.NotificationTrigger;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Defines when and how a user should be notified about a token-cycle event. */
@Entity
@Table(name = "notification_rules")
public class NotificationRule {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationTrigger trigger;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationChannel channel;

  /** Channel-specific target (email address, webhook URL, Slack channel ID, etc.). */
  @Column(nullable = false)
  private String target;

  /** Optional: scope to a specific service. Null = all services. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "service_id")
  private AiService service;

  /** Optional: scope to a specific profile. Null = all profiles. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_id")
  private Profile profile;

  /** Quiet-hours start (hour of day, 0-23, in user timezone). */
  private Integer quietHoursStart;

  /** Quiet-hours end (hour of day, 0-23, in user timezone). */
  private Integer quietHoursEnd;

  /** Timezone for quiet hours, e.g. "Europe/Athens". */
  private String quietHoursTimezone;

  @Column(nullable = false)
  private boolean digestMode = false;

  @Column(nullable = false)
  private boolean active = true;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  // ── Constructors ──────────────────────────────────────────────────────────

  protected NotificationRule() {}

  public NotificationRule(NotificationTrigger trigger, NotificationChannel channel, String target) {
    this.trigger = trigger;
    this.channel = channel;
    this.target = target;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }

  // ── Getters / Setters ─────────────────────────────────────────────────────

  public UUID getId() { return id; }
  public NotificationTrigger getTrigger() { return trigger; }
  public void setTrigger(NotificationTrigger trigger) { this.trigger = trigger; }
  public NotificationChannel getChannel() { return channel; }
  public void setChannel(NotificationChannel channel) { this.channel = channel; }
  public String getTarget() { return target; }
  public void setTarget(String target) { this.target = target; }
  public AiService getService() { return service; }
  public void setService(AiService service) { this.service = service; }
  public Profile getProfile() { return profile; }
  public void setProfile(Profile profile) { this.profile = profile; }
  public Integer getQuietHoursStart() { return quietHoursStart; }
  public void setQuietHoursStart(Integer quietHoursStart) { this.quietHoursStart = quietHoursStart; }
  public Integer getQuietHoursEnd() { return quietHoursEnd; }
  public void setQuietHoursEnd(Integer quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }
  public String getQuietHoursTimezone() { return quietHoursTimezone; }
  public void setQuietHoursTimezone(String quietHoursTimezone) { this.quietHoursTimezone = quietHoursTimezone; }
  public boolean isDigestMode() { return digestMode; }
  public void setDigestMode(boolean digestMode) { this.digestMode = digestMode; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
