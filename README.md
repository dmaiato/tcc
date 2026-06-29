# SQLab

SQLab is an interactive educational platform for learning SQL. Users solve challenges ("missions") by writing SQL queries that run in a browser-based PostgreSQL database (PGlite), submit results to a backend for validation, and earn XP while progressing through thematic scenarios.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Angular 21, Tailwind CSS v4, PGlite (WebAssembly), @ng-icons/lucide, @angular/cdk |
| Backend | Spring Boot 4.0.5, Java 25, Maven, Flyway, JWT (jjwt 0.12.6), BCrypt |
| Database | PostgreSQL 16-alpine |
| Infrastructure | Docker, docker-compose |

## Prerequisites

- **Docker** and **Docker Compose** (for full stack)
- **Java 25** (Zulu OpenJDK) and **Node 22** (for development without Docker)

## Quick Start (Docker)

```bash
# Clone and enter the project
cp .env .env.local   # edit if needed
docker compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:4200 |
| API | http://localhost:8081 |
| Database | localhost:5436 |

> **Note:** The admin seed (`V3__seed_admin.sql`) is not applied automatically.
> To create an admin user, connect to the database and run:
> ```sql
> INSERT INTO users (id, username, email, password_hash, xp, role, created_at)
> VALUES ('00000000-0000-0000-0000-000000000000', 'admin', 'admin@sqlab.com',
>         '$2a$10$0YTw968Ija.MHjNGP8Zkbevt4xVPGONIpf4LQIzvnu/c8enHuYyDi',
>         0, 'ADMIN', NOW())
> ON CONFLICT (email) DO NOTHING;
> ```

### Reset Everything

```bash
docker compose down -v && docker compose up -d --build
```

## Development (without Docker)

### Backend

Requires PostgreSQL running on port 5436 with database `sqlab`.

```bash
cd sqlab-api
./mvnw spring-boot:run
```

### Frontend

```bash
cd sqlab-client
npm install
npm run start
```

Opens at http://localhost:4200. Proxies API calls to http://localhost:8081.

## Tests

### Backend

```bash
cd sqlab-api
./mvnw test
```

- Framework: JUnit 5 + Spring Security Test + Testcontainers
- Database: H2 in PostgreSQL mode (in-memory, Flyway disabled)
- ~45 test files across domain, application, and infrastructure layers
- **Coverage:** JaCoCo 0.8.15 with thresholds: Instruction 70%, Branch 60%, Line 70% — report at `target/site/jacoco/index.html`

### Frontend — Unit Tests

```bash
cd sqlab-client
ng test
```

- Runner: Vitest (via `@angular/build:unit-test` builder)
- Environment: jsdom
- Includes: `src/**/*.spec.ts` (excludes `*.integration.spec.ts`)
- ~18 spec files (services, guards, interceptors, utils, components)
- **Coverage thresholds:**

| Metric | Threshold |
|--------|-----------|
| Statements | 70% |
| Branches | 60% |
| Functions | 70% |
| Lines | 70% |

### Frontend — PGlite Integration Tests

```bash
cd sqlab-client
npm run test:pglite
```

- Runs directly with Vitest (bypasses Angular CLI)
- Environment: **Node** (PGlite requires Node/Bun — does not work in jsdom)
- Config: `vitest.config.node.ts`
- Includes: `src/**/*.integration.spec.ts`
- 7 tests covering in-browser PostgreSQL operations (CRUD, schema inspection, PK detection)

## Configuration

Environment variables (`.env`):

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USER` | `sqlab` | PostgreSQL user |
| `DB_PASSWORD` | `sqlab` | PostgreSQL password |
| `DB_NAME` | `sqlab` | PostgreSQL database name |
| `SQLAB_JWT_SECRET` | *(long random string)* | JWT signing key (min 32 chars) |
| `SQLAB_JWT_EXPIRATION` | `86400000` | JWT expiry in ms (24h) |

## Project Structure

```
tcc/
├── sqlab-api/               # Backend — Spring Boot
│   ├── src/
│   │   ├── main/java/com/sqlab/
│   │   │   ├── domain/           # Entities, exceptions, enums
│   │   │   ├── application/      # Use cases (ports/in + usecase/) + repositories (ports/out)
│   │   │   └── infrastructure/   # Controllers, DTOs, persistence, security
│   │   └── resources/
│   │       └── db/migration/     # Flyway: V1 (schema), V2 (seed data), V3 (admin)
│   ├── Dockerfile
│   └── pom.xml
├── sqlab-client/             # Frontend — Angular
│   ├── src/app/
│   │   ├── core/             # Services, guards, interceptors, models
│   │   ├── features/         # Pages (dashboard, mission, scenario, admin, profile, auth)
│   │   └── shared/           # Header, toast, code-editor-dialog, confirm-dialog
│   ├── Dockerfile
│   ├── angular.json
│   ├── vitest.config.ts
│   └── package.json
├── docker-compose.yaml       # Full stack orchestration
└── .env                      # Default environment variables
```

## API Overview

| Prefix | Access | Description |
|--------|--------|-------------|
| `POST /api/auth/register` | Public | Register |
| `POST /api/auth/login` | Public | Login, returns JWT |
| `GET /api/missions` | Auth | List missions (filter by theme, difficulty) |
| `GET /api/missions/{id}` | Auth | Mission detail (403 if locked) |
| `POST /api/missions/{id}/validate` | Auth | Submit query result `{tuples: [...]}` → `{correct, feedback?}` |
| `GET /api/scenarios` | Auth | List scenarios with progress |
| `GET /api/scenarios/{id}` | Auth | Scenario detail with mission statuses |
| `GET /api/users/me` | Auth | Profile (id, username, email, xp, level) |
| `GET /api/users/me/progress` | Auth | Mission completion history |
| `GET /api/users/me/skills` | Auth | Aggregated technique tags |
| `POST /api/admin/missions` | Admin | Create mission |
| `PUT /api/admin/missions/{id}` | Admin | Update mission |
| `DELETE /api/admin/missions/{id}` | Admin | Delete mission |
| `POST /api/admin/scenarios` | Admin | Create scenario |
| `PUT /api/admin/scenarios/{id}` | Admin | Update scenario |
| `DELETE /api/admin/scenarios/{id}` | Admin | Delete scenario |
| `PUT /api/admin/scenarios/{id}/missions/reorder` | Admin | Reorder scenario missions |

## Seed Data

- **10 missions** across 5 themes (CRIMINAL×3, FINANCE×2, ASTRONOMY×2, CYBERSECURITY×1, BIOLOGY×2)
- **2 scenarios** with sequential unlocking:
  - "Noite no Blue Moon" — 3 missions, requires level 2
  - "The Mendes & Sons Affair" — 2 missions, requires level 3
- **Admin user** — available via `V3__seed_admin.sql` (see Quick Start note)

## Architecture

- **Backend**: Hexagonal architecture (ports & adapters) — controllers depend on use case interfaces, never on services or repositories directly. Domain entities are isolated from JPA and DTOs.
- **Frontend**: Standalone Angular components (no NgModules). Local state via `signal()` / `computed()`, lazy-loaded routes with `loadComponent`, signals-based inputs (`input()`) preferred over `@Input()`.
- **Validation**: In-browser PostgreSQL via PGlite (WebAssembly). User queries run locally, then result tuples are sent to the backend for comparison against expected output (ordered or unordered matching with detailed feedback).
