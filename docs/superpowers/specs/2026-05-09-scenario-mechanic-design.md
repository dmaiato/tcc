# Scenario Mechanic Design

**Date:** 2026-05-09
**Status:** Approved

## Problem

SQLab supports only standalone missions. No way to group missions into ordered sequences with continuous narrative, unlocking sequentially.

## Solution

A **Scenario** is a named, ordered collection of 2+ missions sharing a continuous story. Scenarios live alongside standalone missions without breaking existing functionality.

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

- `description`: overarching narrative displayed on the scenario detail page

### Modified table: `missions`

Add two nullable columns:

```sql
ALTER TABLE missions ADD COLUMN scenario_id UUID REFERENCES scenarios(id) ON DELETE CASCADE;
ALTER TABLE missions ADD COLUMN order_index INTEGER;```

- `scenario_id`: if set, mission belongs to that scenario
- `order_index`: position within the scenario (1-based)
- `UNIQUE (scenario_id, order_index)` ensures no duplicate positions
- `CHECK (scenario_id IS NULL OR order_index IS NOT NULL)` prevents partial data
- Standalone missions have both as NULL
- `ON DELETE CASCADE`: deleting a scenario removes its missions too

No join table needed — a mission belongs to at most one scenario.

### No changes to `progress`

The `progress` table already tracks per-user completion by `mission_id`. Unlocking logic uses this table.

### Updated `Mission` Domain Model

`Mission.java` gains two nullable fields:
```java
private final UUID scenarioId;         // null if standalone
private final Integer orderIndex;       // null if standalone
private final String scenarioTitle;     // null if standalone (populated via LEFT JOIN)
private final Integer scenarioTotalMissions; // null if standalone
```

`MissionJpaEntity` gains:
- Column `scenario_id` + `order_index`
- Lazy `@ManyToOne` to `ScenarioJpaEntity` for title
- `scenarioTotalMissions` populated via a `@Subselect` or computed in the adapter

`MissionMapper.toDomain()` maps all four from the JPA entity.

### Updated `MissionRepository` Port

New methods:
```java
List<Mission> findByScenarioIdOrderByOrderIndex(UUID scenarioId);
int countByScenarioId(UUID scenarioId); // for scenarioTotalMissions
```

Implemented by `MissionJpaRepository` (Spring Data derived queries) and `MissionPersistenceAdapter`.

---

## Migration Strategy (dev-only DB)

Edit existing migration files in-place (project is in dev):

### V1: Append new table + column changes

Add `CREATE TABLE scenarios` and the two ALTER TABLE statements on `missions` at the end of `V1__init_schema.sql`.

### V2: Fixed UUIDs + scenario seed

1. Replace `gen_random_uuid()` with fixed UUIDs:
   - `'00000000-0000-0000-0000-000000000001'` through `'00000000-0000-0000-0000-000000000010'`

2. Seed scenario "Noite no Blue Moon" grouping the 3 CRIMINAL missions:

```sql
INSERT INTO scenarios (id, title, description, theme) VALUES (
    '00000000-0000-0000-0000-0000000000a1',
    'Noite no Blue Moon',
    'São 3h da manhã e o detetive Estranho acaba de chegar ao Blue Moon Cabaret. Um corpo foi encontrado no beco, e as pistas estão no livro de registro. Conforme você investiga, descobre que essa noite guarda mais segredos que um simples assassinato — uma teia de mentiras envolvendo frequentadores, interrogatórios e álibis que não se sustentam.',
    'CRIMINAL'
);
```

3. Assign missions to the scenario via UPDATE:

```sql
UPDATE missions SET scenario_id = '00000000-0000-0000-0000-0000000000a1', order_index = 1 WHERE id = '00000000-0000-0000-0000-000000000001';
UPDATE missions SET scenario_id = '00000000-0000-0000-0000-0000000000a1', order_index = 2 WHERE id = '00000000-0000-0000-0000-000000000002';
UPDATE missions SET scenario_id = '00000000-0000-0000-0000-0000000000a1', order_index = 3 WHERE id = '00000000-0000-0000-0000-000000000003';
```

---

## Backend

### New Domain Entity

