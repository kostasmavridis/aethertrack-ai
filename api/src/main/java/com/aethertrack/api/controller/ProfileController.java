package com.aethertrack.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for Profile (API key account) management.
 */
@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Profiles", description = "Manage API key accounts per service")
public class ProfileController {

  @GetMapping
  @Operation(summary = "List profiles, optionally filtered by service")
  public ResponseEntity<List<?>> listProfiles(
      @RequestParam(required = false) UUID serviceId) {
    return ResponseEntity.ok(List.of());
  }

  @PostMapping
  @Operation(summary = "Create a new profile")
  public ResponseEntity<?> createProfile(@RequestBody Object request) {
    return ResponseEntity.status(201).build();
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a profile")
  public ResponseEntity<?> updateProfile(@PathVariable UUID id, @RequestBody Object request) {
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a profile")
  public ResponseEntity<Void> deleteProfile(@PathVariable UUID id) {
    return ResponseEntity.noContent().build();
  }
}
