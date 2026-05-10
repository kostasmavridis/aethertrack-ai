package com.aethertrack.core.repository;

import com.aethertrack.core.domain.UsageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link UsageLog} entities.
 *
 * <p>Optimised for analytics queries: per-cycle totals, per-project breakdowns,
 * time-series aggregations, and cost forecasting inputs.
 */
@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, UUID> {

  // ── By cycle ────────────────────────────────────────────────────────────

  Page<UsageLog> findAllByTokenCycleIdOrderByLoggedAtDesc(UUID tokenCycleId, Pageable pageable);

  @Query("""
      SELECT COALESCE(SUM(ul.tokensConsumed), 0) FROM UsageLog ul
      WHERE ul.tokenCycle.id = :cycleId
      """)
  Long sumTokensConsumedByCycleId(@Param("cycleId") UUID cycleId);

  // ── By project ──────────────────────────────────────────────────────────

  Page<UsageLog> findAllByProjectIdOrderByLoggedAtDesc(UUID projectId, Pageable pageable);

  @Query("""
      SELECT COALESCE(SUM(ul.tokensConsumed), 0) FROM UsageLog ul
      WHERE ul.project.id = :projectId
        AND ul.loggedAt >= :since
      """)
  Long sumTokensConsumedByProjectIdSince(
      @Param("projectId") UUID projectId,
      @Param("since") Instant since);

  @Query("""
      SELECT COALESCE(SUM(ul.estimatedCostUsd), 0.0) FROM UsageLog ul
      WHERE ul.project.id = :projectId
        AND ul.loggedAt >= :since
      """)
  Double sumCostByProjectIdSince(
      @Param("projectId") UUID projectId,
      @Param("since") Instant since);

  // ── Time-range queries (for trend charts) ───────────────────────────

  @Query("""
      SELECT ul FROM UsageLog ul
      WHERE ul.tokenCycle.id = :cycleId
        AND ul.loggedAt BETWEEN :from AND :to
      ORDER BY ul.loggedAt ASC
      """)
  List<UsageLog> findByCycleIdAndDateRange(
      @Param("cycleId") UUID cycleId,
      @Param("from") Instant from,
      @Param("to") Instant to);

  /**
   * Per-service token consumption over time — powers the trend chart.
   * Returns rows of [serviceId, day-bucket, total-tokens].
   */
  @Query(value = """
      SELECT
          s.id                           AS service_id,
          s.name                         AS service_name,
          DATE_TRUNC('day', ul.logged_at) AS day,
          SUM(ul.tokens_consumed)        AS total_tokens,
          SUM(ul.estimated_cost_usd)     AS total_cost
      FROM usage_logs ul
      JOIN token_cycles tc ON ul.token_cycle_id = tc.id
      JOIN profiles     pr ON tc.profile_id     = pr.id
      JOIN ai_services  s  ON pr.service_id     = s.id
      WHERE ul.logged_at >= :since
      GROUP BY s.id, s.name, DATE_TRUNC('day', ul.logged_at)
      ORDER BY day ASC, s.name ASC
      """, nativeQuery = true)
  List<Object[]> dailyTokensByService(@Param("since") Instant since);

  // ── Cost analytics ─────────────────────────────────────────────────────

  @Query("""
      SELECT COALESCE(SUM(ul.estimatedCostUsd), 0.0) FROM UsageLog ul
      WHERE ul.loggedAt >= :since
      """)
  Double totalCostSince(@Param("since") Instant since);

  @Query("""
      SELECT COALESCE(SUM(ul.tokensConsumed), 0) FROM UsageLog ul
      WHERE ul.loggedAt >= :since
      """)
  Long totalTokensSince(@Param("since") Instant since);
}
