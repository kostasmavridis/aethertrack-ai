package com.aethertrack.api.service;

import com.aethertrack.core.domain.NotificationRule;
import com.aethertrack.core.domain.TokenCycle;
import com.aethertrack.core.domain.enums.NotificationTrigger;
import com.aethertrack.core.event.TokenCycleExhaustedEvent;
import com.aethertrack.core.event.TokenCycleRenewedEvent;
import com.aethertrack.core.repository.NotificationRuleRepository;
import com.aethertrack.notification.dispatcher.NotificationDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves matching notification rules for domain events and dispatches them
 * through the appropriate channel adapters.
 */
@Service
public class NotificationService {

  private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

  private final NotificationRuleRepository notificationRuleRepository;
  private final Map<com.aethertrack.core.domain.enums.NotificationChannel, NotificationDispatcher> dispatchers;

  public NotificationService(
      NotificationRuleRepository notificationRuleRepository,
      List<NotificationDispatcher> dispatcherImplementations) {
    this.notificationRuleRepository = notificationRuleRepository;
    this.dispatchers = dispatcherImplementations.stream()
        .collect(Collectors.toMap(NotificationDispatcher::channel, Function.identity()));
  }

  @EventListener
  public void onTokenCycleExhausted(TokenCycleExhaustedEvent event) {
    dispatchForTrigger(event.getTokenCycle(), NotificationTrigger.CYCLE_EXHAUSTED, "Token cycle exhausted");
  }

  @EventListener
  public void onTokenCycleRenewed(TokenCycleRenewedEvent event) {
    dispatchForTrigger(event.getTokenCycle(), NotificationTrigger.CYCLE_RENEWED, "Token cycle renewed");
  }

  void dispatchForTrigger(TokenCycle cycle, NotificationTrigger trigger, String eventName) {
    List<NotificationRule> rules = notificationRuleRepository.findMatchingRules(
        trigger,
        cycle.getProfile().getService().getId(),
        cycle.getProfile().getId());

    if (rules.isEmpty()) {
      log.debug("No notification rules matched [{}] for cycle [{}]", trigger, cycle.getId());
      return;
    }

    for (NotificationRule rule : rules) {
      NotificationDispatcher dispatcher = dispatchers.get(rule.getChannel());
      if (dispatcher == null) {
        log.warn("No dispatcher registered for channel [{}]", rule.getChannel());
        continue;
      }
      dispatcher.dispatch(rule, cycle, eventName);
    }
  }
}
