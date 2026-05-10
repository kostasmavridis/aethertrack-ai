-- ============================================================
-- AetherTrack AI — Seed Data (dev only)
-- V2__seed_data.sql
-- ============================================================

-- Pre-populate well-known AI services so the registry isn't empty on first boot.
-- Prices and limits are illustrative — update via the UI.

INSERT INTO ai_services (id, name, description, icon_url, accent_color, api_docs_url, pricing_tier, active)
VALUES
  (
    gen_random_uuid(),
    'Anthropic Claude',
    'Claude by Anthropic — Sonnet, Haiku, and Opus models',
    'https://cdn.simpleicons.org/anthropic',
    '#D97706',
    'https://docs.anthropic.com',
    'PRO',
    TRUE
  ),
  (
    gen_random_uuid(),
    'Google Gemini',
    'Gemini 1.5 Pro and Flash by Google DeepMind',
    'https://cdn.simpleicons.org/google',
    '#4285F4',
    'https://ai.google.dev/docs',
    'FREE',
    TRUE
  ),
  (
    gen_random_uuid(),
    'OpenAI',
    'GPT-4o, GPT-4 Turbo, and o1 models',
    'https://cdn.simpleicons.org/openai',
    '#10A37F',
    'https://platform.openai.com/docs',
    'PAY_AS_YOU_GO',
    TRUE
  ),
  (
    gen_random_uuid(),
    'Cursor',
    'AI-first code editor with built-in Claude and GPT-4o',
    'https://cdn.simpleicons.org/cursor',
    '#7C3AED',
    'https://docs.cursor.com',
    'PRO',
    TRUE
  ),
  (
    gen_random_uuid(),
    'GitHub Copilot',
    'AI pair programmer integrated into editors and CLI',
    'https://cdn.simpleicons.org/githubcopilot',
    '#24292F',
    'https://docs.github.com/en/copilot',
    'PRO',
    TRUE
  ),
  (
    gen_random_uuid(),
    'Mistral AI',
    'Mistral Large and Mistral Small via La Plateforme',
    'https://cdn.simpleicons.org/mistral',
    '#FF7000',
    'https://docs.mistral.ai',
    'PAY_AS_YOU_GO',
    TRUE
  );
