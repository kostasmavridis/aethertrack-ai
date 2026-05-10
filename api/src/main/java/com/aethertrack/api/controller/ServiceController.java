package com.aethertrack.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for AI Service registry management.
 *
 * <p>Full CRUD — add, update, remove providers and their metadata.
 */
@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Services", description = "Manage registered AI providers")
public class ServiceController {

  @GetMapping
  @Operation(summary = "List all registered AI services")
  public ResponseEntity<List<?>> listServices() {
    // TODO: inject and delegate to ServiceService
    return ResponseEntity.ok(List.of());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a single AI service by ID")
  public ResponseEntity<?> getService(@PathVariable UUID id) {
    // TODO: delegate to ServiceService
    return ResponseEntity.ok().build();
  }

  @PostMapping
  @Operation(summary = "Register a new AI service")
  public ResponseEntity<?> createService(@RequestBody Object request) {
    // TODO: validate + delegate to ServiceService
    return ResponseEntity.status(201).build();
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing AI service")
  public ResponseEntity<?> updateService(@PathVariable UUID id, @RequestBody Object request) {
    // TODO: delegate to ServiceService
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remove an AI service")
  public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
    // TODO: delegate to ServiceService
    return ResponseEntity.noContent().build();
  }
}
