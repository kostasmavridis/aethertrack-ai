package com.aethertrack.core.domain;

import com.aethertrack.core.domain.enums.RenewalScheduleType;
import com.aethertrack.core.domain.enums.TokenCycleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the TokenCycle domain state machine.
 * No Spring context — pure domain logic only.
 */
@DisplayName("TokenCycle State Machine")
class TokenCycleStateMachineTest {

  private TokenCycle cycle;

  @BeforeEach
  void setUp() {
    // Minimal cycle: 1000 token limit, manual renewal
    AiService service = new AiService("Test Service", com.aethertrack.core.domain.enums.PricingTier.FREE);
    Profile profile = new Profile("test-profile", "enc-key", service);
    cycle = new TokenCycle(profile, 1000L, RenewalScheduleType.MANUAL);
  }

  @Test
  @DisplayName("Initial state is ACTIVE")
  void initialStateIsActive() {
    assertThat(cycle.getState()).isEqualTo(TokenCycleState.ACTIVE);
    assertThat(cycle.getTokensUsed()).isZero();
    assertThat(cycle.getRemainingTokens()).isEqualTo(1000L);
  }

  @Test
  @DisplayName("Recording usage below limit keeps state ACTIVE")
  void recordUsageBelowLimitKeepsActive() {
    cycle.recordUsage(500);
    assertThat(cycle.getState()).isEqualTo(TokenCycleState.ACTIVE);
    assertThat(cycle.getTokensUsed()).isEqualTo(500L);
    assertThat(cycle.getRemainingTokens()).isEqualTo(500L);
    assertThat(cycle.getUsagePercent()).isEqualTo(50.0);
  }

  @Test
  @DisplayName("Hitting the token limit transitions to EXHAUSTED")
  void hittingLimitTransitionsToExhausted() {
    cycle.recordUsage(1000);
    assertThat(cycle.getState()).isEqualTo(TokenCycleState.EXHAUSTED);
    assertThat(cycle.getRemainingTokens()).isZero();
  }

  @Test
  @DisplayName("Exceeding the limit also transitions to EXHAUSTED")
  void exceedingLimitTransitionsToExhausted() {
    cycle.recordUsage(600);
    cycle.recordUsage(600);
    assertThat(cycle.getState()).isEqualTo(TokenCycleState.EXHAUSTED);
  }

  @Test
  @DisplayName("Recording usage on EXHAUSTED cycle throws IllegalStateException")
  void recordUsageOnExhaustedThrows() {
    cycle.recordUsage(1000);
    assertThatThrownBy(() -> cycle.recordUsage(1))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("non-ACTIVE");
  }

  @Test
  @DisplayName("Full renewal cycle: ACTIVE → EXHAUSTED → RENEWING → ACTIVE")
  void fullRenewalCycle() {
    cycle.recordUsage(1000);
    assertThat(cycle.getState()).isEqualTo(TokenCycleState.EXHAUSTED);

    cycle.beginRenewal();
    assertThat(cycle.getState()).isEqualTo(TokenCycleState.RENEWING);

    cycle.completeRenewal();
    assertThat(cycle.getState()).isEqualTo(TokenCycleState.ACTIVE);
    assertThat(cycle.getTokensUsed()).isZero();
    assertThat(cycle.getLastRenewedAt()).isNotNull();
    assertThat(cycle.getRemainingTokens()).isEqualTo(1000L);
  }

  @Test
  @DisplayName("beginRenewal on ACTIVE cycle throws IllegalStateException")
  void beginRenewalOnActiveThrows() {
    assertThatThrownBy(() -> cycle.beginRenewal())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("EXHAUSTED");
  }

  @Test
  @DisplayName("completeRenewal on EXHAUSTED cycle throws IllegalStateException")
  void completeRenewalOnExhaustedThrows() {
    cycle.recordUsage(1000);
    assertThatThrownBy(() -> cycle.completeRenewal())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("RENEWING");
  }
}
