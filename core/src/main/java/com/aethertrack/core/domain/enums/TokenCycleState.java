package com.aethertrack.core.domain.enums;

/**
 * State machine for a token cycle:
 *
 * <pre>
 * ACTIVE → EXHAUSTED → RENEWING → ACTIVE
 * </pre>
 *
 * <p>Transitions emit domain events that drive SSE broadcasts and notification dispatch.
 */
public enum TokenCycleState {
  /** Quota is available; usage recording is open. */
  ACTIVE,

  /** Token limit has been reached; no further usage can be recorded. */
  EXHAUSTED,

  /** Scheduler has triggered a renewal; waiting for reset confirmation. */
  RENEWING
}
