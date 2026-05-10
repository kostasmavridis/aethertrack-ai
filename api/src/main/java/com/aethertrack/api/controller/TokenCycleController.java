package com.aethertrack.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for TokenCycle management and manual overrides.
 */
@RestController
@RequestMapping("/api/v1/token-cycles")
@Tag(name = "Token Cycles", description = "Track and manage token quota cycles")
public class TokenCycleController {

  @GetMapping
  @Operation(summary = "List token cycles, optionally filtered by profile or state")
  public ResponseEntity<List<?>> listCycles(
      @RequestParam(required = false) UUID profileId,
      @RequestParam(required = false) String state) {
    return ResponseEntity.ok(List.of());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a single token cycle")
  public ResponseEntity<?> getCycle(@PathVariable UUID id) {
    return ResponseEntity.ok().build();
  }

  @PostMapping
  @Operation(summary = "Create a new token cycle for a profile")
  public ResponseEntity<?> createCycle(@RequestBody Object request) {
    return ResponseEntity.status(201).build();
  }

  @PostMapping("/{id}/renew")
  @Operation(summary = "Manually mark a token cycle as renewed (resets counter)")
  public ResponseEntity<?> manualRenew(@PathVariable UUID id) {
    // TODO: delegate to TokenCycleService → triggers domain event
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/usage")
  @Operation(summary = "Record token usage against a cycle")
  public ResponseEntity<?> recordUsage(@PathVariable UUID id, @RequestBody Object request) {
    return ResponseEntity.ok().build();
  }
}
