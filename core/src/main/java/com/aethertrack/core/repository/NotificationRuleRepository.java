package com.aethertrack.core.repository;

import com.aethertrack.core.domain.NotificationRule;
import com.aethertrack.core.domain.enums.NotificationChannel;
import com.aethertrack.core.domain.enums.NotificationTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link NotificationRule} entities.
 *
 * <p>The notification engine uses {@link #findMatchingRules} as its primary
 * routing query — it resolves which rules should fire for a given event on a
 * specific service / profile combination.
 */
@Repository
public interface NotificationRuleRepository extends JpaRepository<NotificationRule, UUID> {

  // ── Basic active lookups ────────────────────────────────────────────────

  List<NotificationRule> findAllByActiveTrue();

  List<NotificationRule> findAllByTriggerAndActiveTrue(NotificationTrigger trigger);

  List<NotificationRule> findAllByChannelAndActiveTrue(NotificationChannel channel);

  // ── Routing query (core of the notification engine) ────────────────────

  /**
   * Resolves all active notification rules that match a given trigger + service + profile.
   *
   * <p>A rule matches when:
   * <ul>
   *   <li>The trigger matches exactly.</li>
   *   <li>The rule is scoped to this service, OR scoped to no service (global).</li>
   *   <li>The rule is scoped to this profile, OR scoped to no profile (global).</li>
   * </ul>
   *
   * @param trigger   the event that fired
   * @param serviceId the service involved in the event
   * @param profileId the profile involved in the event
   */
  @Query("""
      SELECT nr FROM NotificationRule nr
      WHERE nr.active = true
        AND nr.trigger = :trigger
        AND (nr.service IS NULL OR nr.service.id = :serviceId)
        AND (nr.profile IS NULL OR nr.profile.id = :profileId)
      ORDER BY nr.channel ASC
      """)
  List<NotificationRule> findMatchingRules(
      @Param("trigger")   NotificationTrigger trigger,
      @Param("serviceId") UUID serviceId,
      @Param("profileId") UUID profileId);

  // ── Scoped lookups ──────────────────────────────────────────────────

  List<NotificationRule> findAllByServiceIdAndActiveTrue(UUID serviceId);

  List<NotificationRule> findAllByProfileIdAndActiveTrue(UUID profileId);

  // ── Stats ────────────────────────────────────────────────────────────

  long countByTriggerAndActiveTrue(NotificationTrigger trigger);

  long countByChannelAndActiveTrue(NotificationChannel channel);
}
