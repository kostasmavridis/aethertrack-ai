package com.aethertrack.core.event;

import com.aethertrack.core.domain.TokenCycle;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event published when a TokenCycle transitions back to ACTIVE.
 * Consumed by the NotificationHub and SSE broadcaster.
 */
public class TokenCycleRenewedEvent extends ApplicationEvent {

  private final TokenCycle tokenCycle;

  public TokenCycleRenewedEvent(Object source, TokenCycle tokenCycle) {
    super(source);
    this.tokenCycle = tokenCycle;
  }

  public TokenCycle getTokenCycle() {
    return tokenCycle;
  }
}
