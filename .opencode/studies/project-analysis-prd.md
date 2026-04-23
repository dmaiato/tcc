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
- `briefing` (String) - Mission description
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

**Theme:** SQL, JOIN, SUBQUERIES, AGGREGATION, WINDOW_FUNCTIONS, CTES

**DifficultyLevel:** EASY, MEDIUM, HARD

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
- Register: `{ "id", "username", "email", "xp" }`
- Login: `{ "token" }`

### 6.2 Missions

| Method | Endpoint | Access | Description |
|--------|---------|--------|-------------|
| GET | /api/missions | Auth | List allmissions (optional: theme, difficulty) |
| GET | /api/missions/{id} | Auth | Get mission details |
| POST | /api/missions/{id}/validate | Auth | Submit SQL results for validation |

**Query Parameters:**
- `theme` (optional): SQL, JOIN, SUBQUERIES, etc.
- `difficulty` (optional): EASY, MEDIUM, HARD

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
| GET | /api/users/me | Auth | Get current user profile |
| GET | /api/users/me/progress | Auth | Get user's mission progress |

**Profile Response:**
```json
{ "id", "username", "email", "xp" }
```

**Progress Response:**
```json
[{ "missionId", "completed", "completedAt" }]
```

---

## 7. Security

### 7.1 JWT Configuration
- Secret key: Configured via `sqlab.jwt.secret`
- Expiration: Configured via `sqlab.jwt.expiration`

### 7.2 Role-Based Access Control

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
- Port: 5436 (docker-compose)
- Database: `sqlab`
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
sqlab-api/
├── pom.xml
├── docker-compose.yaml
├── Dockerfile
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/sqlab/
│   │   └── resources/
│   └── test/
│       └── java/com/sqlab/
```

---

## 11. Build & Run

```bash
# Build
./mvnw clean package -DskipTests

# Run with Docker
docker-compose up -d

# Or run locally
./mvnw spring-boot:run
```

**Required Environment Variables:**
- `sqlab.jwt.secret` - JWT signing key
- `sqlab.jwt.expiration` - Token expiration millis

---

## 12. Non-Functional Requirements

- **Stateless Authentication:** JWT-based, no server-side sessions
- **Password Security:** BCrypt hashed, never stored in plain text
- **Input Validation:** Jakarta Validation annotations on DTOs
- **Error Handling:** Global exception handler with structured error responses
- **Database Versioning:** Flyway migrations for schema changes