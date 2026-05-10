package com.aethertrack.api.dto;

/** Request payload for recording token usage. */
public record RecordUsageRequest(long tokensConsumed) {}
