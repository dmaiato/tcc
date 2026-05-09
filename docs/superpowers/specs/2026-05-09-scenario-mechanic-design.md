# Scenario Mechanic Design

**Date:** 2026-05-09
**Status:** Approved

## Problem

SQLab currently supports only standalone missions. There is no way to group missions into ordered sequences with a continuous narrative, where each mission unlocks only after the previous one is completed.

## Solution

Introduce a **Scenario** entity — a named, ordered collection of 2+ missions sharing a continuous story. Scenarios live alongside standalone missions without breaking existing functionality.

---

## Data Model

### New table: `scenarios`

```sql
CREATE TABLE scenarios (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    theme       VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

- `description`: the overarching narrative, displayed on the scenario detail page
- `theme`: one of `ASTRONOMY`, `CRIMINAL`, `CYBERSECURITY`, `FINANCE`, `BIOLOGY`

### New table: `scenario_missions`

```sql
CREATE TABLE scenario_missions (
    scenario_id  UUID    NOT NULL REFERENCES scenarios(id) ON DELETE CASCADE,
    mission_id   UUID    NOT NULL REFERENCES missions(id) ON DELETE CASCADE,
    order_index  INTEGER NOT NULL,
    PRIMARY KEY (scenario_id, mission_id),
    UNIQUE (scenario_id, order_index),
    UNIQUE (mission_id)
);
```

- A mission belongs to **at most one** scenario (enforced by application logic / unique constraint on mission_id if needed). Design uses join table so the `missions` table itself requires zero changes.
- `order_index` starts at 1 and must be contiguous within a scenario.

### No changes to `missions` or `progress`

- Standalone missions keep `scenario_id = null` conceptually — no schema change needed.
- `progress` already tracks per-user completion by `mission_id`; unlocking logic uses this table.

### V1 Migration: Append new tables

Add the two `CREATE TABLE` statements to `V1__init_schema.sql`.

### V2 Migration: Replace `gen_random_uuid()` with fixed UUIDs + seed scenarios

1. Change each `gen_random_uuid()` in the INSERT to a fixed UUID:
   - `'00000000-0000-0000-0000-000000000001'` through `'00000000-0000-0000-0000-000000000010'`

2. Seed scenario — group the 3 CRIMINAL missions (1: O Último Gole, 2: Madrugada Suspeita, 3: Teia de Mentiras) into a scenario called "Noite no Blue Moon":

```sql
INSERT INTO scenarios (id, title, description, theme) VALUES (
    '00000000-0000-0000-0000-0000000000a1',
    'Noite no Blue Moon',
    'São 3h da manhã e o detetive Estranho acaba de chegar ao Blue Moon Cabaret. Um corpo foi encontrado no beco, e as pistas estão todas no livro de registro da noite. Mas conforme você investiga, descobre que essa noite guarda mais segredos do que um simples assassinato — uma teia de mentiras que envolve frequentadores, interrogatórios e álibis que não se sustentam. Três missões o aguardam nas sombras do cabaré.',
    'CRIMINAL'
);

INSERT INTO scenario_missions (scenario_id, mission_id, order_index) VALUES
    ('00000000-0000-0000-0000-0000000000a1', '00000000-0000-0000-0000-000000000001', 1),
    ('00000000-0000-0000-0000-0000000000a1', '00000000-0000-0000-0000-000000000002', 2),
    ('00000000-0000-0000-0000-0000000000a1', '00000000-0000-0000-0000-000000000003', 3);
```

---

## Backend Architecture

### New Domain Entity: `Scenario`

```java
package com.sqlab.domain.model;

import java.util.UUID;

public class Scenario {
    private final UUID id;
    private final String title;
    private final String description;
    private final Theme theme;

    // all-args constructor + getters
}
```

### New Ports

**In (use cases):**
- `GetScenariosUseCase` — `List<ScenarioSummary> handle()` and `ScenarioDetail handle(FindScenarioQuery)`
- `ScenarioDetail` contains the scenario metadata + list of `ScenarioMissionItem` (each with id, title, techniques, xpReward, difficulty, ordered, and `status: LOCKED|AVAILABLE|COMPLETED`)

**Out (repositories):**
- `ScenarioRepository` — `findAll()`, `findById(UUID)`, `findMissionIdsByScenarioId(UUID)` (returns ordered list)
- `ScenarioLockRepository` (or methods on ScenarioRepository) — `isMissionUnlocked(UUID userId, UUID missionId)`

### New persistence layer (follow existing patterns):

| Layer | Class |
|-------|-------|
| JPA Entity | `ScenarioJpaEntity` |
| JPA Repository | `ScenarioJpaRepository` |
| Persistence Adapter | `ScenarioPersistenceAdapter` (implements `ScenarioRepository`) |
| Domain Mapper | `ScenarioMapper` |

### Access Control — Unlock Logic

Centralized in a reusable helper (e.g., `ScenarioLockService`):

```
isMissionUnlocked(userId, missionId):
  scenarioId ← find scenario containing this mission
  if no scenario → return true (standalone)
  orderIndex ← find order_index of mission within scenario
  if orderIndex == 1 → return true (first is always open)
  previousMissionId ← find mission with order_index - 1 in same scenario
  return progressRepository.existsByUserIdAndMissionId(userId, previousMissionId)
