-- ============================================================
-- AetherTrack AI — Initial Schema
-- V1__init_schema.sql
-- ============================================================

-- ── AI Services ───────────────────────────────────────────────
CREATE TABLE ai_services (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   VARCHAR(500),
    icon_url      VARCHAR(500),
    accent_color  VARCHAR(7),
    api_docs_url  VARCHAR(500),
    metadata      JSONB,
    pricing_tier  VARCHAR(30) NOT NULL DEFAULT 'FREE',
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_services_name ON ai_services(name);
CREATE INDEX idx_ai_services_active ON ai_services(active);

-- ── Profiles ──────────────────────────────────────────────────
CREATE TABLE profiles (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name      VARCHAR(100) NOT NULL,
    encrypted_api_key TEXT NOT NULL,
    tag               VARCHAR(50),
    service_id        UUID NOT NULL REFERENCES ai_services(id) ON DELETE CASCADE,
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profiles_service_id ON profiles(service_id);
CREATE INDEX idx_profiles_tag ON profiles(tag);

-- ── Projects ──────────────────────────────────────────────────
CREATE TABLE projects (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── Project ↔ Service mapping (M:M) ───────────────────────────
CREATE TABLE project_service_mappings (
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES ai_services(id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, service_id)
);

-- ── Token Cycles ──────────────────────────────────────────────
CREATE TABLE token_cycles (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id             UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    state                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                               CHECK (state IN ('ACTIVE','EXHAUSTED','RENEWING')),
    token_limit            BIGINT NOT NULL,
    tokens_used            BIGINT NOT NULL DEFAULT 0,
    renewal_schedule_type  VARCHAR(20) NOT NULL
                               CHECK (renewal_schedule_type IN ('DAILY','WEEKLY','CUSTOM_CRON','MANUAL')),
    renewal_cron           VARCHAR(100),
    renewal_timezone       VARCHAR(60) NOT NULL DEFAULT 'UTC',
    cycle_started_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    cycle_expires_at       TIMESTAMPTZ,
    last_renewed_at        TIMESTAMPTZ,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_token_cycles_profile_id ON token_cycles(profile_id);
CREATE INDEX idx_token_cycles_state ON token_cycles(state);
CREATE INDEX idx_token_cycles_expires_at ON token_cycles(cycle_expires_at);

-- ── Usage Logs ────────────────────────────────────────────────
CREATE TABLE usage_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_cycle_id      UUID NOT NULL REFERENCES token_cycles(id) ON DELETE CASCADE,
    project_id          UUID REFERENCES projects(id) ON DELETE SET NULL,
    tokens_consumed     BIGINT NOT NULL,
    estimated_cost_usd  NUMERIC(12, 6),
    logged_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usage_logs_token_cycle_id ON usage_logs(token_cycle_id);
CREATE INDEX idx_usage_logs_project_id ON usage_logs(project_id);
CREATE INDEX idx_usage_logs_logged_at ON usage_logs(logged_at DESC);

-- ── Notification Rules ────────────────────────────────────────
CREATE TABLE notification_rules (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trigger               VARCHAR(40) NOT NULL
                              CHECK (trigger IN (
                                  'CYCLE_EXHAUSTED','CYCLE_RENEWED',
                                  'USAGE_THRESHOLD_REACHED','RENEWAL_IMMINENT'
                              )),
    channel               VARCHAR(20) NOT NULL
                              CHECK (channel IN ('EMAIL','WEBHOOK','SLACK','DISCORD','DESKTOP_PUSH')),
    target                TEXT NOT NULL,
    service_id            UUID REFERENCES ai_services(id) ON DELETE CASCADE,
    profile_id            UUID REFERENCES profiles(id) ON DELETE CASCADE,
    quiet_hours_start     SMALLINT CHECK (quiet_hours_start BETWEEN 0 AND 23),
    quiet_hours_end       SMALLINT CHECK (quiet_hours_end BETWEEN 0 AND 23),
    quiet_hours_timezone  VARCHAR(60),
    digest_mode           BOOLEAN NOT NULL DEFAULT FALSE,
    active                BOOLEAN NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notification_rules_trigger ON notification_rules(trigger);
CREATE INDEX idx_notification_rules_service_id ON notification_rules(service_id);
