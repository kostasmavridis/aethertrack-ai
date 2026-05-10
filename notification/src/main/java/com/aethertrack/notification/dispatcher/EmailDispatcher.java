package com.aethertrack.notification.dispatcher;

import com.aethertrack.core.domain.NotificationRule;
import com.aethertrack.core.domain.TokenCycle;
import com.aethertrack.core.domain.enums.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Dispatches notifications via SMTP email (MailHog in dev). */
@Component
public class EmailDispatcher implements NotificationDispatcher {

  private static final Logger log = LoggerFactory.getLogger(EmailDispatcher.class);

  private final JavaMailSender mailSender;

  public EmailDispatcher(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public NotificationChannel channel() {
    return NotificationChannel.EMAIL;
  }

  @Override
  public void dispatch(NotificationRule rule, TokenCycle cycle, String eventName) {
    try {
      var message = new SimpleMailMessage();
      message.setTo(rule.getTarget());
      message.setSubject("[AetherTrack] " + eventName);
      message.setText(buildBody(cycle, eventName));
      mailSender.send(message);
      log.info("Email notification sent to [{}] for event [{}]", rule.getTarget(), eventName);
    } catch (Exception e) {
      log.error("Failed to send email notification to [{}]: {}", rule.getTarget(), e.getMessage(), e);
      throw new RuntimeException("Email dispatch failed", e);
    }
  }

  private String buildBody(TokenCycle cycle, String eventName) {
    return String.format(
        """
        AetherTrack AI Notification
        ─────────────────────────────
        Event:   %s
        Service: %s
        Profile: %s
        State:   %s
        Used:    %d / %d tokens (%.1f%%)
        """,
        eventName,
        cycle.getProfile().getService().getName(),
        cycle.getProfile().getDisplayName(),
        cycle.getState(),
        cycle.getTokensUsed(),
        cycle.getTokenLimit(),
        cycle.getUsagePercent());
  }
}
