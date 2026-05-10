package com.aethertrack.api.controller;

import com.aethertrack.api.dto.CreateAiServiceRequest;
import com.aethertrack.api.dto.UpdateAiServiceRequest;
import com.aethertrack.api.service.AiServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for AI Service registry management.
 */
@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Services", description = "Manage registered AI providers")
public class ServiceController {

  private final AiServiceService aiServiceService;

  public ServiceController(AiServiceService aiServiceService) {
    this.aiServiceService = aiServiceService;
  }

  @GetMapping
  @Operation(summary = "List all registered AI services")
  public ResponseEntity<List<?>> listServices(@RequestParam(required = false) String q) {
    return ResponseEntity.ok(q == null ? aiServiceService.listActive() : aiServiceService.search(q));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a single AI service by ID")
  public ResponseEntity<?> getService(@PathVariable UUID id) {
    return ResponseEntity.ok(aiServiceService.getById(id));
  }

  @PostMapping
  @Operation(summary = "Register a new AI service")
  public ResponseEntity<?> createService(@RequestBody CreateAiServiceRequest request) {
    var created = aiServiceService.create(
        request.name(),
        request.description(),
        request.iconUrl(),
        request.accentColor(),
        request.apiDocsUrl(),
        request.metadata(),
        request.pricingTier());
    return ResponseEntity.created(URI.create("/api/v1/services/" + created.getId())).body(created);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing AI service")
  public ResponseEntity<?> updateService(@PathVariable UUID id, @RequestBody UpdateAiServiceRequest request) {
    return ResponseEntity.ok(aiServiceService.update(
        id,
        request.name(),
        request.description(),
        request.iconUrl(),
        request.accentColor(),
        request.apiDocsUrl(),
        request.metadata(),
        request.pricingTier(),
        request.active()));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remove an AI service")
  public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
    aiServiceService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