```java
public class Scenario {
    private final UUID id;
    private final String title;
    private final String description;
    private final Theme theme;
}
```

### New Ports

**In:**
- `GetScenariosUseCase` — `List<ScenarioSummary> handle()` and `ScenarioDetail handle(FindScenarioQuery)`
- `GetMissionsUseCase.FindByIdQuery` gains optional field: `UUID userId` (null for anonymous)

**Out:**
- `ScenarioRepository` — `findAll()`, `findById(UUID)`

### New Persistence Layer

| Layer | Class |
|-------|-------|
| JPA Entity | `ScenarioJpaEntity` |
| JPA Repository | `ScenarioJpaRepository` |
| Adapter | `ScenarioPersistenceAdapter` (implements `ScenarioRepository`) |
| Mapper | `ScenarioMapper` |

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

#### Response shape `GET /api/scenarios/{id}`

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
      "status": "COMPLETED"
    },
    {
      "id": "uuid",
      "title": "Madrugada Suspeita",
      "techniques": ["SELECT", "WHERE"],
      "xpReward": 100,
      "difficulty": "BEGINNER",
      "status": "AVAILABLE"
    },
    {
      "id": "uuid",
      "title": "Teia de Mentiras",
      "techniques": ["SELECT", "INNER JOIN"],
      "xpReward": 200,
      "difficulty": "INTERMEDIATE",
      "status": "LOCKED"
    }
  ],
  "userProgress": {
    "completedCount": 1,
    "totalCount": 3
  }
}
```

### Modified DTOs

`MissionResponse` gains nullable fields (all from `Mission` domain model now):
```java
UUID scenarioId;
String scenarioTitle;
Integer scenarioOrderIndex;
Integer scenarioTotalMissions;
```

`MissionSummary` gains:
```java
UUID scenarioId;
```

### `ScenarioSummary` DTO (for list endpoint)

```java
public record ScenarioSummary(
    UUID id,
    String title,
    Theme theme,
    int totalMissions,
    int completedMissions
) {}
```

`completedMissions` is computed per-user: count of progress records where mission is in this scenario.

### Access Control — Unlock Logic

Extracted to a reusable helper method on the `Mission` domain model to avoid duplication:

```java
// Mission.java
public boolean isLockedFor(UUID userId, ProgressRepository progressRepo, MissionRepository missionRepo) {
    if (scenarioId == null || orderIndex == null || orderIndex == 1) return false;
    return missionRepo.findByScenarioIdOrderByOrderIndex(scenarioId).stream()
        .filter(m -> m.getOrderIndex() != null && m.getOrderIndex() == this.orderIndex - 1)
        .findFirst()
        .map(prev -> !progressRepo.existsByUserIdAndMissionId(userId, prev.getId()))
        .orElse(false); // if prev mission doesn't exist (gap), treat as locked
}
```

Alternatively, a simpler repository query:
```java
// MissionRepository
boolean isPreviousMissionCompleted(UUID userId, UUID scenarioId, int orderIndex);
// SQL: SELECT EXISTS(SELECT 1 FROM progress p JOIN missions m ON p.mission_id = m.id
//      WHERE p.user_id = ? AND m.scenario_id = ? AND m.order_index = ? AND p.completed = true)
```

**Used in:**

**`GetMissionsService`** — after `FindByIdQuery`:
```
if (mission.isLockedFor(query.userId(), progressRepo, missionRepo))
    throw new MissionLockedException(mission.getId(), scenarioTitle);
```

**`ValidateMissionService`** — before validation:
```
if (mission.isLockedFor(command.userId(), progressRepo, missionRepo))
    throw new MissionLockedException(mission.getId(), scenarioTitle);
