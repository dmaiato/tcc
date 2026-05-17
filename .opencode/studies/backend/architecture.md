# SQLab Backend Architecture

## Overview

Spring Boot REST API with **hexagonal architecture** (ports & adapters). Java 25, Maven, PostgreSQL.

## Ports & Adapters Layout

```
ports/in/     → Use case interfaces (driving ports)
ports/out/    → Repository interfaces (driven ports)
usecase/      → Service implementations
domain/model/ → Domain entities
infrastructure/
  adapter/in/web/  → REST controllers + DTOs
  adapter/out/persistence/ → JPA adapters + mappers
  config/           → Security, CORS, JWT config
```

## Key Principles

| Principle | Rule |
|-----------|------|
| **Dependency direction** | Controller → UseCase (interface) ← Service. Controller never directly depends on Service or Repository. |
| **DTOs in web layer** | `dto/` package under `adapter/in/web/`. Domain models never leak into controllers. |
| **Domain exceptions** | Custom exceptions in `domain/exception/` — handled by `GlobalExceptionHandler`. |
| **Mappers** | `*Mapper.java` in `adapter/out/persistence/mapper/` — JPA entity ↔ domain model. |

## Domain Model

### Mission
- Fields: id, title, briefing, objective, hint, ddlScript, dmlScript, expectedResult, theme, difficulty, xpReward, techniques, scenarioId (nullable), orderIndex (nullable), scenarioTitle (transient, populated by service)
- `@AllArgsConstructor` (Lombok — `@RequiredArgsConstructor` removed due to mixed final/non-final constructor resolution conflict)

### Scenario
- Fields: id, title, description, theme, createdAt
- No `orderIndex` on Scenario — ordering is per-mission via `missions.order_index`

### User
- Fields: id, username, email, password, xp, role, createdAt
- Level computed via `floor(sqrt(xp / 100)) + 1` (server-authoritative, not stored)

### Progress
- Fields: id, userId, missionId, completed, completedAt
- Composite unique: one progress row per user-mission pair

## Database Schema

### Tables
- `users` — id UUID PK, username, email, password, xp, role, created_at
- `missions` — id UUID PK, title, briefing, objective, hint, ddl_script, dml_script, expected_result JSONB, theme VARCHAR(20), difficulty VARCHAR(20), xp_reward INT, techniques TEXT[], scenario_id UUID FK → scenarios(id) ON DELETE CASCADE, order_index INT
  - `CHECK (scenario_id IS NULL OR order_index IS NOT NULL)` — prevents orphaned order_index
  - `uq_scenario_order` was removed — CHECK constraint + app logic is sufficient
- `scenarios` — id UUID PK, title VARCHAR(100), description TEXT, theme VARCHAR(20), created_at TIMESTAMP
- `progress` — id UUID PK, user_id UUID, mission_id UUID, completed BOOLEAN, completed_at TIMESTAMP
- ON DELETE CASCADE: deleting a scenario cascades to linked missions

## API Structure

### Public API (`/api/`)
| Prefix | Purpose |
|--------|---------|
| `/api/auth/*` | Register, login, logout, refresh tokens |
| `/api/missions` | List missions, get mission detail (+admin variant at `/admin`), validate solution |
| `/api/scenarios` | List scenarios, get scenario detail (public, with per-user progress) |
| `/api/users/me` | Profile, progress, skills |

### Admin API (`/api/admin/`)
| Prefix | Purpose |
|--------|---------|
| `/api/admin/missions` | CRUD missions (create, update, delete) |
| `/api/admin/scenarios` | CRUD scenarios + reorder missions |

Errors: `MissionLockedException` → 403 with `{ code: "MISSION_LOCKED", scenarioId }`. `ScenarioNotFoundException` → 404.

## Security

### JWT Flow
1. Login/register → backend returns `{ token, id, username, email }`
2. Frontend stores `token` in localStorage
3. `JwtAuthFilter` extracts Bearer token, validates, sets `Authentication` in SecurityContext
4. `JwtAuthenticationEntryPoint` returns 401 JSON for invalid/expired tokens
5. Admin routes checked via `ADMIN` role in `SecurityConfig`

### Security Config
- CORS allows `http://localhost:4200`
- `/api/auth/**` and `/api/missions/**` (GET) are public
- Admin endpoints require `ADMIN` role
- CSRF disabled (token-based auth)

## Key Patterns

### Two-phase Reorder (Scenario Missions)
To avoid unique constraint violation when swapping adjacent indices (e.g., 1→2 and 2→1 simultaneously):
1. Set all indices to negative values: `-(index + 1)` → temporarily no duplicates
2. Flush to DB
3. Set to correct positive indices
4. All within one `@Transactional` method

### Batch Progress Query
Instead of N+1 per mission: single `findCompletedMissionIdsByUserId(userId)` returns a `Set<UUID>`, used to compute per-mission lock/complete status.

### Auto-calculate orderIndex
When creating a mission with `scenarioId` but no `orderIndex`: `ManageMissionService.create()` calls `countByScenarioId(scenarioId) + 1`.

### save() preserves createdAt
`MissionPersistenceAdapter.save()` reads existing `createdAt` from DB before merge to prevent Hibernate overwriting it with null on update.

## Seed Data
- `V1__init_schema.sql` — Tables + constraints + indexes
- `V2__seed_missions.sql` — 10 missions across 5 themes (CRIMINAL×3, FINANCE×2, ASTRONOMY×2, CYBERSECURITY×1, BIOLOGY×2) + seed scenario "Noite no Blue Moon"
- `V3__seed_admin.sql` — Admin user (not yet applied)
