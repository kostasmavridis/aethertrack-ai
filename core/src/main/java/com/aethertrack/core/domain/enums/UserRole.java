package com.aethertrack.core.domain.enums;

/** Authority roles for the AetherTrack application. */
public enum UserRole {
  /** Standard user — can manage their own services, profiles, and cycles. */
  USER,

  /** Administrator — full read/write across all resources. */
  ADMIN
}
