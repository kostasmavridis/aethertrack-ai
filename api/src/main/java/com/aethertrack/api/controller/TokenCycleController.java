package com.aethertrack.api.controller;

import com.aethertrack.api.dto.CreateTokenCycleRequest;
import com.aethertrack.api.dto.RecordUsageRequest;
import com.aethertrack.api.service.TokenCycleService;
import com.aethertrack.core.domain.enums.TokenCycleState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for TokenCycle management and manual overrides.
 */
@RestController
@RequestMapping("/api/v1/token-cycles")
@Tag(name = "Token Cycles", description = "Track and manage token quota cycles")
public class TokenCycleController {

  private final TokenCycleService tokenCycleService;

  public TokenCycleController(TokenCycleService tokenCycleService) {
    this.tokenCycleService = tokenCycleService;
  }

  @GetMapping
  @Operation(summary = "List token cycles, optionally filtered by profile or state")
  public ResponseEntity<List<?>> listCycles(
      @RequestParam(required = false) UUID profileId,
      @RequestParam(required = false) TokenCycleState state) {
    return ResponseEntity.ok(tokenCycleService.list(profileId, state));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a single token cycle")
  public ResponseEntity<?> getCycle(@PathVariable UUID id) {
    return ResponseEntity.ok(tokenCycleService.getById(id));
  }

  @PostMapping
  @Operation(summary = "Create a new token cycle for a profile")
  public ResponseEntity<?> createCycle(@RequestBody CreateTokenCycleRequest request) {
    var created = tokenCycleService.create(
        request.profileId(),
        request.tokenLimit(),
        request.renewalScheduleType(),
        request.renewalCron(),
        request.renewalTimezone(),
        request.cycleExpiresAt());
    return ResponseEntity.created(URI.create("/api/v1/token-cycles/" + created.getId())).body(created);
  }

  @PostMapping("/{id}/renew")
  @Operation(summary = "Manually mark a token cycle as renewed (resets counter)")
  public ResponseEntity<?> manualRenew(@PathVariable UUID id) {
    return ResponseEntity.ok(tokenCycleService.manualRenew(id));
  }

  @PostMapping("/{id}/usage")
  @Operation(summary = "Record token usage against a cycle")
  public ResponseEntity<?> recordUsage(@PathVariable UUID id, @RequestBody RecordUsageRequest request) {
    return ResponseEntity.ok(tokenCycleService.recordUsage(id, request.tokensConsumed()));
  }
}
