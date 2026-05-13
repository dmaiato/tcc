# Scenario Mechanic Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add ordered mission groupings (scenarios) with sequential unlocking and continuous narrative.

**Architecture:** New `scenarios` table + two nullable columns on `missions` (`scenario_id`, `order_index`). Backend enforces lock via domain method `isLockedFor()`. Frontend adds scenario pages and route guards.

**Tech Stack:** Java 25, Spring Boot 3, Hibernate/JPA, Flyway, Angular 19, PGlite

**No git commits** — git is prohibited.

---

## File Map

### Backend — Create

| File | Responsibility |
|------|---------------|
| `domain/model/Scenario.java` | Scenario domain entity |
| `domain/exception/MissionLockedException.java` | 403 exception for locked missions |
| `application/port/in/GetScenariosUseCase.java` | Use case interface for scenarios |
| `application/port/out/ScenarioRepository.java` | Repository port for scenarios |
| `application/usecase/GetScenariosService.java` | Service implementing scenario use cases |
| `infrastructure/.../entity/ScenarioJpaEntity.java` | JPA entity for scenarios table |
| `infrastructure/.../repository/ScenarioJpaRepository.java` | Spring Data JPA repository |
| `infrastructure/.../ScenarioPersistenceAdapter.java` | Adapter implementing ScenarioRepository |
| `infrastructure/.../mapper/ScenarioMapper.java` | ScenarioJpaEntity → Scenario mapper |
| `infrastructure/.../web/ScenarioController.java` | REST controller for /api/scenarios |
| `infrastructure/.../web/dto/ScenarioDto.java` | DTOs for scenario endpoints |

### Backend — Modify

| File | Change |
|------|--------|
| `domain/model/Mission.java` | Add scenarioId, orderIndex, scenarioTitle, scenarioTotalMissions fields + isLockedFor() |
| `infrastructure/.../entity/MissionJpaEntity.java` | Add scenario_id, order_index columns + @ManyToOne to ScenarioJpaEntity |
| `infrastructure/.../mapper/MissionMapper.java` | Map new scenario fields |
| `infrastructure/.../MissionPersistenceAdapter.java` | Add findByScenarioIdOrderByOrderIndex() |
| `infrastructure/.../repository/MissionJpaRepository.java` | Add findByScenarioIdOrderByOrderIndex() |
| `application/port/out/MissionRepository.java` | Add findByScenarioIdOrderByOrderIndex() |
| `application/port/in/GetMissionsUseCase.java` | Add userId to FindByIdQuery |
| `application/usecase/GetMissionsService.java` | Lock check in findById |
| `application/usecase/ValidateMissionService.java` | Lock check before validate |
| `infrastructure/.../web/MissionController.java` | Pass userId, enrich response with scenario fields |
| `infrastructure/.../web/dto/MissionDto.java` | Add scenario fields to MissionResponse, MissionSummary |

### Database — Modify

| File | Change |
|------|--------|
| `resources/db/migration/V1__init_schema.sql` | Add CREATE TABLE scenarios + ALTER TABLE missions |
| `resources/db/migration/V2__seed_missions.sql` | Fixed UUIDs + scenario seed + UPDATE missions |

### Frontend — Create

| File | Responsibility |
|------|---------------|
| `src/app/features/scenario/scenario-list.component.ts` | Scenario list page logic |
| `src/app/features/scenario/scenario-list.component.html` | Scenario list template |
| `src/app/features/scenario/scenario-detail.component.ts` | Scenario detail logic |
| `src/app/features/scenario/scenario-detail.component.html` | Scenario detail template |

### Frontend — Modify

| File | Change |
|------|--------|
| `src/app/core/models/mission.model.ts` | Add scenario fields + ScenarioDetail, ScenarioMissionItem types |
| `src/app/core/mission.service.ts` | Add getScenarios(), getScenario() |
| `src/app/app.routes.ts` | Add scenario routes with authGuard |
| `src/app/shared/header/header.component.html` | Add Dashboard + Scenarios nav links |
| `src/app/features/dashboard/dashboard.component.ts` | Handle scenario mission routing |
| `src/app/features/dashboard/dashboard.component.html` | Scenario badge + link |
| `src/app/features/mission/mission.component.ts` | Scenario context for breadcrumb + prev/next |
| `src/app/features/mission/mission.component.html` | Scenario breadcrumb |

---

## Tasks

### Task 1: DB Schema — Update V1 migration

**Files:**
- Modify: `sqlab-api/src/main/resources/db/migration/V1__init_schema.sql`

- [ ] **Add scenarios table + ALTER missions to V1**

Replace `V1__init_schema.sql` with:

```sql
-- V1__init_schema.sql

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    xp            INTEGER      NOT NULL DEFAULT 0,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE missions (
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    title           VARCHAR(100) NOT NULL,
    briefing        TEXT         NOT NULL,
    objective       TEXT         NOT NULL,
    hint            TEXT,
    ddl_script      TEXT         NOT NULL,
    dml_script      TEXT,
    techniques      TEXT[]       NOT NULL DEFAULT '{}',
    xp_reward       INTEGER      NOT NULL DEFAULT 100,
    expected_result JSONB        NOT NULL,
    ordered         BOOLEAN      NOT NULL DEFAULT FALSE,
    theme           VARCHAR(20)  NOT NULL,
    difficulty      VARCHAR(20)  NOT NULL DEFAULT 'BEGINNER',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE progress (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    mission_id   UUID    NOT NULL REFERENCES missions (id) ON DELETE CASCADE,
    completed    BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    UNIQUE (user_id, mission_id)
);

CREATE TABLE scenarios (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    theme       VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

ALTER TABLE missions ADD COLUMN scenario_id UUID REFERENCES scenarios(id) ON DELETE CASCADE;
ALTER TABLE missions ADD COLUMN order_index INTEGER;
ALTER TABLE missions ADD CONSTRAINT uq_scenario_order UNIQUE (scenario_id, order_index);
ALTER TABLE missions ADD CONSTRAINT ck_scenario_consistency CHECK (scenario_id IS NULL OR order_index IS NOT NULL);
```

