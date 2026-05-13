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
| `/api/missions/:id/validate` | POST | Submit SQL results (403 if locked) |

### User Progress

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/users/me` | GET | Get current user profile (id, username, email, xp, level, createdAt) |
| `/api/users/me/progress` | GET | Get completed missions |
| `/api/users/me/skills` | GET | Get aggregated skill tags |

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

## Implemented Features

### Core Services
| File | Purpose |
|------|---------|
| `src/environments/environment.ts` | API config (http://localhost:8081/api) |
| `src/app/core/models/user.model.ts` | User, UserResponse (id, username, email, xp, level, createdAt) |
| `src/app/core/models/auth-response.model.ts` | LoginRequest, RegisterRequest, AuthResponse DTOs |
| `src/app/core/models/mission.model.ts` | Mission (+scenarioId, scenarioTitle, scenarioOrderIndex, scenarioTotalMissions), MissionSummary (+scenarioId), ScenarioDetail, ScenarioMissionItem, ScenarioSummary |
| `src/app/core/api.service.ts` | Base HTTP service with error handling |
| `src/app/core/auth/auth.service.ts` | Login/register/logout/refresh token management with Signals |
| `src/app/core/auth/auth.guard.ts` | authGuard, guestGuard |
| `src/app/core/interceptors/auth.interceptor.ts` | Attaches JWT Bearer token |
| `src/app/core/interceptors/auth-error.interceptor.ts` | Catches 401/403 → logout() → redirect to /login (EMPTY, no error propagation) |
| `src/app/core/mission.service.ts` | Mission CRUD + validation + getScenarios()/getScenario() |
| `src/app/core/profile.service.ts` | Fetch profile/progress/skills (forkJoin) |
| `src/app/core/pglite.service.ts` | Browser PostgreSQL via WebAssembly |

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
| Component | File |
|-----------|------|
| SqlEditorComponent | `src/app/features/mission/sql-editor/` |
| ActionBarComponent | `src/app/features/mission/action-bar/` |
| ResultsPaneComponent | `src/app/features/mission/results-pane/` |
| MissionTabsComponent | `src/app/features/mission/mission-tabs/` |
| DataViewerComponent | `src/app/features/mission/data-viewer/` |
| ToastComponent | `src/app/shared/toast/` |

### Auth Flow
1. User submits login/register form
2. AuthService calls API → receives JWT + user data
3. Tokens stored in localStorage
4. Auth interceptor attaches JWT to all API requests
5. Auth error interceptor catches 401/403 → calls `authService.logout()` → redirects to `/login` (error does not propagate to components)
6. Route guards protect /dashboard, /profile, /missions/:id; redirect guests to /login

## Project Structure

```
src/
├── app/
│   ├── core/
│   │   ├── auth/
│   │   │   ├── auth.service.ts
│   │   │   └── auth.guard.ts
│   │   ├── models/
│   │   │   ├── user.model.ts
│   │   │   ├── auth-response.model.ts
│   │   │   └── mission.model.ts
│   │   ├── interceptors/
│   │   │   ├── auth.interceptor.ts
│   │   │   └── auth-error.interceptor.ts
│   │   ├── api.service.ts
│   │   ├── mission.service.ts
│   │   ├── profile.service.ts
│   │   └── pglite.service.ts
│   ├── features/
│   │   ├── login/
│   │   ├── register/
│   │   ├── dashboard/
│   │   ├── profile/
│   │   ├── scenario/
│   │   └── mission/
│   │       ├── sql-editor/
│   │       ├── action-bar/
│   │       ├── results-pane/
│   │       ├── mission-tabs/
│   │       └── data-viewer/
│   ├── shared/
│   │   ├── header/
│   │   └── toast/
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