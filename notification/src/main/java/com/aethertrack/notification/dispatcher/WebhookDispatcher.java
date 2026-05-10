package com.aethertrack.notification.dispatcher;

import com.aethertrack.core.domain.NotificationRule;
import com.aethertrack.core.domain.TokenCycle;
import com.aethertrack.core.domain.enums.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Dispatches notifications via HTTP POST webhook.
 * Works for generic webhooks, Slack incoming webhooks, and Discord webhooks.
 */
@Component
public class WebhookDispatcher implements NotificationDispatcher {

  private static final Logger log = LoggerFactory.getLogger(WebhookDispatcher.class);

  private final RestClient restClient;

  public WebhookDispatcher(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.build();
  }

  @Override
  public NotificationChannel channel() {
    return NotificationChannel.WEBHOOK;
  }

  @Override
  public void dispatch(NotificationRule rule, TokenCycle cycle, String eventName) {
    var payload = Map.of(
        "event", eventName,
        "service", cycle.getProfile().getService().getName(),
        "profile", cycle.getProfile().getDisplayName(),
        "state", cycle.getState().name(),
        "tokensUsed", cycle.getTokensUsed(),
        "tokenLimit", cycle.getTokenLimit(),
        "usagePercent", cycle.getUsagePercent()
    );

    try {
      restClient.post()
          .uri(rule.getTarget())
          .body(payload)
          .retrieve()
          .toBodilessEntity();
      log.info("Webhook dispatched to [{}] for event [{}]", rule.getTarget(), eventName);
    } catch (Exception e) {
      log.error("Webhook dispatch failed to [{}]: {}", rule.getTarget(), e.getMessage(), e);
      throw new RuntimeException("Webhook dispatch failed", e);
    }
  }
}
