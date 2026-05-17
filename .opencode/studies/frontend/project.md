# SQLab - Project Context

## Overview

**SQLab** is an educational platform for learning SQL through interactive missions. Users solve SQL challenges by writing queries that execute in a browser-based PostgreSQL database (PGlite), then submit results to a backend for validation.

## Architecture

### Frontend

| Aspect | Technology |
|--------|-----------|
| Framework | Angular 21 |
| Port | localhost:4200 |
| Browser Database | PGlite (PostgreSQL in WebAssembly) |
| State Management | Angular Signals |
| Routing | Angular Router with guards |
| HTTP | HttpClient with interceptors |
| Drag & Drop | @angular/cdk drag-drop |

### Backend

| Aspect | Technology |
|--------|-----------|
| Framework | Spring Boot (Java) |
| Port | localhost:8081 |
| Authentication | JWT with refresh tokens |
| Database | PostgreSQL |

## Key Features

### 1. Mission-Based Learning
- Missions organized by category
- Difficulty levels (beginner, intermediate, advanced)
- Users choose freely which missions to attempt

### 2. In-Browser SQL Execution
- Users write SQL queries in a code editor
- Queries execute directly in PGlite (browser sandbox)
- Immediate feedback on query results

### 3. Backend Validation
- Submit result tuples to backend
- Backend checks if correct data was fetched
- Progress tracking across missions

### 4. User Authentication
- JWT-based authentication
- Refresh token rotation
- Route protection for authenticated areas

## User Flow

1. **Register/Login** → Get JWT access token + refresh token
2. **Browse Missions** → View available missions by category/difficulty
3. **Select Mission** → See mission description and expected results
4. **Write SQL** → Execute queries in browser PGlite
5. **Submit Solution** → Backend validates the SQL results
6. **Track Progress** → Backend records completed missions

## API Endpoints (Expected)

### Authentication

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/register` | POST | Register new user |
| `/api/auth/login` | POST | Login, receive tokens |
| `/api/auth/refresh` | POST | Refresh access token |
| `/api/auth/logout` | POST | Logout |

### Scenarios

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/scenarios` | GET | List all scenarios with per-user progress |
| `/api/scenarios/:id` | GET | Get scenario detail with per-user mission status |

### Missions

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/missions` | GET | List all missions |
| `/api/missions/:id` | GET | Get mission details (403 if locked via scenario) |
| `/api/missions/:id/validate` | POST | Submit SQL results (403 if locked). Response: `{ correct: boolean, feedback?: string }` |

### User Progress

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/users/me` | GET | Get current user profile (id, username, email, xp, level, createdAt) |
| `/api/users/me/progress` | GET | Get completed missions. Response: `[{ missionId, completed, completedAt, missionTitle, scenarioId?, scenarioTitle? }]` |
| `/api/users/me/skills` | GET | Get aggregated skill tags |

### Admin — Missions

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/admin/missions` | POST | Create mission |
| `/api/admin/missions/{id}` | PUT | Update mission |
| `/api/admin/missions/{id}` | DELETE | Delete mission |
| `/api/missions/{id}/admin` | GET | Get full mission detail for editing |

### Admin — Scenarios

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/admin/scenarios` | POST | Create scenario |
| `/api/admin/scenarios/{id}` | PUT | Update scenario |
| `/api/admin/scenarios/{id}` | DELETE | Delete scenario |
| `/api/admin/scenarios/{id}` | GET | Get scenario admin detail with missions |
| `/api/admin/scenarios/{scenarioId}/missions/reorder` | PUT | Reorder missions (accepts `{ missionIds: UUID[] }`) |

## Tech Stack Summary

```
Frontend (Angular 21)
├── @angular/core (Signals, DI)
├── @angular/router (Routing, Guards)
├── @angular/common/http (HttpClient, Interceptors)
└── @electric-sql/pglite (Browser PostgreSQL)

Backend (Spring Boot)
├── Spring Security (JWT)
├── Spring Data JPA
└── PostgreSQL
```

## Security Considerations

1. **Token Storage**: Access token in localStorage (needed for API calls), refresh token in localStorage or httpOnly cookie
2. **HTTPS**: Required in production
3. **Token Expiry**: Access token ~24 hours, Refresh token ~7 days
4. **Route Guards**: Protect authenticated routes from unauthenticated access

## Development Phases

### Phase 1: Authentication ✅ COMPLETED
- Login/Register pages
- JWT pipeline with refresh tokens
- Route protection

### Phase 2: Mission Listing & Profile ✅ COMPLETED
- Mission browser with theme/difficulty filtering
- Mission workbench (SQL editor, schema explorer, results pane)
- User profile page with XP bar, stats, skills, progress table
- Header stats wired to real API data

### Phase 3: Solution Validation ✅ COMPLETED
- PGlite integration for in-browser SQL execution
- Result submission to backend
- Backend validation with ordered/unordered matching
- XP rewards on correct submission

