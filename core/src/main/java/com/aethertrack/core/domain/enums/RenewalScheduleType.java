package com.aethertrack.core.domain.enums;

/** How frequently a token cycle resets. */
public enum RenewalScheduleType {
  /** Resets every day at a configured time. */
  DAILY,

  /** Resets every week on a configured day and time. */
  WEEKLY,

  /** Resets on a user-provided Quartz cron expression. */
  CUSTOM_CRON,

  /** Manual reset only; the user explicitly marks the cycle as renewed. */
  MANUAL
}