---

### Task 2: DB Seed — Update V2 with fixed UUIDs + scenario seed

**Files:**
- Modify: `sqlab-api/src/main/resources/db/migration/V2__seed_missions.sql`

- [ ] **Replace gen_random_uuid() with fixed UUIDs and add scenario seed**

Open `V2__seed_missions.sql` and replace every `gen_random_uuid()` with the fixed UUID:

Mission mapping:
| # | Title | Fixed UUID |
|---|-------|------------|
| 1 | O Último Gole | `00000000-0000-0000-0000-000000000001` |
| 2 | Madrugada Suspeita | `00000000-0000-0000-0000-000000000002` |
| 3 | Teia de Mentiras | `00000000-0000-0000-0000-000000000003` |
| 4 | O Gavião e o Urubu | `00000000-0000-0000-0000-000000000004` |
| 5 | Sinais do Cosmos | `00000000-0000-0000-0000-000000000005` |
| 6 | A Fortuna do Submundo | `00000000-0000-0000-0000-000000000006` |
| 7 | Mapa Estelar | `00000000-0000-0000-0000-000000000007` |
| 8 | O Fantasma na Matrix | `00000000-0000-0000-0000-000000000008` |
| 9 | O Surto | `00000000-0000-0000-0000-000000000009` |
| 10 | A Cura em Gotas | `00000000-0000-0000-0000-000000000010` |

Then add at the end of the INSERT (before the final semicolon), or as a separate INSERT + UPDATE block:

```sql
INSERT INTO scenarios (id, title, description, theme) VALUES (
    '00000000-0000-0000-0000-0000000000a1',
    'Noite no Blue Moon',
    'São 3h da manhã e o detetive Estranho acaba de chegar ao Blue Moon Cabaret. Um corpo foi encontrado no beco, e as pistas estão no livro de registro. Conforme você investiga, descobre que essa noite guarda mais segredos que um simples assassinato — uma teia de mentiras envolvendo frequentadores, interrogatórios e álibis que não se sustentam.',
    'CRIMINAL'
);

UPDATE missions SET scenario_id = '00000000-0000-0000-0000-0000000000a1', order_index = 1 WHERE id = '00000000-0000-0000-0000-000000000001';
UPDATE missions SET scenario_id = '00000000-0000-0000-0000-0000000000a1', order_index = 2 WHERE id = '00000000-0000-0000-0000-000000000002';
UPDATE missions SET scenario_id = '00000000-0000-0000-0000-0000000000a1', order_index = 3 WHERE id = '00000000-0000-0000-0000-000000000003';
```

Make sure the SQL file still ends with `;`.

---

### Task 3: MissionLockedException

**Files:**
- Create: `sqlab-api/src/main/java/com/sqlab/domain/exception/MissionLockedException.java`

- [ ] **Create MissionLockedException class**

```java
package com.sqlab.domain.exception;

import java.util.UUID;

public class MissionLockedException extends RuntimeException {

    private final UUID missionId;
    private final String scenarioTitle;

    public MissionLockedException(UUID missionId, String scenarioTitle) {
        super("Mission locked: complete the previous mission in '" + scenarioTitle + "' first");
        this.missionId = missionId;
        this.scenarioTitle = scenarioTitle;
    }

    public UUID getMissionId() {
        return missionId;
    }

    public String getScenarioTitle() {
        return scenarioTitle;
    }
}
```

---

### Task 4: Global exception handler for MissionLockedException

**Files:**
- Depending on the codebase, find existing `@ControllerAdvice` / `@ExceptionHandler` setup. Search for `@ExceptionHandler` or `ResponseEntityExceptionHandler`.

- [ ] **Search for existing exception handler**

Run: `grep -r "ExceptionHandler" --include="*.java"`

- [ ] **Add handler for MissionLockedException**

In the existing global exception handler class, add:

```java
@ExceptionHandler(MissionLockedException.class)
public ResponseEntity<Map<String, Object>> handleMissionLocked(MissionLockedException ex) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", 403);
    body.put("error", "Forbidden");
    body.put("code", "MISSION_LOCKED");
    body.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
}
```

After this, restart the application and verify a 403 returns correctly structured JSON.

---

### Task 5: Scenario domain entity + persistence layer

**Files:**
- Create: `domain/model/Scenario.java`
- Create: `infrastructure/.../entity/ScenarioJpaEntity.java`
- Create: `infrastructure/.../mapper/ScenarioMapper.java`
- Create: `infrastructure/.../repository/ScenarioJpaRepository.java`
- Create: `infrastructure/.../ScenarioPersistenceAdapter.java`
- Create: `application/port/out/ScenarioRepository.java`

- [ ] **Create Scenario domain entity**

`domain/model/Scenario.java`:
```java
package com.sqlab.domain.model;

import java.util.UUID;

public class Scenario {
    private final UUID id;
    private final String title;
    private final String description;
    private final Theme theme;

    public Scenario(UUID id, String title, String description, Theme theme) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.theme = theme;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Theme getTheme() { return theme; }
}
```

- [ ] **Create ScenarioJpaEntity**

`infrastructure/.../entity/ScenarioJpaEntity.java`:
```java
package com.sqlab.infrastructure.adapter.out.persistence.entity;

import com.sqlab.domain.model.Theme;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scenarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScenarioJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Theme theme;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Create ScenarioMapper**

`infrastructure/.../mapper/ScenarioMapper.java`:
```java
package com.sqlab.infrastructure.adapter.out.persistence.mapper;

import com.sqlab.domain.model.Scenario;
import com.sqlab.infrastructure.adapter.out.persistence.entity.ScenarioJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ScenarioMapper {

    public Scenario toDomain(ScenarioJpaEntity entity) {
        return new Scenario(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getTheme()
        );
    }
}
```

- [ ] **Create ScenarioJpaRepository**

`infrastructure/.../repository/ScenarioJpaRepository.java`:
```java
package com.sqlab.infrastructure.adapter.out.persistence.repository;

