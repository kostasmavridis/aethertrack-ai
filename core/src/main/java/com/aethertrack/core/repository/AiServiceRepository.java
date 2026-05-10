package com.aethertrack.core.repository;

import com.aethertrack.core.domain.AiService;
import com.aethertrack.core.domain.enums.PricingTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link AiService} entities.
 *
 * <p>Provides standard CRUD plus domain-specific queries for the service registry.
 */
@Repository
public interface AiServiceRepository extends JpaRepository<AiService, UUID> {

  // ── Basic lookups ──────────────────────────────────────────────────────

  Optional<AiService> findByName(String name);

  boolean existsByName(String name);

  // ── Active filter ─────────────────────────────────────────────────────

  List<AiService> findAllByActiveTrue();

  List<AiService> findAllByActiveTrueOrderByNameAsc();

  // ── Pricing tier ────────────────────────────────────────────────────

  List<AiService> findAllByPricingTier(PricingTier pricingTier);

  List<AiService> findAllByPricingTierAndActiveTrue(PricingTier pricingTier);

  // ── Search ───────────────────────────────────────────────────────────

  @Query("""
      SELECT s FROM AiService s
      WHERE s.active = true
        AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')))
      ORDER BY s.name ASC
      """)
  List<AiService> searchByNameOrDescription(@Param("query") String query);

  // ── Project associations ─────────────────────────────────────────────

  @Query("""
      SELECT s FROM AiService s
      JOIN s.projects p
      WHERE p.id = :projectId
        AND s.active = true
      ORDER BY s.name ASC
      """)
  List<AiService> findActiveByProjectId(@Param("projectId") UUID projectId);

  // ── Stats ────────────────────────────────────────────────────────────

  long countByActiveTrue();

  long countByPricingTierAndActiveTrue(PricingTier pricingTier);
}
