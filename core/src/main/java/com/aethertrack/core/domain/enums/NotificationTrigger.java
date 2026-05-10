package com.aethertrack.core.domain.enums;

/** Events that trigger a notification. */
public enum NotificationTrigger {
  /** Fired when a token cycle transitions to EXHAUSTED. */
  CYCLE_EXHAUSTED,

  /** Fired when a token cycle transitions back to ACTIVE after renewal. */
  CYCLE_RENEWED,

  /** Fired when usage crosses a configurable threshold (e.g. 80%). */
  USAGE_THRESHOLD_REACHED,

  /** Fired when a renewal is about to occur (configurable lead time). */
  RENEWAL_IMMINENT
}
