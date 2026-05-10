-- ============================================================
-- AetherTrack AI — Users table
-- V3__users_table.sql
-- ============================================================

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(60)  NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash TEXT         NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER'
                      CHECK (role IN ('USER', 'ADMIN')),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email    ON users(email);

-- Optional: seed a default admin for dev (password: admin1234 — change immediately)
INSERT INTO users (username, email, password_hash, role)
VALUES (
  'admin',
  'admin@aethertrack.dev',
  -- BCrypt(12) of "admin1234" — MUST be rotated before production use
  '$2a$12$1InE4ycJBOTiDfb9MRNL5e5Z2Y0/MmVIMbFrqkzXfpJ0yNsRlajfi',
  'ADMIN'
);