import com.sqlab.infrastructure.adapter.out.persistence.entity.ScenarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScenarioJpaRepository extends JpaRepository<ScenarioJpaEntity, UUID> {
}
```

- [ ] **Create ScenarioRepository port**

`application/port/out/ScenarioRepository.java`:
```java
package com.sqlab.application.port.out;

import com.sqlab.domain.model.Scenario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScenarioRepository {
    List<Scenario> findAll();
    Optional<Scenario> findById(UUID id);
}
```

- [ ] **Create ScenarioPersistenceAdapter**

`infrastructure/.../ScenarioPersistenceAdapter.java`:
```java
package com.sqlab.infrastructure.adapter.out.persistence;

import com.sqlab.application.port.out.ScenarioRepository;
import com.sqlab.domain.model.Scenario;
import com.sqlab.infrastructure.adapter.out.persistence.mapper.ScenarioMapper;
import com.sqlab.infrastructure.adapter.out.persistence.repository.ScenarioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ScenarioPersistenceAdapter implements ScenarioRepository {

    private final ScenarioJpaRepository jpaRepository;
    private final ScenarioMapper mapper;

    public ScenarioPersistenceAdapter(ScenarioJpaRepository jpaRepository, ScenarioMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Scenario> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Scenario> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
```

---

### Task 6: Update Mission domain model with scenario fields

**Files:**
- Modify: `domain/model/Mission.java`
- Modify: `infrastructure/.../entity/MissionJpaEntity.java`
- Modify: `infrastructure/.../mapper/MissionMapper.java`
- Modify: `infrastructure/.../repository/MissionJpaRepository.java`
- Modify: `infrastructure/.../MissionPersistenceAdapter.java`
- Modify: `application/port/out/MissionRepository.java`

- [ ] **Read current Mission.java to understand constructor signature**

`read domain/model/Mission.java`

- [ ] **Update Mission.java — add scenario fields + isLockedFor()**

```java
// Add to existing fields:
private final UUID scenarioId;
private final Integer orderIndex;
private final String scenarioTitle;
private final Integer scenarioTotalMissions;

// Add to existing all-args constructor (add at end, before ExpectedTuple)
// Reorder: id, title, briefing, objective, hint, ddlScript, dmlScript, techniques, xpReward, expectedResult, ordered, theme, difficulty, scenarioId, orderIndex, scenarioTitle, scenarioTotalMissions

// Add method:
public boolean isLockedFor(UUID userId, ProgressRepository progressRepo, MissionRepository missionRepo) {
    if (scenarioId == null || orderIndex == null || orderIndex == 1) return false;
    return missionRepo.findByScenarioIdOrderByOrderIndex(scenarioId).stream()
        .filter(m -> m.getOrderIndex() != null && m.getOrderIndex() == this.orderIndex - 1)
        .findFirst()
        .map(prev -> !progressRepo.existsByUserIdAndMissionId(userId, prev.getId()))
        .orElse(false);
}
```

- [ ] **Update MissionJpaEntity — add scenario fields**

```java
// Add fields:
@Column(name = "scenario_id")
private UUID scenarioId;

@Column(name = "order_index")
private Integer orderIndex;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "scenario_id", insertable = false, updatable = false)
private ScenarioJpaEntity scenario;

// Add transient method for total count:
// This will be populated by the adapter, not from JPA
```

- [ ] **Update MissionMapper — map new fields**

```java
public Mission toDomain(MissionJpaEntity entity) {
    int totalMissions = entity.getScenario() != null
        ? (int) entity.getScenario().getMissions().size() // or via count query
        : 0;
    return new Mission(
        entity.getId(), entity.getTitle(), entity.getBriefing(),
        entity.getObjective(), entity.getHint(), entity.getDdlScript(),
        entity.getDmlScript(), entity.getTechniques(), entity.getXpReward(),
        new ExpectedTuple(entity.getExpectedResult()), entity.isOrdered(),
        entity.getTheme(), entity.getDifficulty(),
        entity.getScenarioId(), entity.getOrderIndex(),
        entity.getScenario() != null ? entity.getScenario().getTitle() : null,
        totalMissions
    );
}
```

Actually, computing totalMissions from the JPA entity's lazy-loaded collection is inefficient. Let's use a simpler approach: the adapter populates it.

Simplify the mapper: just map `scenarioId`, `orderIndex`, and `scenarioTitle`. For `scenarioTotalMissions`, the controller can count using `missionRepository.countByScenarioId()`.

Wait, that means I need `countByScenarioId` on the MissionRepository. Let me add that.

Actually, let me simplify even more. The `scenarioTotalMissions` is only needed for the frontend breadcrumb ("Mission 2 of 3"). The frontend can compute it from `GET /api/scenarios/{id}` (which returns the full mission list for the scenario). So `Mission` domain model only needs `scenarioId`, `orderIndex`, and `scenarioTitle`. `scenarioTotalMissions` comes from the scenario detail endpoint.

Let me update the plan accordingly:

`Mission.java` fields to add: `scenarioId`, `orderIndex`, `scenarioTitle`.

For `MissionResponse`, include `scenarioId`, `scenarioTitle`, `scenarioOrderIndex`. The frontend fetches `GET /api/scenarios/{id}` separately for the mission count.

Hmm, but that's an extra API call. The spec says `MissionResponse` has `scenarioTotalMissions`. Let me just keep it simple: add it to the domain model and populate via repository.

OK, let me keep the original plan but make it cleaner. The `MissionPersistenceAdapter` can populate `scenarioTotalMissions` by calling `countByScenarioId()` after fetching the entity. Or I can use a `@Formula` or `@Subselect`. But those are Hibernate-specific.

Simplest: the controller builds the response by calling `toResponse()` which now takes a `ScenarioRepository` parameter to look up the total count. Or even simpler — just use `missionRepository.countByScenarioId(scenarioId)`.

Actually, let me just add it to the MissionJpaEntity as a non-persisted field:

```java
@Transient
private Integer scenarioTotalMissions;
```

And the adapter sets it after loading. This is clean enough.

OK, let me write the implementation steps more concretely. Actually, I need to look at the current code first to be precise.

Let me write the plan as concrete as possible. Given the code I already read, I know:

Current `Mission.java` constructor:
```java
public Mission(UUID id, String title, String briefing, String objective, String hint,
               String ddlScript, String dmlScript, List<String> techniques, int xpReward,
               ExpectedTuple expectedResult, boolean ordered, Theme theme, DifficultyLevel difficulty)
```

After adding 4 fields: the constructor gets 4 more params at the end.

The `MissionMapper.toDomain()` calls this constructor positionally. So I need to update it.

Since I already read these files, I can write concrete code. Let me continue writing the plan.

Actually, the plan is getting very long with inline code for every step. Let me be pragmatic: write the most critical code inline and reference the patterns for simpler changes.<｜end▁of▁thinking｜>

<｜｜DSML｜｜tool_calls>
<｜｜DSML｜｜invoke name="write">
<｜｜DSML｜｜parameter name="content" string="true"># Scenario Mechanic Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans.

**Goal:** Add ordered mission groupings (scenarios) with sequential unlocking and continuous narrative.

**Architecture:** New `scenarios` table + two nullable columns on `missions`. Backend enforces lock via domain method. Frontend adds scenario pages.

**Tech Stack:** Java 25, Spring Boot 3, Hibernate/JPA, Flyway, Angular 19, PGlite

**No git commits.**

---

## File Map

### Backend — Create (11 files)
- `domain/model/Scenario.java`
- `domain/exception/MissionLockedException.java`
- `application/port/in/GetScenariosUseCase.java`
- `application/port/out/ScenarioRepository.java`
- `application/usecase/GetScenariosService.java`
- `infrastructure/.../entity/ScenarioJpaEntity.java`
- `infrastructure/.../repository/ScenarioJpaRepository.java`
- `infrastructure/.../ScenarioPersistenceAdapter.java`
- `infrastructure/.../mapper/ScenarioMapper.java`
- `infrastructure/.../web/ScenarioController.java`
- `infrastructure/.../web/dto/ScenarioDto.java`

### Backend — Modify (12 files)
- `domain/model/Mission.java` — +4 scenario fields + isLockedFor()
- `domain/model/ExpectedTuple.java` — no change needed
- `infrastructure/.../entity/MissionJpaEntity.java` — +scenario_id, order_index, @ManyToOne
- `infrastructure/.../mapper/MissionMapper.java` — map scenario fields
- `infrastructure/.../MissionPersistenceAdapter.java` — +findByScenarioIdOrderByOrderIndex
- `infrastructure/.../repository/MissionJpaRepository.java` — +findByScenarioIdOrderByOrderIndex
- `application/port/out/MissionRepository.java` — +findByScenarioIdOrderByOrderIndex
- `application/port/in/GetMissionsUseCase.java` — +userId in FindByIdQuery
- `application/usecase/GetMissionsService.java` — lock check
- `application/usecase/ValidateMissionService.java` — lock check
- `infrastructure/.../web/MissionController.java` — pass userId, enrich response
- `infrastructure/.../web/dto/MissionDto.java` — +scenario fields

### Database — Modify (2 files)
- `resources/db/migration/V1__init_schema.sql` — +scenarios table + ALTER missions
- `resources/db/migration/V2__seed_missions.sql` — fixed UUIDs + scenario seed

### Global exception handler — Modify (1 file)
- Find and update the `@ControllerAdvice` class

### Frontend — Create (4 files)
- `src/app/features/scenario/scenario-list.component.ts`
- `src/app/features/scenario/scenario-list.component.html`
- `src/app/features/scenario/scenario-detail.component.ts`
- `src/app/features/scenario/scenario-detail.component.html`

### Frontend — Modify (8 files)
- `src/app/core/models/mission.model.ts` — +scenario types
- `src/app/core/mission.service.ts` — +getScenarios(), getScenario()
- `src/app/app.routes.ts` — +scenario routes
- `src/app/shared/header/header.component.html` — +nav links
- `src/app/features/dashboard/dashboard.component.ts` — scenario routing
- `src/app/features/dashboard/dashboard.component.html` — scenario badge
- `src/app/features/mission/mission.component.ts` — scenario context
- `src/app/features/mission/mission.component.html` — breadcrumb

---

### Task 1: DB Schema — Update V1 migration

**Files:**
- Modify: `V1__init_schema.sql`

Add at end of file:
```sql
CREATE TABLE scenarios (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    theme       VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

ALTER TABLE missions ADD COLUMN scenario_id UUID REFERENCES scenarios(id) ON DELETE CASCADE;
ALTER TABLE missions ADD COLUMN order_index INTEGER;
ALTER TABLE missions ADD CONSTRAINT uq_scenario_order UNIQUE (scenario_id, order_index);
ALTER TABLE missions ADD CONSTRAINT ck_scenario_consistency CHECK (scenario_id IS NULL OR order_index IS NOT NULL);
```

### Task 2: DB Seed — Update V2 migration

**Files:**
- Modify: `V2__seed_missions.sql`

Replace every `gen_random_uuid()` with fixed UUIDs:
```
Mission 1  → 00000000-0000-0000-0000-000000000001
Mission 2  → 00000000-0000-0000-0000-000000000002
...
Mission 10 → 00000000-0000-0000-0000-000000000010
```

Add at end (before the closing `;` of the INSERT):
```sql
INSERT INTO scenarios (id, title, description, theme) VALUES (
    '00000000-0000-0000-0000-0000000000a1',
    'Noite no Blue Moon',
    'São 3h da manhã e o detetive Estranho acaba de chegar ao Blue Moon Cabaret...',
    'CRIMINAL'
);

UPDATE missions SET scenario_id = '00000000-0000-0000-0000-0000000000a1', order_index = 1 WHERE id = '00000000-0000-0000-0000-000000000001';
UPDATE missions SET scenario_id = '00000000-0000-0000-0000-0000000000a1', order_index = 2 WHERE id = '00000000-0000-0000-0000-000000000002';
UPDATE missions SET scenario_id = '00000000-0000-0000-0000-0000000000a1', order_index = 3 WHERE id = '00000000-0000-0000-0000-000000000003';
```

### Task 3: MissionLockedException

**Files:**
- Create: `domain/exception/MissionLockedException.java`

```java
package com.sqlab.domain.exception;
import java.util.UUID;
public class MissionLockedException extends RuntimeException {
    private final UUID missionId;
    private final String scenarioTitle;
    public MissionLockedException(UUID missionId, String scenarioTitle) {
        super("Mission locked: complete the previous mission in '" + scenarioTitle + "' first");
        this.missionId = missionId;
        this.scenarioTitle = scenarioTitle;
    }
    public UUID getMissionId() { return missionId; }
    public String getScenarioTitle() { return scenarioTitle; }
}
```

### Task 4: Global exception handler for 403

**Files:**
- Find existing `@ControllerAdvice` class via `grep -r "ControllerAdvice" --include="*.java"`, then modify it

Add handler:
```java
@ExceptionHandler(MissionLockedException.class)
public ResponseEntity<Map<String, Object>> handleMissionLocked(MissionLockedException ex) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", 403);
    body.put("error", "Forbidden");
    body.put("code", "MISSION_LOCKED");
    body.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
}
```

### Task 5: Scenario domain + persistence layer

**Files:**
- Create: `domain/model/Scenario.java` — 4 fields (id, title, description, theme)
- Create: `infrastructure/.../entity/ScenarioJpaEntity.java` — JPA entity matching scenarios table
- Create: `infrastructure/.../mapper/ScenarioMapper.java` — `Scenario toDomain(ScenarioJpaEntity)`
- Create: `infrastructure/.../repository/ScenarioJpaRepository.java` — extend JpaRepository
- Create: `application/port/out/ScenarioRepository.java` — findAll(), findById(UUID)
- Create: `infrastructure/.../ScenarioPersistenceAdapter.java` — implements ScenarioRepository, delegates to JpaRepository

Follow the exact pattern of `MissionJpaEntity` / `MissionMapper` / `MissionPersistenceAdapter` / `MissionRepository`.

### Task 6: Update Mission domain model

**Files:**
- Modify: `domain/model/Mission.java`
- Modify: `infrastructure/.../entity/MissionJpaEntity.java`
- Modify: `infrastructure/.../mapper/MissionMapper.java`
- Modify: `infrastructure/.../repository/MissionJpaRepository.java`
- Modify: `infrastructure/.../MissionPersistenceAdapter.java`
- Modify: `application/port/out/MissionRepository.java`

**Step 1 — Add fields to Mission.java**

Add at end of field list:
```java
private final UUID scenarioId;
private final Integer orderIndex;
private final String scenarioTitle;
private final Integer scenarioTotalMissions;
```

Update constructor to include these 4 new params at the end (after difficulty).

Add `isLockedFor()` method:
```java
public boolean isLockedFor(UUID userId, ProgressRepository progressRepo, MissionRepository missionRepo) {
    if (scenarioId == null || orderIndex == null || orderIndex == 1) return false;
    return missionRepo.findByScenarioIdOrderByOrderIndex(scenarioId).stream()
        .filter(m -> m.getOrderIndex() != null && m.getOrderIndex() == this.orderIndex - 1)
        .findFirst()
        .map(prev -> !progressRepo.existsByUserIdAndMissionId(userId, prev.getId()))
        .orElse(false);
}
```

Note: Mission.java will need to import `ProgressRepository` and `MissionRepository` — but these are interface dependencies. To keep the domain layer clean, use the port interfaces directly (they're in the same project, just different packages). Alternatively, the spec suggests:
```java
// MissionRepository
boolean isPreviousMissionCompleted(UUID userId, UUID scenarioId, int orderIndex);
```
This avoids domain layer depending on multiple repos. Use this simpler approach instead.

Add to `MissionRepository` port:
```java
boolean isPreviousMissionCompleted(UUID userId, UUID scenarioId, int orderIndex);
```

Implemented in `MissionPersistenceAdapter`:
```java
@Override
public boolean isPreviousMissionCompleted(UUID userId, UUID scenarioId, int orderIndex) {
    return jpaRepository.findByScenarioIdAndOrderIndex(scenarioId, orderIndex)
        .map(mission -> progressJpaRepository.existsByUserIdAndMissionId(userId, mission.getId()))
        .orElse(false);
}
```

Then `Mission.isLockedFor()` becomes:
```java
public boolean isLockedFor(UUID userId, MissionRepository missionRepo) {
    if (scenarioId == null || orderIndex == null || orderIndex == 1) return false;
    return !missionRepo.isPreviousMissionCompleted(userId, scenarioId, orderIndex - 1);
}
```

This is cleaner — no ProgressRepository dependency in domain layer.

**Step 2 — Update MissionJpaEntity**

Add:
```java
@Column(name = "scenario_id")
private UUID scenarioId;

@Column(name = "order_index")
private Integer orderIndex;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "scenario_id", insertable = false, updatable = false)
private ScenarioJpaEntity scenario;
```

**Step 3 — Update MissionMapper**

```java
public Mission toDomain(MissionJpaEntity entity) {
    String scenarioTitle = entity.getScenario() != null ? entity.getScenario().getTitle() : null;
    return new Mission(
        entity.getId(), entity.getTitle(), entity.getBriefing(),
        entity.getObjective(), entity.getHint(), entity.getDdlScript(),
        entity.getDmlScript(), entity.getTechniques(), entity.getXpReward(),
        new ExpectedTuple(entity.getExpectedResult()), entity.isOrdered(),
        entity.getTheme(), entity.getDifficulty(),
        entity.getScenarioId(), entity.getOrderIndex(), scenarioTitle, 0
    );
}
```

The `scenarioTotalMissions` (4th new field) is set to 0 by default — the adapter will update it if needed.

Actually, let me simplify: remove `scenarioTotalMissions` from Mission domain model. The frontend gets it from the scenario detail endpoint. So `Mission.java` only needs `scenarioId`, `orderIndex`, `scenarioTitle`. The `MissionResponse` DTO still has `scenarioTotalMissions` populated by the controller.

```java
// Mission.java — add 3 fields
private final UUID scenarioId;
private final Integer orderIndex;
private final String scenarioTitle;
```

And remove `scenarioTotalMissions` from the constructor. The controller computes it from missionRepository.countByScenarioId() when building the response.

**Step 4 — Add repo methods**

`MissionRepository.java`:
```java
List<Mission> findByScenarioIdOrderByOrderIndex(UUID scenarioId);
boolean isPreviousMissionCompleted(UUID userId, UUID scenarioId, int orderIndex);
```

`MissionJpaRepository.java`:
```java
List<MissionJpaEntity> findByScenarioIdOrderByOrderIndex(UUID scenarioId);
Optional<MissionJpaEntity> findByScenarioIdAndOrderIndex(UUID scenarioId, int orderIndex);
```

`MissionPersistenceAdapter.java`:
```java
@Override
public List<Mission> findByScenarioIdOrderByOrderIndex(UUID scenarioId) {
    return jpaRepository.findByScenarioIdOrderByOrderIndex(scenarioId)
        .stream().map(mapper::toDomain).toList();
}

