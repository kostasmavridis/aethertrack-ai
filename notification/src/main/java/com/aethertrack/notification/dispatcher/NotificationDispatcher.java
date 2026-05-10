package com.aethertrack.notification.dispatcher;

import com.aethertrack.core.domain.NotificationRule;
import com.aethertrack.core.domain.TokenCycle;
import com.aethertrack.core.domain.enums.NotificationChannel;

/**
 * SPI for notification channel implementations.
 *
 * <p>Implement this interface to add a new notification channel.
 * Spring will auto-discover all beans implementing this interface.
 */
public interface NotificationDispatcher {

  /** Returns the channel this dispatcher handles. */
  NotificationChannel channel();

  /**
   * Dispatch a notification for the given token cycle event.
   *
   * @param rule      the matched notification rule (contains target, quiet hours, etc.)
   * @param cycle     the token cycle that triggered the event
   * @param eventName human-readable event name (e.g. "Token cycle renewed")
   */
  void dispatch(NotificationRule rule, TokenCycle cycle, String eventName);
}