```

**Where the check is called:**

| Endpoint | Behavior if locked |
|----------|-------------------|
| `GET /api/missions/{id}` | Return `403 Forbidden` |
| `POST /api/missions/{id}/validate` | Return `403 Forbidden` before any validation |

New exception: `MissionLockedException` (maps to 403).

### Modified DTOs

### Modified DTOs (no domain model changes)

`MissionResponse` gains nullable fields (populated by the controller via `ScenarioRepository`):
```java
UUID scenarioId;
String scenarioTitle;
Integer scenarioOrderIndex;
Integer scenarioTotalMissions;
```

`MissionSummary` gains nullable field:
```java
UUID scenarioId;
```

The `Mission` domain model stays unchanged. The controller (or a helper service) enriches the response by calling `ScenarioRepository.findScenarioInfoByMissionId()`.

### New Repository Method

```java
// ScenarioRepository
Optional<ScenarioInfo> findScenarioInfoByMissionId(UUID missionId);

// ScenarioInfo — simple value object
public record ScenarioInfo(UUID scenarioId, String scenarioTitle,
                           int orderIndex, int totalMissions) {}
```

### New Controller

```java
@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    @GetMapping
    public List<ScenarioSummary> listAll();

    @GetMapping("/{id}")
    public ScenarioDetail findById(@PathVariable UUID id,
                                   @AuthenticationPrincipal String userId);
}
```

#### Response shape for `GET /api/scenarios/{id}`

```json
{
  "id": "uuid",
  "title": "Noite no Blue Moon",
  "description": "São 3h da manhã...",
  "theme": "CRIMINAL",
  "missions": [
    {
      "id": "uuid",
      "title": "O Último Gole",
      "techniques": ["SELECT"],
      "xpReward": 100,
      "difficulty": "BEGINNER",
      "ordered": false,
      "status": "COMPLETED"
    },
    {
      "id": "uuid",
      "title": "Madrugada Suspeita",
      "techniques": ["SELECT", "WHERE"],
      "xpReward": 100,
      "difficulty": "BEGINNER",
      "ordered": false,
      "status": "AVAILABLE"
    },
    {
      "id": "uuid",
      "title": "Teia de Mentiras",
      "techniques": ["SELECT", "INNER JOIN"],
      "xpReward": 200,
      "difficulty": "INTERMEDIATE",
      "ordered": false,
      "status": "LOCKED"
    }
  ],
  "userProgress": {
    "completedCount": 1,
    "totalCount": 3
  }
}
```

---

## Frontend Architecture

### New Route

```typescript
{ path: 'scenarios/:id', loadComponent: ScenarioDetailComponent }
```

### New Component: `ScenarioDetailComponent`

- **Location:** `src/app/features/scenario/scenario-detail/`
- **Layout:**
  1. Back button → `/dashboard`
  2. Scenario title (large heading)
  3. Theme badge
  4. Description narrative (styled blockquote or card)
  5. Progress bar: `1 / 3 completed`
  6. Vertical list of mission cards, each showing:
     - Order number badge
     - Title
     - Difficulty + XP
     - Status indicator:
       - ✅ Completed (green, clickable → /mission/:id)
       - ▶ Available (highlighted, clickable → /mission/:id)
       - 🔒 Locked (dimmed, not clickable)
- **Data fetching:** `GET /api/scenarios/{id}` returns all info in one call.

### Changed Component: `DashboardComponent`

- `MissionSummary` now includes `scenarioId` (nullable).
- In the template, if `mission.scenarioId` is non-null, the `<a>` link points to `/scenarios/{scenarioId}` instead of `/mission/{id}`, and a small "Scenario" badge is shown on the card.

### Changed Component: `MissionComponent`

- `Mission` model gets optional fields: `scenarioId`, `scenarioTitle`, `scenarioOrderIndex`, `scenarioTotalMissions`.
- When `mission.scenarioId` is set:
  - The navigation strip shows `scenarioTitle` as a breadcrumb link → `/scenarios/:id`
  - Shows "Mission X of Y" within the scenario
  - Prev/Next navigation uses the scenario's mission order instead of the global list
- Fetching the scenario mission list: either from cache (if user came from scenario page) or via `GET /api/scenarios/{id}`.
- If `GET /api/missions/{id}` returns 403, display a "Mission Locked" message with a link back to the scenario page.

### Changed Service: `MissionService`

New method:
```typescript
getScenario(id: string): Observable<ScenarioDetail>
```

New model:
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
  ordered: boolean;
  status: 'LOCKED' | 'AVAILABLE' | 'COMPLETED';
}
```

---

## Error Handling

### 403 Forbidden (Locked Mission)

Backend throws `MissionLockedException`. Frontend catches in interceptor or component:
- In `MissionComponent.loadMission()`: if 403, show lock screen with "Complete the previous mission first" and a link to the scenario page.
- In `submitSolution()`: if 403, same treatment.

### No Scenarios Exist

- Scenario list returns empty array.
- Dashboard shows all missions as standalone (no scenario badges).
- Scenario detail route can 404 if ID doesn't exist.

---

## Seed Data

### Scenario: "Noite no Blue Moon" (CRIMINAL)

| Order | Mission | Difficulty | Purpose |
|-------|---------|------------|---------|
| 1 | O Último Gole | BEGINNER | SELECT all nightclub records |
| 2 | Madrugada Suspeita | BEGINNER | Filter with WHERE (patrons after 2AM) |
| 3 | Teia de Mentiras | INTERMEDIATE | JOIN interrogations with patrons |

### Narrative

The `description` field tells the overarching story. Each mission's `briefing` picks up where the last left off, moving the detective work forward through the night.

---

## Non-Goals

- No branching or non-linear scenarios (linear only).
- No shared DDL/DML between scenario missions (each mission remains self-contained).
- No scenario editor UI (admin only via SQL for now).
- No scenario completion rewards (XP is per-mission only).

---

## Future Considerations

- Scenario completion bonus XP (awarded when all missions in a scenario are done).
- Scenario unlock animation / cutscene text.
- Multiple scenarios per theme.
- Admin CRUD for scenarios.