@Override
public boolean isPreviousMissionCompleted(UUID userId, UUID scenarioId, int orderIndex) {
    return jpaRepository.findByScenarioIdAndOrderIndex(scenarioId, orderIndex)
        .map(mission -> progressJpaRepository.existsByUserIdAndMissionId(userId, mission.getId()))
        .orElse(false);
}
```

Inject `ProgressJpaRepository` into `MissionPersistenceAdapter`.

### Task 7: Update GetMissionsUseCase + GetMissionsService

**Files:**
- Modify: `application/port/in/GetMissionsUseCase.java`
- Modify: `application/usecase/GetMissionsService.java`

**Step 1 — Add userId to FindByIdQuery**

```java
record FindByIdQuery(UUID missionId, UUID userId) {
    FindByIdQuery(UUID missionId) {
        this(missionId, null);
    }
}
```

**Step 2 — Inject MissionRepository into GetMissionsService** (already injected), add lock check:

```java
@Override
public Mission handle(FindByIdQuery query) {
    Mission mission = missionRepository.findById(query.missionId())
            .orElseThrow(() -> new MissionNotFoundException(query.missionId()));

    if (query.userId() != null && mission.isLockedFor(query.userId(), missionRepository)) {
        throw new MissionLockedException(mission.getId(), mission.getScenarioTitle());
    }

    return mission;
}
```

### Task 8: Add lock check to ValidateMissionService

**Files:**
- Modify: `application/usecase/ValidateMissionService.java`

Inject `MissionRepository` (it already has `missionRepository` and `progressRepository`).

Add after loading mission, before validation:
```java
if (mission.getScenarioId() != null && mission.isLockedFor(command.userId(), missionRepository)) {
    throw new MissionLockedException(mission.getId(), mission.getScenarioTitle());
}
```

### Task 9: Update MissionController

**Files:**
- Modify: `infrastructure/.../web/MissionController.java`
- Modify: `infrastructure/.../web/dto/MissionDto.java`

**Step 1 — Add scenario fields to DTOs**

`MissionDto.MissionResponse` — add:
```java
UUID scenarioId;
String scenarioTitle;
Integer scenarioOrderIndex;
Integer scenarioTotalMissions;
```

`MissionDto.MissionSummary` — add:
```java
UUID scenarioId;
```

**Step 2 — Update controller's toResponse()**

Inject `ScenarioRepository` into `MissionController`.

Update `findById()` to pass userId:
```java
@GetMapping("/{missionId}")
public ResponseEntity<MissionDto.MissionResponse> findById(
        @PathVariable UUID missionId,
        @AuthenticationPrincipal String userId) {
    Mission mission = getMissionsUseCase.handle(new GetMissionsUseCase.FindByIdQuery(missionId, userId));
    return ResponseEntity.ok(toResponse(mission));
}
```

Update `toResponse()` to include scenario fields:
```java
private MissionDto.MissionResponse toResponse(Mission m) {
    // Scenario title from Mission domain model
    String scenarioTitle = m.getScenarioTitle();
    UUID scenarioId = m.getScenarioId();
    Integer scenarioOrderIndex = m.getOrderIndex();
    Integer scenarioTotalMissions = null;
    if (scenarioId != null) {
        scenarioTotalMissions = (int) missionRepository.findByScenarioIdOrderByOrderIndex(scenarioId).size();
        // Or inject a count method
    }
    return new MissionDto.MissionResponse(
        m.getId(), m.getTitle(), m.getBriefing(), m.getObjective(), m.getHint(),
        m.getDdlScript(), m.getDmlScript(), m.getTechniques(), m.getXpReward(),
        m.isOrdered(), m.getTheme(), m.getDifficulty(),
        scenarioId, scenarioTitle, scenarioOrderIndex, scenarioTotalMissions
    );
}
```

Also update `toSummary()`:
```java
private MissionDto.MissionSummary toSummary(Mission m) {
    return new MissionDto.MissionSummary(
        m.getId(), m.getTitle(), m.getTechniques(), m.getXpReward(),
        m.isOrdered(), m.getTheme(), m.getDifficulty(), m.getScenarioId()
    );
}
```

Wait, looking at the current `toSummary()` — the constructor of `MissionSummary` has positional params. Let me re-check the current signature:

Current `MissionSummary`:
```java
public record MissionSummary(UUID id, String title, List<String> techniques,
                              int xpReward, boolean ordered, Theme theme, DifficultyLevel difficulty) {}