### Phase 4: Scenario Mechanic ✅ COMPLETED
- Scenarios table + FK on missions with order_index
- Batch progress queries (eliminated N+1)
- Sequential unlocking backend enforcement (403 MISSION_LOCKED)
- Scenario list/detail pages with status icons
- Scenario-aware mission navigation (prev/next within scenario)
- Lock screen for direct URL access to locked missions

### Phase 5: Detailed Validation & Design Fidelity ✅ COMPLETED
- Dashboard design aligned with STYLING_GUIDE_v3 (container, badges, XP, icons)
- Validation returns detailed feedback (row count, column mismatch, order, value errors)
- NUMERIC type comparison via BigDecimal (fixes PGlite string vs JSONB number mismatch)
- Results pane layout with fixed inner border + scrolling data
- Data viewer with type-specific icons (hash, type, toggle, calendar, fingerprint, code, key for PK/FK)
- Theme emoji icons on dashboard mission cards
- Global Ctrl+Enter for query execution
- Profile shows mission names + scenario badges from API
- Mission page loading with clear phases (Loading mission... → Initializing database...)

### Phase 6: Admin Panel CRUD ✅ COMPLETED
- Admin Dashboard at `/admin` with cards linking to Mission Control and Scenario Control
- Mission CRUD (create/edit/delete) with accordion form sections (Details, DDL, DML, Expected Result)
- Scenario CRUD (create/edit/delete) with missions drag-drop reorder
- Backend hexagonal architecture with `ManageMissionUseCase`/`ManageScenarioUseCase`
- Admin endpoints under `/api/admin/` prefix (separate from public API)
- Flat routes for all admin pages (no Angular child routes)
- Two-phase reorder (negative temp indices → correct indices) to avoid unique constraint violations
- `CodeEditorDialogComponent` — terminal-style fullscreen modal for DDL/DML/ExpectedResult
- `ConfirmDialogComponent` — reusable alert modal with destructive theme tokens
- `adminGuard` for role-based access (requires ADMIN role)
- Scenario-backed mission creation (`?scenarioId=` query param pre-fills theme, auto-calculates orderIndex)

## Implemented Features

