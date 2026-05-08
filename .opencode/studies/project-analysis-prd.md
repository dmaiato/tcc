# SQLab-API Project Requirements Document

**Project Name:** SQLab-API
**Type:** REST API (Spring Boot Application)
**Version:** 0.0.1-SNAPSHOT

---

## 1. Project Overview

SQLab-API is a RESTful backend service for a SQL learning lab platform where users complete SQL challenges ("missions"), earn XP rewards, and track their progress through interactive exercises.

---

## 2. Target Users

| Role | Description |
|------|-----------|
| STUDENT | Learners who complete SQL missions and track progress |
| ADMIN | Content managers who create/update/delete missions |

---

## 3. Technology Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 4.0.5 |
| Language | Java 25 |
| Build Tool | Maven |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Password Encoding | BCrypt |
| Migrations | Flyway |
| Code Generation | Lombok |

---

## 4. Architecture

Clean Architecture with clear layer separation:

```
src/main/java/com/sqlab/
├── domain/
│   ├── model/          # Core business entities
│   └── exception/     # Domain exceptions
├── application/
│   ├── port/
│   │   ├── in/       # Use case interfaces (input ports)
│   │   └── out/      # Repository interfaces (output ports)
│   └── usecase/      # Use case implementations
└── infrastructure/
    ├── adapter/
    │   ├── in/web/   # REST controllers, DTOs
    │   └── out/      # Persistence adapters, mappers
    └── config/       # Security, JWT, beans
```

---

## 5. Domain Models

### 5.1 User
- `id` (UUID)
- `username` (String, unique)
- `email` (String, unique)
- `passwordHash` (String)
- `xp` (int) - Experience points
- `role` (UserRole: STUDENT, ADMIN)
- `createdAt` (LocalDateTime)

### 5.2 Mission
- `id` (UUID)
- `title` (String)
- `briefing` (String) - Narrative context / story
- `objective` (String) - Explicit task description
- `hint` (String, nullable) - Key SQL command(s) to solve the mission
- `ddlScript` (String) - SQL to create tables
- `dmlScript` (String) - SQL to insert test data
- `techniques` (List<String>) - SQL concepts taught
- `xpReward` (int) - XP awarded on completion
- `expectedResult` (ExpectedTuple) - Expected query output
- `ordered` (boolean) - Whether row order matters
- `theme` (Theme) - Mission category
- `difficulty` (DifficultyLevel)
- `createdAt` (LocalDateTime)

### 5.3 Progress
- `id` (UUID)
- `userId` (UUID)
- `missionId` (UUID)
- `completed` (boolean)
- `completedAt` (LocalDateTime)

### 5.4 Enums

**Theme:** ASTRONOMY, CYBERSECURITY, CRIMINAL, FINANCE, BIOLOGY

**DifficultyLevel:** BEGINNER, INTERMEDIATE, ADVANCED, EXPERT

**UserRole:** STUDENT, ADMIN

---

## 6. API Endpoints

### 6.1 Authentication

| Method | Endpoint | Access | Description |
|--------|---------|--------|-------------|
| POST | /api/auth/register | Public | Register new user |
| POST | /api/auth/login | Public | Login, returns JWT token |

**Request Bodies:**
- Register: `{ "username", "email", "password" }`
- Login: `{ "email", "password" }`

**Response:**
- Register: `{ "id", "username", "email", "xp", "level", "token" }`
- Login: `{ "id", "username", "email", "xp", "level", "token" }`

### 6.2 Missions

| Method | Endpoint | Access | Description |
|--------|---------|--------|-------------|
| GET | /api/missions | Auth | List allmissions (optional: theme, difficulty) |
| GET | /api/missions/{id} | Auth | Get mission details |
| POST | /api/missions/{id}/validate | Auth | Submit SQL results for validation |

**Query Parameters:**
- `theme` (optional): ASTRONOMY, CYBERSECURITY, CRIMINAL, FINANCE, BIOLOGY
- `difficulty` (optional): BEGINNER, INTERMEDIATE, ADVANCED, EXPERT

**Mission Response:**
```json
{ "id", "title", "briefing", "objective", "hint", "ddlScript", "dmlScript", "techniques", "xpReward", "ordered", "theme", "difficulty" }
```