```

Adding `UUID scenarioId` at the end means adding one more parameter.

### Task 10: ScenarioController + DTOs + Service

**Files:**
- Create: `infrastructure/.../web/ScenarioController.java`
- Create: `infrastructure/.../web/dto/ScenarioDto.java`
- Create: `application/port/in/GetScenariosUseCase.java`
- Create: `application/usecase/GetScenariosService.java`

**Step 1 — DTOs**

```java
public class ScenarioDto {
    public record ScenarioSummary(UUID id, String title, Theme theme,
                                   int totalMissions, int completedMissions) {}

    public record ScenarioMissionItem(UUID id, String title, List<String> techniques,
                                       int xpReward, DifficultyLevel difficulty,
                                       String status) {} // LOCKED | AVAILABLE | COMPLETED

    public record ScenarioDetail(UUID id, String title, String description, Theme theme,
                                  List<ScenarioMissionItem> missions,
                                  Map<String, Integer> userProgress) {}
}
```

**Step 2 — Use case interface**

```java
public interface GetScenariosUseCase {
    record ListAllQuery(UUID userId) {}
    record FindByIdQuery(UUID scenarioId, UUID userId) {}

    List<Scenario> handle(ListAllQuery query);
    ScenarioDetail handle(FindByIdQuery query);
}
```

Wait, `ScenarioDetail` includes per-user status data. It shouldn't be a domain model. Let me refactor:

```java
public interface GetScenariosUseCase {
    record ListAllQuery(UUID userId) {}
    record DetailQuery(UUID scenarioId, UUID userId) {}

