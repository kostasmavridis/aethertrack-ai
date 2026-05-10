# 🌌 AetherTrack AI

> *Track intelligently. Renew calmly. Build without interruption.*

AetherTrack AI is a production-ready dashboard for developers who juggle multiple AI services. It tracks token cycles, renewal schedules, usage analytics, and fires real-time notifications so you never hit a wall mid-build.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 26 (records, virtual threads, pattern matching) |
| Framework | Spring Boot 4.0.x / Spring Framework 7 |
| Persistence | PostgreSQL 18.3 + Flyway + Hibernate 7 |
| Cache / Pub-Sub | Redis 7 |
| Scheduling | Quartz + Spring Scheduler (virtual-thread executor) |
| API | REST + OpenAPI 3.1 + Server-Sent Events |
| Security | Spring Security 7 + JWT + Jasypt |
| Observability | Micrometer 2.x + OpenTelemetry + Prometheus + Grafana |
| Frontend | React 18 + Vite + TailwindCSS + Radix UI |
| Containers | Docker Compose v2 |

## Project Structure

```
aethertrack-ai/
├── core/           Domain model, enums, state machine, shared utilities
├── api/            REST controllers, OpenAPI config, SSE endpoints
├── scheduler/      Quartz jobs, token-cycle renewal engine
├── notification/   Channel dispatchers (email, webhook, Slack, Discord)
├── ui/             React + Vite frontend (served via Nginx)
├── docker/         Docker Compose stack + observability configs
└── pom.xml         Multi-module Maven parent
```

## Quick Start

```bash
# 1. Clone
git clone https://github.com/kostasmavridis/aethertrack-ai.git
cd aethertrack-ai

# 2. Configure
cp .env.example .env
# Edit .env with your secrets

# 3. Spin up the full stack
docker compose -f docker/docker-compose.yml up -d

# 4. Build & run the app (dev)
./mvnw spring-boot:run -pl api -Dspring-boot.run.profiles=dev
```

## Services (Docker Compose)

| Service | Port | Description |
|---|---|---|
| `app` | 8080 | Spring Boot API |
| `postgres` | 5432 | PostgreSQL 18.3 |
| `redis` | 6379 | Redis 7 |
| `mailhog` | 1025/8025 | Dev mail catcher |
| `prometheus` | 9090 | Metrics scraper |
| `grafana` | 3001 | Dashboards |
| `nginx` | 80/443 | Reverse proxy + UI |

## Modules

### `core`
Pure domain — no Spring dependencies. Contains:
- JPA entities: `Service`, `Profile`, `Project`, `TokenCycle`, `UsageLog`, `NotificationRule`
- `TokenCycleState` enum + state-machine transitions
- Domain events: `TokenCycleRenewedEvent`, `TokenCycleExhaustedEvent`
- Shared value objects and utilities

### `api`
Spring Boot application entry point. Contains:
- REST controllers for all resources
- OpenAPI 3.1 configuration
- SSE event stream (`/api/events`)
- JWT filter chain
- Global exception handler

### `scheduler`
Quartz-backed renewal engine. Contains:
- `TokenCycleRenewalJob` — fires on cron, resets cycle state
- `SchedulerService` — CRUD for job triggers
- Timezone-aware scheduling, misfire handling

### `notification`
Pluggable notification channels. Contains:
- `NotificationDispatcher` interface
- `EmailDispatcher`, `WebhookDispatcher`, `SlackDispatcher`, `DiscordDispatcher`
- Retry with exponential back-off
- Quiet-hours and digest-mode logic

## License

MIT
