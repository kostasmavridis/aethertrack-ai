package com.aethertrack.core.repository;

import com.aethertrack.core.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Project} entities.
 *
 * <p>Projects have a many-to-many relationship with AiService via project_service_mappings.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

  // ── Basic lookups ──────────────────────────────────────────────────────

  Optional<Project> findByName(String name);

  boolean existsByName(String name);

  List<Project> findAllByActiveTrueOrderByNameAsc();

  // ── Service associations ─────────────────────────────────────────────

  @Query("""
      SELECT p FROM Project p
      JOIN p.services s
      WHERE s.id = :serviceId
        AND p.active = true
      ORDER BY p.name ASC
      """)
  List<Project> findActiveByServiceId(@Param("serviceId") UUID serviceId);

  @Query("""
      SELECT COUNT(p) FROM Project p
      JOIN p.services s
      WHERE s.id = :serviceId
        AND p.active = true
      """)
  long countActiveByServiceId(@Param("serviceId") UUID serviceId);

  // ── Search ───────────────────────────────────────────────────────────

  @Query("""
      SELECT p FROM Project p
      WHERE p.active = true
        AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))
      ORDER BY p.name ASC
      """)
  List<Project> searchByNameOrDescription(@Param("query") String query);

  // ── Stats ────────────────────────────────────────────────────────────

  long countByActiveTrue();
}