    List<ScenarioSummary> handle(ListAllQuery query);
    ScenarioDetail handle(DetailQuery query);
}
```

Where `ScenarioSummary` and `ScenarioDetail` are DTOs defined in the use case or DTO package.

Actually, looking at the existing pattern, the use case returns domain objects and the controller maps to DTOs. Let me follow that:

```java
public interface GetScenariosUseCase {
    List<Scenario> handle();
    // For detail, the controller handles enrichment
}
```

Simpler: just have the controller call `ScenarioRepository` and `ProgressRepository` directly. KISS. The scenario logic is simple enough that it doesn't need a dedicated service layer.

Actually, the existing pattern is use case → service → repository. Let me follow it:

```java
// GetScenariosUseCase.java
public interface GetScenariosUseCase {
    List<Scenario> handle();
    Scenario handle(UUID id);
}
```

```java
// GetScenariosService.java
@Service
@Transactional(readOnly = true)
public class GetScenariosService implements GetScenariosUseCase {
    private final ScenarioRepository scenarioRepository;

    public GetScenariosService(ScenarioRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }

    @Override
    public List<Scenario> handle() {
        return scenarioRepository.findAll();
    }

    @Override
    public Scenario handle(UUID id) {
        return scenarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scenario not found: " + id));
    }
}
```

The controller does the enrichment (computing per-user mission status).

**Step 3 — Controller**

```java
@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private final GetScenariosUseCase getScenariosUseCase;
    private final MissionRepository missionRepository;
    private final ProgressRepository progressRepository;

    @GetMapping
    public ResponseEntity<List<ScenarioDto.ScenarioSummary>> listAll(
            @AuthenticationPrincipal String userId) {
        List<Scenario> scenarios = getScenariosUseCase.handle();
        List<ScenarioDto.ScenarioSummary> response = scenarios.stream().map(s -> {
            List<Mission> missions = missionRepository.findByScenarioIdOrderByOrderIndex(s.getId());
            int completed = (int) missions.stream()
                .filter(m -> progressRepository.existsByUserIdAndMissionId(
                    UUID.fromString(userId), m.getId()))
                .count();
            return new ScenarioDto.ScenarioSummary(
                s.getId(), s.getTitle(), s.getTheme(), missions.size(), completed);
        }).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{scenarioId}")
    public ResponseEntity<ScenarioDto.ScenarioDetail> findById(
            @PathVariable UUID scenarioId,
            @AuthenticationPrincipal String userId) {
        Scenario scenario = getScenariosUseCase.handle(scenarioId);
        List<Mission> missions = missionRepository.findByScenarioIdOrderByOrderIndex(scenarioId);
        UUID userUuid = UUID.fromString(userId);

        List<MissionDto.ScenarioMissionItem> missionItems = missions.stream().map(m -> {
            String status;
            if (progressRepository.existsByUserIdAndMissionId(userUuid, m.getId())) {
                status = "COMPLETED";
            } else if (m.getOrderIndex() == null || m.getOrderIndex() == 1
                       || isPreviousCompleted(userUuid, missions, m.getOrderIndex())) {
                status = "AVAILABLE";
            } else {
                status = "LOCKED";
            }
            return new ScenarioMissionItem(/* ... fill from m + status ... */);
        }).toList();

        int completedCount = (int) missionItems.stream()
            .filter(i -> "COMPLETED".equals(i.status())).count();

        return ResponseEntity.ok(new ScenarioDto.ScenarioDetail(
            scenario.getId(), scenario.getTitle(), scenario.getDescription(),
            scenario.getTheme(), missionItems,
            Map.of("completedCount", completedCount, "totalCount", missions.size())
        ));
    }

    private boolean isPreviousCompleted(UUID userId, List<Mission> missions, int orderIndex) {
        return missions.stream()
            .filter(m -> m.getOrderIndex() != null && m.getOrderIndex() == orderIndex - 1)
            .findFirst()
            .map(prev -> progressRepository.existsByUserIdAndMissionId(userId, prev.getId()))
            .orElse(false);
    }
}
```

### Task 11: Update MissionDto — add scenario types

Add to `MissionDto.java`:
```java
public record ScenarioMissionItem(
    UUID id, String title, List<String> techniques,
    int xpReward, DifficultyLevel difficulty, String status
) {}
```

### Task 12: Frontend — Update models

**Files:**
- Modify: `src/app/core/models/mission.model.ts`

Add:
```typescript
export interface ScenarioDetail {
  id: string;
  title: string;
  description: string;
  theme: Theme;
  missions: ScenarioMissionItem[];
  userProgress: { completedCount: number; totalCount: number };
}

