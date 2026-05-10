package com.aethertrack.api.controller;

import com.aethertrack.api.dto.CreateProfileRequest;
import com.aethertrack.api.dto.UpdateProfileRequest;
import com.aethertrack.api.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for Profile (API key account) management.
 */
@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Profiles", description = "Manage API key accounts per service")
public class ProfileController {

  private final ProfileService profileService;

  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping
  @Operation(summary = "List profiles, optionally filtered by service")
  public ResponseEntity<List<?>> listProfiles(
      @RequestParam(required = false) UUID serviceId,
      @RequestParam(required = false) String tag) {
    return ResponseEntity.ok(profileService.listActive(serviceId, tag));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a single profile by ID")
  public ResponseEntity<?> getProfile(@PathVariable UUID id) {
    return ResponseEntity.ok(profileService.getById(id));
  }

  @PostMapping
  @Operation(summary = "Create a new profile")
  public ResponseEntity<?> createProfile(@RequestBody CreateProfileRequest request) {
    var created = profileService.create(
        request.serviceId(),
        request.displayName(),
        request.apiKey(),
        request.tag());
    return ResponseEntity.created(URI.create("/api/v1/profiles/" + created.getId())).body(created);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a profile")
  public ResponseEntity<?> updateProfile(@PathVariable UUID id, @RequestBody UpdateProfileRequest request) {
    return ResponseEntity.ok(profileService.update(
        id,
        request.displayName(),
        request.apiKey(),
        request.tag(),
        request.active()));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a profile")
  public ResponseEntity<Void> deleteProfile(@PathVariable UUID id) {
    profileService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
