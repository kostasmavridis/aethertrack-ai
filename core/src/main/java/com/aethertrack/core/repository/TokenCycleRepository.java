package com.aethertrack.core.repository;

import com.aethertrack.core.domain.TokenCycle;
import com.aethertrack.core.domain.enums.RenewalScheduleType;
import com.aethertrack.core.domain.enums.TokenCycleState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link TokenCycle} entities.
 *
 * <p>Contains scheduler-critical queries for finding cycles that are due for renewal
 * and analytics queries for the usage dashboard.
 */
@Repository
public interface TokenCycleRepository extends JpaRepository<TokenCycle, UUID> {

  // ── By profile ────────────────────────────────────────────────────────

  List<TokenCycle> findAllByProfileId(UUID profileId);

  List<TokenCycle> findAllByProfileIdAndState(UUID profileId, TokenCycleState state);

  // ── By state (scheduler uses these heavily) ────────────────────────

  List<TokenCycle> findAllByState(TokenCycleState state);

  long countByState(TokenCycleState state);

  // ── Scheduler — cycles due for renewal ──────────────────────────────

  /**
   * Finds all ACTIVE or EXHAUSTED cycles with a time-based expiry that has passed.
   * Used by the scheduler to trigger renewal for missed or overdue cycles.
   */
  @Query("""
      SELECT tc FROM TokenCycle tc
      WHERE tc.state IN ('ACTIVE', 'EXHAUSTED')
        AND tc.cycleExpiresAt IS NOT NULL
        AND tc.cycleExpiresAt <= :now
      """)
  List<TokenCycle> findCyclesDueForRenewal(@Param("now") Instant now);

  /**
   * Finds EXHAUSTED cycles with MANUAL renewal type —
   * these need a user action and should be surfaced in the dashboard.
   */
  @Query("""
      SELECT tc FROM TokenCycle tc
      WHERE tc.state = 'EXHAUSTED'
        AND tc.renewalScheduleType = 'MANUAL'
      ORDER BY tc.updatedAt DESC
      """)
  List<TokenCycle> findExhaustedManualCycles();

  /**
   * Finds all ACTIVE cycles by schedule type —
   * used when bootstrapping Quartz jobs on startup.
   */
  List<TokenCycle> findAllByStateAndRenewalScheduleType(
      TokenCycleState state, RenewalScheduleType renewalScheduleType);

  // ── Threshold alerts ────────────────────────────────────────────────

  /**
   * Finds ACTIVE cycles where usage has crossed a given percentage threshold.
   * Used by the notification engine to fire USAGE_THRESHOLD_REACHED alerts.
   *
   * @param thresholdPercent e.g. 0.8 for 80%
   */
  @Query("""
      SELECT tc FROM TokenCycle tc
      WHERE tc.state = 'ACTIVE'
        AND tc.tokenLimit > 0
        AND (CAST(tc.tokensUsed AS double) / CAST(tc.tokenLimit AS double)) >= :thresholdPercent
      ORDER BY tc.updatedAt DESC
      """)
  List<TokenCycle> findActiveCyclesAboveThreshold(@Param("thresholdPercent") double thresholdPercent);

  // ── Analytics ───────────────────────────────────────────────────────

  /**
   * Returns the total tokens used across all ACTIVE cycles for a given service.
   */
  @Query("""
      SELECT COALESCE(SUM(tc.tokensUsed), 0) FROM TokenCycle tc
      JOIN tc.profile p
      JOIN p.service s
      WHERE s.id = :serviceId
        AND tc.state = 'ACTIVE'
      """)
  Long sumTokensUsedByServiceId(@Param("serviceId") UUID serviceId);

  /**
   * Returns the total token limit across all ACTIVE cycles for a given service.
   */
  @Query("""
      SELECT COALESCE(SUM(tc.tokenLimit), 0) FROM TokenCycle tc
      JOIN tc.profile p
      JOIN p.service s
      WHERE s.id = :serviceId
        AND tc.state = 'ACTIVE'
      """)
  Long sumTokenLimitByServiceId(@Param("serviceId") UUID serviceId);
}