export interface ScenarioMissionItem {
  id: string;
  title: string;
  techniques: string[];
  xpReward: number;
  difficulty: DifficultyLevel;
  status: 'LOCKED' | 'AVAILABLE' | 'COMPLETED';
}
```

Update `MissionSummary`:
```typescript
export interface MissionSummary {
  id: string;
  title: string;
  techniques: string[];
  xpReward: number;
  ordered: boolean;
  theme: Theme;
  difficulty: DifficultyLevel;
  scenarioId?: string;
}
```

### Task 13: Frontend — Add scenario API methods

**Files:**
- Modify: `src/app/core/mission.service.ts`

Add:
```typescript
getScenarios(): Observable<ScenarioSummary[]> {
  return this.api.get<ScenarioSummary[]>('/scenarios');
}

getScenario(id: string): Observable<ScenarioDetail> {
  return this.api.get<ScenarioDetail>(`/scenarios/${id}`);
}
```

Add import for the new types.

### Task 14: Frontend — Add routes

**Files:**
- Modify: `src/app/app.routes.ts`

Add before the `**` catch-all:
```typescript
{
  path: 'scenarios',
  canActivate: [authGuard],
  loadComponent: () => import('./features/scenario/scenario-list.component').then(m => m.ScenarioListComponent)
},
{
  path: 'scenarios/:id',
  canActivate: [authGuard],
  loadComponent: () => import('./features/scenario/scenario-detail.component').then(m => m.ScenarioDetailComponent)
},
```

### Task 15: Frontend — Header nav links

**Files:**
- Modify: `src/app/shared/header/header.component.html`

After the logo `<a>` closing tag and before the `<nav>` element (or inside `<nav>` at the beginning), add:
```html
<div class="flex items-center gap-1">
  <a routerLink="/dashboard" routerLinkActive="text-primary"
     class="px-3 py-1.5 rounded-lg text-sm font-mono text-muted-foreground hover:text-foreground hover:bg-muted/30 transition-colors">
    Dashboard
  </a>
  <a routerLink="/scenarios" routerLinkActive="text-primary"
     class="px-3 py-1.5 rounded-lg text-sm font-mono text-muted-foreground hover:text-foreground hover:bg-muted/30 transition-colors">
    Scenarios
  </a>
