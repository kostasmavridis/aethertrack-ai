package com.aethertrack.scheduler.job;

import com.aethertrack.core.event.TokenCycleRenewedEvent;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Quartz job that fires when a token cycle's renewal schedule triggers.
 *
 * <p>Flow:
 * <ol>
 *   <li>Read {@code tokenCycleId} from the job data map.</li>
 *   <li>Load the TokenCycle via a repository (injected at runtime).</li>
 *   <li>Call {@code tokenCycle.beginRenewal()} then {@code tokenCycle.completeRenewal()}.</li>
 *   <li>Persist the updated state.</li>
 *   <li>Publish {@link TokenCycleRenewedEvent} → SSE broadcast + notifications.</li>
 * </ol>
 */
@Component
public class TokenCycleRenewalJob implements Job {

  private static final Logger log = LoggerFactory.getLogger(TokenCycleRenewalJob.class);
  public static final String TOKEN_CYCLE_ID_KEY = "tokenCycleId";

  private final ApplicationEventPublisher eventPublisher;

  public TokenCycleRenewalJob(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    JobDataMap dataMap = context.getMergedJobDataMap();
    String cycleIdStr = dataMap.getString(TOKEN_CYCLE_ID_KEY);

    if (cycleIdStr == null) {
      log.warn("TokenCycleRenewalJob fired without a tokenCycleId — skipping.");
      return;
    }

    UUID cycleId = UUID.fromString(cycleIdStr);
    log.info("Renewing token cycle [{}]", cycleId);

    // TODO: inject TokenCycleRepository, load cycle, call beginRenewal() + completeRenewal(), save
    // eventPublisher.publishEvent(new TokenCycleRenewedEvent(this, tokenCycle));
  }
}
