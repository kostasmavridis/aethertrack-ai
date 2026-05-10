package com.aethertrack.core.repository;

import com.aethertrack.core.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Profile} entities.
 *
 * <p>Profiles represent API key accounts scoped to a specific AI service.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

  // ── By service ────────────────────────────────────────────────────────

  List<Profile> findAllByServiceId(UUID serviceId);

  List<Profile> findAllByServiceIdAndActiveTrue(UUID serviceId);

  List<Profile> findAllByActiveTrueOrderByDisplayNameAsc();

  // ── By tag ────────────────────────────────────────────────────────────

  List<Profile> findAllByTag(String tag);

  List<Profile> findAllByTagAndActiveTrue(String tag);

  @Query("""
      SELECT DISTINCT p.tag FROM Profile p
      WHERE p.tag IS NOT NULL
        AND p.active = true
      ORDER BY p.tag ASC
      """)
  List<String> findDistinctActiveTags();

  // ── Combined filters ──────────────────────────────────────────────────

  List<Profile> findAllByServiceIdAndTagAndActiveTrue(UUID serviceId, String tag);

  Optional<Profile> findByDisplayNameAndServiceId(String displayName, UUID serviceId);

  // ── Stats ────────────────────────────────────────────────────────────

  long countByServiceIdAndActiveTrue(UUID serviceId);

  long countByActiveTrue();
}