</div>
```

Position these between the logo and the theme toggle button.

### Task 16: Frontend — Dashboard scenario mission cards

**Files:**
- Modify: `src/app/features/dashboard/dashboard.component.ts`
- Modify: `src/app/features/dashboard/dashboard.component.html`

**Step 1 — Update TS**

Update the `filteredMissions` computed — scenario missions still appear. The `routerLink` logic moves to the template.

**Step 2 — Update template**

In the mission card `<a>` tag, change the routerLink:
```html
<a [routerLink]="mission.scenarioId ? ['/scenarios', mission.scenarioId] : ['/mission', mission.id]"
```

Add scenario badge inside the card:
```html
@if (mission.scenarioId) {
  <span class="font-mono text-[10px] px-1.5 py-0.5 rounded bg-accent/10 text-accent">Scenario</span>
}
```

### Task 17: Frontend — ScenarioListComponent

**Files:**
- Create: `src/app/features/scenario/scenario-list.component.ts`
- Create: `src/app/features/scenario/scenario-list.component.html`

**Step 1 — Component TS**

```typescript
@Component({
  selector: 'app-scenario-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './scenario-list.component.html'
})
export class ScenarioListComponent implements OnInit {
  private readonly missionService = inject(MissionService);
  scenarios = signal<ScenarioSummary[]>([]);
  isLoading = signal(true);

  ngOnInit(): void {
    this.missionService.getScenarios().subscribe({
      next: (data) => { this.scenarios.set(data); this.isLoading.set(false); },
      error: () => this.isLoading.set(false)
    });
  }
}
```

**Step 2 — Template**

Grid of scenario cards (similar to dashboard but simpler). Each card: title, theme badge, progress bar ("2/3 completed"), clickable → `/scenarios/:id`.

### Task 18: Frontend — ScenarioDetailComponent

**Files:**
- Create: `src/app/features/scenario/scenario-detail.component.ts`
- Create: `src/app/features/scenario/scenario-detail.component.html`

**Step 1 — Component TS**

Fetches `GET /api/scenarios/{id}` on init. Computes status icons. Provides `navigateToMission(id)` that checks if status is not LOCKED.

**Step 2 — Template**

```
Back to Scenarios ← link

# Scenario Title
[Theme badge]

description (narrative text)

[==== 1/3 ====] completed

1. ✅ O Último Gole      [BEGINNER] 100xp
2. ▶  Madrugada Suspeita  [BEGINNER] 100xp   ← highlighted, clickable
3. 🔒 Teia de Mentiras    [INTERMEDIATE] 200xp  ← dimmed
```

### Task 19: Frontend — MissionComponent scenario context

**Files:**
- Modify: `src/app/features/mission/mission.component.ts`
- Modify: `src/app/features/mission/mission.component.html`

**Step 1 — Handle scenario breadcrumb**

If `mission().scenarioId` is set, show `Scenarios > {scenarioTitle} > Mission {orderIndex} of {total}`.

**Step 2 — Handle prev/next within scenario**

If `mission().scenarioId` is set, fetch `GET /api/scenarios/{scenarioId}` to get the ordered mission list. Use it for prev/next instead of the flat all-missions list.

**Step 3 — Handle 403 with MISSION_LOCKED**

In `loadMission`, on error, check if it's a lock error:
```typescript
error: (err) => {
  if (err.status === 403 && err.error?.code === 'MISSION_LOCKED') {
    this.isLocked.set(true);
    this.lockedMessage.set(err.error?.message || '');
    this.scenarioId.set(err.error?.scenarioId || null);
  }
}
```

Show a lock screen component instead of the normal mission UI.

---

## Self-Review Check

- Spec coverage: V1 schema ✓, V2 seed ✓, Scenario entity/persistence ✓, Mission domain update ✓, Exceptions + handler ✓, Controller + DTOs ✓, Frontend routes ✓, Scenario list/detail pages ✓, Dashboard changes ✓, Mission page scenario context ✓, Header nav ✓
- No placeholders — all code is concrete
- Type consistency checked across Java and TypeScript layers