### Core Services
| File | Purpose |
|------|---------|
| `src/environments/environment.ts` | API config (http://localhost:8081/api) |
| `src/app/core/models/user.model.ts` | User, UserResponse (id, username, email, xp, level, createdAt) |
| `src/app/core/models/auth-response.model.ts` | LoginRequest, RegisterRequest, AuthResponse DTOs |
| `src/app/core/models/mission.model.ts` | Mission, MissionSummary, MissionProgress, MissionAdminDetail, ScenarioDetail, ScenarioMissionItem, ScenarioSummary |
| `src/app/core/models/scenario.model.ts` | ScenarioSummary, ScenarioAdminDetail, ScenarioMissionSummary, CreateScenarioRequest, UpdateScenarioRequest, ReorderMissionsRequest |
| `src/app/core/api.service.ts` | Base HTTP service with error handling |
| `src/app/core/auth/auth.service.ts` | Login/register/logout/refresh token management with Signals. Provides `userRole` getter |
| `src/app/core/auth/auth.guard.ts` | authGuard, guestGuard |
| `src/app/core/auth/admin.guard.ts` | adminGuard — checks `user().role === 'ADMIN'` |
| `src/app/core/interceptors/auth.interceptor.ts` | Attaches JWT Bearer token |
| `src/app/core/interceptors/auth-error.interceptor.ts` | Catches 401/403 → logout() → redirect to /login (EMPTY, no error propagation) |
| `src/app/core/mission.service.ts` | Mission CRUD + validation + getScenarios()/getScenario(). `validateMission()` returns `{ correct: boolean, feedback?: string }`. Admin methods: `getMissionAdmin()`, `createMission()`, `updateMission()`, `deleteMission()` |
| `src/app/core/scenario.service.ts` | Scenario CRUD + `getAdminDetail()` + `reorderMissions()` |
| `src/app/core/profile.service.ts` | Fetch profile/progress/skills (forkJoin). Progress includes missionTitle, scenarioId, scenarioTitle |
| `src/app/core/pglite.service.ts` | Browser PostgreSQL via WebAssembly. `getSchema()` returns columns with `isPrimaryKey`, `isForeignKey` |

### Feature Components
| Component | Route | Guard | File |
|-----------|-------|-------|------|
| LoginComponent | /login | guestGuard | `src/app/features/login/` |
| RegisterComponent | /register | guestGuard | `src/app/features/register/` |
| DashboardComponent | /dashboard | authGuard | `src/app/features/dashboard/` |
| ProfileComponent | /profile | authGuard | `src/app/features/profile/` |
| ScenarioListComponent | /scenarios | authGuard | `src/app/features/scenario/` |
| ScenarioDetailComponent | /scenarios/:id | authGuard | `src/app/features/scenario/` |
| MissionComponent | /mission/:id | authGuard | `src/app/features/mission/` |
| AdminComponent | /admin | adminGuard | `src/app/features/admin/` |
| AdminMissionListComponent | /admin/missions | adminGuard | `src/app/features/admin/` |
| AdminScenarioListComponent | /admin/scenarios | adminGuard | `src/app/features/admin/` |
| MissionFormComponent | /admin/mission/new, /admin/mission/:id/edit | adminGuard | `src/app/features/admin/` |
| ScenarioFormComponent | /admin/scenario/new, /admin/scenario/:id/edit | adminGuard | `src/app/features/admin/` |
| HeaderComponent | - | - | `src/app/shared/header/` |

### Scenario Models
| Type | Fields |
|------|--------|
| `ScenarioSummary` | id, title, theme, totalMissions, completedMissions |
| `ScenarioDetail` | id, title, description, theme, missions[ScenarioMissionItem], userProgress |
| `ScenarioMissionItem` | id, title, techniques, xpReward, difficulty, status('LOCKED'\|'AVAILABLE'\|'COMPLETED') |

### Scenario Lock Flow
1. **Backend-enforced**: `GET /api/missions/{id}` and `POST /api/missions/{id}/validate` throw 403 `MISSION_LOCKED` for locked scenario missions
2. **Frontend lock screen**: `mission.component.ts` catches 403 with `code === 'MISSION_LOCKED'` → shows lock icon + message + "Back to Scenario" button (navigates to `/scenarios/{scenarioId}`)
3. **Scenario detail page**: LOCKED missions show dimmed with lock icon, click is blocked by `navigateToMission` guard

### Shared Components
| Component | File | Notes |
|-----------|------|-------|
| SqlEditorComponent | `src/app/features/mission/sql-editor/` | Handles Ctrl+Enter internally, emits `(submit)` |
| ActionBarComponent | `src/app/features/mission/action-bar/` | Three states for Verify: idle/success/fail via `verifyClasses` getter |
| ResultsPaneComponent | `src/app/features/mission/results-pane/` | Fixed inner border, scrolling data, `animate-fade-in` on mount |
| MissionTabsComponent | `src/app/features/mission/mission-tabs/` | Mission + Schema tabs, `parseDDL()` for fallback schema |
| DataViewerComponent | `src/app/features/mission/data-viewer/` | `ColumnInfo { name, type, isPrimaryKey, isForeignKey }`, type-specific icons |
| ToastComponent | `src/app/shared/toast/` | Solid `bg-card`, colored border + glow per type, dismissible |
| CodeEditorDialogComponent | `src/app/shared/code-editor-dialog/` | Terminal-style fullscreen modal for DDL/DML/ExpectedResult, live sync, Tab indent |
| ConfirmDialogComponent | `src/app/shared/confirm-dialog/` | Reusable alert modal with `bg-destructive` tokens, `animate-scale-in`, Esc/backdrop close |

### Auth Flow
1. User submits login/register form
2. AuthService calls API → receives JWT + user data
3. Tokens stored in localStorage
4. Auth interceptor attaches JWT to all API requests
5. Auth error interceptor catches 401/403 → calls `authService.logout()` → redirects to `/login` (error does not propagate to components)
6. Route guards protect /dashboard, /profile, /missions/:id; redirect guests to /login
7. `adminGuard` additionally checks `user().role === 'ADMIN'` for admin routes (redirects to /dashboard if not admin)

## Project Structure

```
src/
├── app/
│   ├── core/
│   │   ├── auth/
│   │   │   ├── auth.service.ts
│   │   │   ├── auth.guard.ts
│   │   │   └── admin.guard.ts
│   │   ├── models/
│   │   │   ├── user.model.ts
│   │   │   ├── auth-response.model.ts
│   │   │   ├── mission.model.ts
│   │   │   └── scenario.model.ts
│   │   ├── interceptors/
│   │   │   ├── auth.interceptor.ts
│   │   │   └── auth-error.interceptor.ts
│   │   ├── api.service.ts
│   │   ├── mission.service.ts
│   │   ├── scenario.service.ts
│   │   ├── profile.service.ts
│   │   └── pglite.service.ts
│   ├── features/
│   │   ├── login/
│   │   ├── register/
│   │   ├── dashboard/
│   │   ├── profile/
│   │   ├── scenario/
│   │   ├── admin/
│   │   │   ├── admin.component.ts/.html
│   │   │   ├── admin-mission-list.component.ts/.html
│   │   │   ├── admin-scenario-list.component.ts/.html
│   │   │   ├── mission-form.component.ts/.html
│   │   │   ├── scenario-form.component.ts/.html/.css
│   │   └── mission/
│   │       ├── sql-editor/
│   │       ├── action-bar/
│   │       ├── results-pane/
│   │       ├── mission-tabs/
│   │       └── data-viewer/
│   ├── shared/
│   │   ├── header/
│   │   ├── toast/
│   │   ├── code-editor-dialog/
│   │   └── confirm-dialog/
│   ├── app.config.ts
│   ├── app.routes.ts
│   ├── app.ts
│   ├── app.html
│   └── app.css
├── environments/
│   └── environment.ts
└── index.html
```

## Reference Documentation

- Angular: https://angular.dev/
- PGlite: https://pglite.dev/docs/