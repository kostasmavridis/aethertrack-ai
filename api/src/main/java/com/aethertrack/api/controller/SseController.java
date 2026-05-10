package com.aethertrack.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-Sent Events stream for real-time UI updates.
 *
 * <p>Clients subscribe to {@code /api/events} and receive:
 * <ul>
 *   <li>{@code token-cycle-exhausted} — when a cycle hits the limit</li>
 *   <li>{@code token-cycle-renewed}   — when a cycle resets</li>
 *   <li>{@code notification}          — when a notification is dispatched</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/events")
@Tag(name = "SSE", description = "Real-time Server-Sent Events stream")
public class SseController {

  private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "Subscribe to real-time token-cycle and notification events")
  public SseEmitter subscribe() {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    emitters.add(emitter);
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError(e -> emitters.remove(emitter));
    return emitter;
  }

  /** Called by domain event listeners to broadcast an event to all subscribers. */
  public void broadcast(String eventName, Object data) {
    var deadEmitters = new CopyOnWriteArrayList<SseEmitter>();
    emitters.forEach(emitter -> {
      try {
        emitter.send(SseEmitter.event().name(eventName).data(data));
      } catch (Exception e) {
        deadEmitters.add(emitter);
      }
    });
    emitters.removeAll(deadEmitters);
  }
}
