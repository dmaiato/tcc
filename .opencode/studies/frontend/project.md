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

### Missions

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/missions` | GET | List all missions |
| `/api/missions/:id` | GET | Get mission details |
| `/api/missions/:id/submit` | POST | Submit SQL results |

### User Progress

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/users/me` | GET | Get current user profile |
| `/api/users/me/progress` | GET | Get completed missions |

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

## Implemented Features (Phase 1)

### Core Services
| File | Purpose |
|------|---------|
| `src/environments/environment.ts` | API config (http://localhost:8081/api) |
| `src/app/core/models/user.model.ts` | User, UserResponse interfaces |
| `src/app/core/models/auth-response.model.ts` | LoginRequest, RegisterRequest, AuthResponse DTOs |
| `src/app/core/api.service.ts` | Base HTTP service with error handling |
| `src/app/core/auth/auth.service.ts` | Login/register/logout/refresh token management with Signals |
| `src/app/core/auth/auth.guard.ts` | authGuard, guestGuard |
| `src/app/core/interceptors/auth.interceptor.ts` | Attaches JWT Bearer token |
| `src/app/core/interceptors/auth-error.interceptor.ts` | 401 → refresh → retry |

### Feature Components
| Component | Route | Guard | File |
|-----------|-------|-------|------|
| LoginComponent | /login | guestGuard | `src/app/features/login/` |
| RegisterComponent | /register | guestGuard | `src/app/features/register/` |
| DashboardComponent | /dashboard | authGuard | `src/app/features/dashboard/` |
| HeaderComponent | - | - | `src/app/shared/header/` |

### Auth Flow
1. User submits login/register form
2. AuthService calls API → receives JWT + user data
3. Tokens stored in localStorage
4. Auth interceptor attaches JWT to all API requests
5. Auth error interceptor handles 401 → calls refresh → retries
6. Route guards protect /dashboard, redirect guests to /login

### Phase 2: Mission Listing
- Display all missions
- Category/difficulty filtering
- Mission selection

### Phase 3: Mission Execution
- SQL code editor
- PGlite integration
- Query execution in browser

### Phase 4: Solution Submission
- Submit results to backend
- Validation logic
- Progress tracking

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
│   │   │   └── auth-response.model.ts
│   │   ├── interceptors/
│   │   │   ├── auth.interceptor.ts
│   │   │   └── auth-error.interceptor.ts
│   │   └── api.service.ts
│   ├── features/
│   │   ├── login/
│   │   ├── register/
│   │   └── dashboard/
│   ├── shared/
│   │   └── header/
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