**Validation Request:**
```json
{ "tuples": [{ "column1": "value1", "column2": "value2" }] }
```

**Validation Response:**
```json
{ "correct": true }
```

### 6.3 User Profile

| Method | Endpoint | Access | Description |
|--------|---------|--------|-------------|
| GET | /api/users/me | Auth | Get current user profile (including level, createdAt) |
| GET | /api/users/me/progress | Auth | Get user's mission progress |
| GET | /api/users/me/skills | Auth | Get user's aggregated skill tags |

**Profile Response:**
```json
{ "id", "username", "email", "xp", "level", "createdAt" }
```

- `level` computed server-side via `floor(sqrt(xp/100)) + 1`
- `createdAt` ISO string (`"yyyy-MM-dd'T'HH:mm:ss"`)

**Progress Response:**
```json
[{ "missionId", "completed", "completedAt" }]
```

**Skills Response:**
```json
{ "skills": ["JOIN", "AGGREGATION"] }
```

---

## 7. Security

### 7.1 JWT Configuration
- Secret key: Configured via `sqlab.jwt.secret`
- Expiration: Configured via `sqlab.jwt.expiration`

### 7.2 Authentication Entry Point
Invalid/expired tokens are handled by `JwtAuthenticationEntryPoint`:
- Returns `401` with JSON `{"status":401,"message":"Invalid or expired token"}`
- Configured via `.exceptionHandling()` in `SecurityConfig`
- Frontend `authErrorInterceptor` catches 401/403 on non-auth endpoints → calls `logout()` → redirects to `/login`
- Errors do NOT propagate to components (`EMPTY` return in interceptor)

### 7.3 Role-Based Access Control

### 7.3 Role-Based Access Control

| Endpoint | Required Role |
|-----------|---------------|
| POST /api/auth/* | Public |
| GET /api/missions | STUDENT, ADMIN |
| POST /api/missions/*/validate | STUDENT, ADMIN |
| GET /api/users/me/* | STUDENT, ADMIN |
| POST /api/missions | ADMIN only |
| PUT /api/missions/* | ADMIN only |
| DELETE /api/missions/* | ADMIN only |

---

## 8. Database

### 8.1 Configuration
- PostgreSQL 16-alpine
- Port: 5436 (mapped to 5432 in container)
- Database: `sqlab` (configurable via .env)
- User: `sqlab` / Password: `sqlab`

### 8.2 Tables (managed by Flyway)
- `users` - User accounts
- `missions` - SQL challenges
- `user_progress` - Mission completion tracking

---

## 9. Mission Validation Logic

Missions validate user-submitted SQL results against expected outputs:

```java
public boolean validate(List<Map<String, Object>> submitted) {
    return ordered
        ? expectedResult.matchesOrdered(submitted)
        : expectedResult.matchesUnordered(submitted);
}
```

- `ordered=true`: Row order must match exactly
- `ordered=false`: Rows can be in any order

When correctly validated:
1. Progress record is created/updated
2. User XP is incremented by mission's `xpReward`

---

## 10. Project Structure

```
.
├── docker-compose.yaml
├── .env
├── sqlab-api/           # Backend (Spring Boot)
│   ├── Dockerfile
│   └── src/
└── sqlab-client/        # Frontend (Angular)
    ├── Dockerfile
    └── src/
```

---

## 11. Build & Run

The project is fully dockerized for development with hot-reloading support.

### 11.1 Prerequisites
- Docker and Docker Compose

### 11.2 Configuration
Copy the default `.env` (if not present) and adjust if needed:
- `DB_USER`, `DB_PASSWORD`, `DB_NAME`
- `SQLAB_JWT_SECRET` (Escaped `$$` for Docker Compose)

### 11.3 Running the stack
```bash
# Start all services (Database, API, Client)
docker-compose up --build
```

### 11.4 Services
- **Frontend:** http://localhost:4200
- **Backend API:** http://localhost:8081
- **Database:** localhost:5436

---

## 12. Non-Functional Requirements

- **Stateless Authentication:** JWT-based, no server-side sessions
- **Password Security:** BCrypt hashed, never stored in plain text
- **Input Validation:** Jakarta Validation annotations on DTOs
- **Error Handling:** Global exception handler with structured error responses
- **Database Versioning:** Flyway migrations for schema changes