```

**`MissionLockedException`** → 403 with body:
```json
{
  "status": 403,
  "message": "Mission locked: complete 'Madrugada Suspeita' first",
  "code": "MISSION_LOCKED"
}
```

Frontend parses `code === "MISSION_LOCKED"` to show the lock screen with a link to the scenario page, vs. generic 403 for auth errors.

---

## Frontend

### New Routes

```typescript
{ path: 'scenarios',     loadComponent: ScenarioListComponent, canActivate: [authGuard] }
{ path: 'scenarios/:id', loadComponent: ScenarioDetailComponent, canActivate: [authGuard] }
```

### Nav Links in Header

Two links between the logo and the theme toggle:
```
[SQLab]  Dashboard  Scenarios  [🌙]  [avatar]
```

Styled as simple text links with `routerLinkActive="text-primary"` for active state.

### ScenarioListComponent (`/scenarios`)

Grid of scenario cards, each showing:
- Title
- Theme badge
- X of Y missions completed (progress bar)
- Click → `/scenarios/:id`

Fetches data from `GET /api/scenarios`.

### ScenarioDetailComponent (`/scenarios/:id`)

1. Back button → `/scenarios`
2. Scenario title (large)
3. Theme badge
4. Description narrative
5. Progress bar: `1 / 3 completed`
6. Vertical list of mission cards:
   - Order number badge
   - Title
   - Difficulty + XP
   - Status: ✅ COMPLETED (clickable → `/mission/:id`) / ▶ AVAILABLE (highlighted, clickable) / 🔒 LOCKED (dimmed, disabled)

### Dashboard Changes

- `MissionSummary` includes `scenarioId` (nullable)
- Scenario missions appear individually in the grid (each as its own card), not as a group
- If `mission.scenarioId` is set:
  - Card shows a "Scenario" badge (label: title of the scenario)
  - `[routerLink]` → `/scenarios/:scenarioId` instead of `/mission/:id`
  - Checkmark icon still shows per-mission completion
- Otherwise: unchanged behavior
- Theme/difficulty filters work the same — scenario missions are filtered like standalone ones

### MissionComponent Changes

- `Mission` model gets optional fields: `scenarioId`, `scenarioTitle`, `scenarioOrderIndex`, `scenarioTotalMissions`
- If `scenarioId` is set:
  - Breadcrumb: `Scenarios > Noite no Blue Moon > Mission 2 of 3`
  - Prev/next navigates within scenario. Data source: `GET /api/scenarios/{id}` fetches the ordered mission list (or cached from the scenario page). Falls back to the flat all-missions list if no scenario.
- If `GET /missions/:id` returns 403 with `code: "MISSION_LOCKED"`: lock screen with "Complete the previous mission first" + link to scenario page

### New Model Types

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

### New Service Method

```typescript
getScenarios(): Observable<ScenarioSummary[]>
getScenario(id: string): Observable<ScenarioDetail>
```

---

## Seed Data

### "Noite no Blue Moon" (CRIMINAL, 3 missions)

| # | Title | Difficulty | Teaches |
|---|-------|------------|---------|
| 1 | O Último Gole | BEGINNER | SELECT |
| 2 | Madrugada Suspeita | BEGINNER | SELECT, WHERE |
| 3 | Teia de Mentiras | INTERMEDIATE | SELECT, INNER JOIN |

---

## Testing

### Backend

| Scope | What to test |
|-------|-------------|
| Migration | V1 schema changes apply cleanly; V2 seed creates scenario + assigns missions |
| Repository | `findByScenarioIdOrderByOrderIndex` returns missions in correct order; `isPreviousMissionCompleted` works with/without progress |
| Service | Lock logic: standalone mission always unlocked; first mission unlocked; subsequent mission locked without progress; locked after progress |
| Controller | `GET /api/scenarios/{id}` returns correct mission statuses per user; `GET /api/missions/{id}` returns 403 for locked mission; `POST /.../validate` returns 403 for locked |
| Exception | `MissionLockedException` serializes with correct status/code/message |

### Frontend

| Scope | What to test |
|-------|-------------|
| ScenarioList | Renders scenario cards; shows progress bar |
| ScenarioDetail | Renders narrative; shows mission list with correct status icons; locked missions not clickable |
| MissionComponent | Shows lock screen for 403 MISSION_LOCKED; shows breadcrumb for scenario missions |
| Dashboard | Scenario mission card routes to `/scenarios/:id` instead of `/mission/:id` |

---

## Non-Goals

- No branching/non-linear scenarios
- No shared DDL/DML between missions
- No scenario editor UI
- No scenario completion bonus XP
