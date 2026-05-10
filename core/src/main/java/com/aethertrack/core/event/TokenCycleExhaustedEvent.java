package com.aethertrack.core.event;

import com.aethertrack.core.domain.TokenCycle;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event published when a TokenCycle transitions to EXHAUSTED.
 * Consumed by the NotificationHub and SSE broadcaster.
 */
public class TokenCycleExhaustedEvent extends ApplicationEvent {

  private final TokenCycle tokenCycle;

  public TokenCycleExhaustedEvent(Object source, TokenCycle tokenCycle) {
    super(source);
    this.tokenCycle = tokenCycle;
  }

  public TokenCycle getTokenCycle() {
    return tokenCycle;
  }
}
