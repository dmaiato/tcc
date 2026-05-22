# Admin Test Mode — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow admins to test mission solutions (validate SQL results) without recording progress or awarding XP, and see expected results.

**Architecture:** New backend use case (`AdminValidateMissionUseCase`) with zero side effects, new endpoint `POST /api/missions/{id}/validate/admin` following the existing `/admin` suffix convention. Frontend detects admin role and adapts the same mission page — no route duplication.

**Tech Stack:** Spring Boot 4.0.5 (Java 25), Angular 21, PGlite

---

### Task 1: Create `AdminValidateMissionUseCase` interface

**Files:**
- Create: `sqlab-api/src/main/java/com/sqlab/application/port/in/AdminValidateMissionUseCase.java`

**Context:** Port interface for admin-only validation. No `userId` — admins don't need lock check, progress, or XP. Reuses the existing `ValidationResult` domain model.

- [ ] **Step 1: Create the use case interface**

```java
package com.sqlab.application.port.in;

import com.sqlab.domain.model.ValidationResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AdminValidateMissionUseCase {

    record Command(UUID missionId, List<Map<String, Object>> submittedTuples) {}

    ValidationResult handle(Command command);
}
```

- [ ] **Step 2: Verify file compiles**

Run: `mvn compile -pl sqlab-api`
Expected: `BUILD SUCCESS` (interface-only, no compilation errors)

- [ ] **Step 3: Commit**

```bash
git add sqlab-api/src/main/java/com/sqlab/application/port/in/AdminValidateMissionUseCase.java
git commit -m "feat: add AdminValidateMissionUseCase port interface"
```

---

### Task 2: Create `AdminValidateMissionService` implementation

**Files:**
- Create: `sqlab-api/src/main/java/com/sqlab/application/usecase/AdminValidateMissionService.java`

**Context:** Service that validates a mission solution without side effects. Single dependency on `MissionRepository`. Calls `mission.validate()` (same domain logic as student flow). Throws `MissionNotFoundException` if mission doesn't exist.

- [ ] **Step 1: Create the service**

```java
package com.sqlab.application.usecase;

import com.sqlab.application.port.in.AdminValidateMissionUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.ValidationResult;
import org.springframework.stereotype.Service;

@Service
public class AdminValidateMissionService implements AdminValidateMissionUseCase {

    private final MissionRepository missionRepository;

    public AdminValidateMissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    @Override
    public ValidationResult handle(Command command) {
        Mission mission = missionRepository.findById(command.missionId())
                .orElseThrow(() -> new MissionNotFoundException(command.missionId()));

        return mission.validate(command.submittedTuples());
    }
}
```

- [ ] **Step 2: Verify file compiles**

Run: `mvn compile -pl sqlab-api`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add sqlab-api/src/main/java/com/sqlab/application/usecase/AdminValidateMissionService.java
git commit -m "feat: add AdminValidateMissionService with zero side effects"
```

---

### Task 3: Add admin validate endpoint to `MissionController`

**Files:**
- Modify: `sqlab-api/src/main/java/com/sqlab/infrastructure/adapter/in/web/MissionController.java`

**Context:** Add `POST /api/missions/{missionId}/validate/admin` endpoint. Follows the suffix `/admin` convention already established by `GET /api/missions/{id}/admin`. Reuses existing DTOs `MissionDto.ValidationRequest` and `MissionDto.ValidationResponse`.

- [ ] **Step 1: Inject `AdminValidateMissionUseCase` into controller**

Add the new field and constructor parameter:

```java
import com.sqlab.application.port.in.AdminValidateMissionUseCase;

// After line 27: private final ManageMissionUseCase manageMissionUseCase;
private final AdminValidateMissionUseCase adminValidateMissionUseCase;

// In constructor (after manageMissionUseCase):
public MissionController(GetMissionsUseCase getMissionsUseCase,
                          ValidateMissionUseCase validateMissionUseCase,
                          ManageMissionUseCase manageMissionUseCase,
                          AdminValidateMissionUseCase adminValidateMissionUseCase) {
    this.getMissionsUseCase = getMissionsUseCase;
    this.validateMissionUseCase = validateMissionUseCase;
    this.manageMissionUseCase = manageMissionUseCase;
    this.adminValidateMissionUseCase = adminValidateMissionUseCase;
}
```

- [ ] **Step 2: Add the admin validate endpoint**

Insert after the `validate()` method (after line 71):

```java
@PostMapping("/{missionId}/validate/admin")
public ResponseEntity<MissionDto.ValidationResponse> adminValidate(
        @PathVariable UUID missionId,
        @Valid @RequestBody MissionDto.ValidationRequest request) {

    ValidationResult result = adminValidateMissionUseCase.handle(
            new AdminValidateMissionUseCase.Command(missionId, request.tuples())
    );
    return ResponseEntity.ok(new MissionDto.ValidationResponse(result.correct(), result.feedback()));
}
```

- [ ] **Step 3: Verify file compiles**

Run: `mvn compile -pl sqlab-api`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add sqlab-api/src/main/java/com/sqlab/infrastructure/adapter/in/web/MissionController.java
git commit -m "feat: add POST /api/missions/{id}/validate/admin endpoint"
```

---

### Task 4: Add security rule for admin validate endpoint

**Files:**
- Modify: `sqlab-api/src/main/java/com/sqlab/infrastructure/config/SecurityConfig.java`

**Context:** Only users with `ADMIN` role can call the admin validate endpoint. Follows existing pattern of `.requestMatchers(HttpMethod.POST, "/api/missions").hasRole("ADMIN")`.

- [ ] **Step 1: Add the security rule**

Insert after line 44 (after `.requestMatchers(HttpMethod.GET, "/api/missions/*/admin")`):

```java
                        .requestMatchers(HttpMethod.POST, "/api/missions/*/validate/admin").hasRole("ADMIN")
```

Full context (lines 43-46 will become):
```java
                        .requestMatchers(HttpMethod.POST,   "/api/missions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/missions/*/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/missions/*/validate/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/missions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/missions/**").hasRole("ADMIN")
```

- [ ] **Step 2: Verify file compiles**

Run: `mvn compile -pl sqlab-api`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add sqlab-api/src/main/java/com/sqlab/infrastructure/config/SecurityConfig.java
git commit -m "feat: secure POST /api/missions/*/validate/admin with ADMIN role"
```

---

### Task 5: Add `adminValidateMission` method to frontend `MissionService`

**Files:**
- Modify: `sqlab-client/src/app/core/mission.service.ts`

**Context:** New method that calls `POST /api/missions/{id}/validate/admin`. Same request/response shape as the existing `validateMission()`.

- [ ] **Step 1: Add the `adminValidateMission` method**

Insert after `validateMission()` (after line 30):

```typescript
adminValidateMission(id: string, tuples: Record<string, unknown>[]): Observable<{ correct: boolean; feedback?: string }> {
  return this.api.post<{ correct: boolean; feedback?: string }>(`/missions/${id}/validate/admin`, { tuples });
}
```

- [ ] **Step 2: Verify file compiles**

Run: `npx tsc --noEmit` or check project builds:
```bash
cd sqlab-client && npm run build
```
Expected: `Application bundle generation complete`

- [ ] **Step 3: Commit**

```bash
git add sqlab-client/src/app/core/mission.service.ts
git commit -m "feat: add adminValidateMission method to MissionService"
```

---

### Task 6: Adapt `MissionComponent` for admin test mode

**Files:**
- Modify: `sqlab-client/src/app/features/mission/mission.component.ts`

**Context:** When admin visits a mission page, `loadMission()` calls `getMissionAdmin()` instead (bypasses lock check, returns expected result). `submitSolution()` calls `adminValidateMission()` instead of `validateMission()`. Toast message is "Validation correct" for admin, "Mission complete!" for student.

- [ ] **Step 1: Add `AuthService` import and inject**

```typescript
// Line 1: add AuthService to imports
import { AuthService } from '../../core/auth/auth.service';
```

Add inject after `toastService` (line 27):
```typescript
private readonly authService = inject(AuthService);
```

- [ ] **Step 2: Add new signals**

After `schema = signal(...)` (line 53):
```typescript
expectedResult = signal<Record<string, unknown>[] | null>(null);
expectedColumns = signal<string[]>([]);
showExpected = signal(false);
```

- [ ] **Step 3: Modify `loadMission` to use admin endpoint**

Replace `loadMission` (lines 161-184) with:

```typescript
private loadMission(id: string): void {
  this.isLoading.set(true);
  this.isLocked.set(false);

  const missionObservable = this.authService.isAdmin()
    ? this.missionService.getMissionAdmin(id)
    : this.missionService.getMissionById(id);

  missionObservable.subscribe({
    next: (mission) => {
      this.mission.set(mission);
      if (this.authService.isAdmin()) {
        this.loadExpectedResult(mission);
      }
      this.initializePglite(mission);
      this.updateNavigation();
      if (mission.scenarioId) {
        this.loadScenarioMissions(mission.scenarioId);
      }
    },
    error: (err) => {
      this.isLoading.set(false);
      if (err.status === 403 && err.error?.code === 'MISSION_LOCKED') {
        this.isLocked.set(true);
        this.lockedMessage.set(err.error?.message || 'This mission is locked.');
        this.lockedScenarioId.set(err.error?.scenarioId || null);
      } else {
        this.router.navigate(['/dashboard']);
      }
    }
  });
}
```

- [ ] **Step 4: Add `loadExpectedResult` method**

After `loadSchema()` (after line 159):
```typescript
private loadExpectedResult(mission: Mission): void {
  if ('expectedResult' in mission && mission.expectedResult) {
    const result = mission.expectedResult as { rows: Record<string, unknown>[] };
    this.expectedResult.set(result.rows);
    if (result.rows.length > 0) {
      this.expectedColumns.set(Object.keys(result.rows[0]));
    }
  }
}
```

- [ ] **Step 5: Modify `submitSolution` for admin**

Replace `submitSolution` (lines 281-312) with:

```typescript
submitSolution(): void {
  const result = this.queryResult();
  if (!result) return;

  const mission = this.mission();
  if (!mission) return;

  this.isValidating.set(true);
  this.submitError.set(null);
  this.validationResult.set(null);

  const normalizedRows = this.normalizeRows(result.rows);

  const validateCall = this.authService.isAdmin()
    ? this.missionService.adminValidateMission(mission.id, normalizedRows)
    : this.missionService.validateMission(mission.id, normalizedRows);

  validateCall.subscribe({
    next: (response) => {
      this.validationResult.set(response);
      if (response.correct) {
        const msg = this.authService.isAdmin() ? 'Validation correct' : 'Mission complete!';
        this.toastService.success(msg);
      } else if (response.feedback) {
        this.toastService.error(response.feedback);
      } else {
        this.toastService.error('Incorrect result. Try again.');
      }
    },
    error: (err) => {
      const message = err instanceof Error ? err.message : 'Validation failed';
      this.submitError.set(message);
      this.toastService.error(message);
    },
    complete: () => this.isValidating.set(false)
  });
}
```

- [ ] **Step 6: Verify file compiles**

Run: `cd sqlab-client && npm run build`
Expected: `Application bundle generation complete`

- [ ] **Step 7: Commit**

```bash
git add sqlab-client/src/app/features/mission/mission.component.ts
git commit -m "feat: adapt MissionComponent for admin test mode"
```

---

### Task 7: Add admin test mode UI to mission template

**Files:**
- Modify: `sqlab-client/src/app/features/mission/mission.component.html`

**Context:** When admin is viewing a mission, show:
1. "Test Mode" accent badge in the nav strip
2. "← Admin" link alongside "← Missions" (returns to `/admin/missions`)
3. Collapsible "Expected Result" panel below the results pane

- [ ] **Step 1: Add admin badge and back link to nav strip**

Replace the navigator strip section (lines 35-42) with:

```html
  <!-- Navigator strip -->
  <div class="flex items-center justify-center gap-1 px-5 py-1.5 border-b border-border bg-background/50 shrink-0">
    <a routerLink="/" class="font-mono text-[10px] text-muted-foreground hover:text-primary transition-colors px-2 py-1 rounded hover:bg-muted">← Missions</a>
    @if (authService.isAdmin()) {
      <a routerLink="/admin/missions" class="font-mono text-[10px] text-accent hover:text-accent/80 transition-colors px-2 py-1 rounded hover:bg-accent/5">← Admin</a>
      <span class="font-mono text-[9px] text-accent px-2 py-0.5 rounded-full bg-accent/10 border border-accent/20">Test Mode</span>
    }
    @if (mission.scenarioId && mission.scenarioTitle) {
      <a [routerLink]="['/scenarios', mission.scenarioId]" class="font-mono text-[10px] text-muted-foreground hover:text-primary transition-colors px-2 py-1 rounded hover:bg-muted">{{ mission.scenarioTitle }}</a>
      <span class="font-mono text-[10px] text-muted-foreground">›</span>
    }
    <span class="font-mono text-[10px] text-muted-foreground px-3">{{ currentIndex() + 1 }} / {{ totalMissions() }}</span>
  </div>
```

- [ ] **Step 2: Add expected result panel**

Insert after `<!-- results pane -->` (after line 56, before closing `</div>`):

```html
      @if (authService.isAdmin() && expectedResult() && expectedResult()!.length > 0) {
        <div class="rounded-lg border border-accent/30 bg-accent/5 overflow-hidden">
          <button (click)="showExpected.set(!showExpected())"
            class="w-full flex items-center justify-between px-4 py-2.5 font-mono text-[10px] text-accent hover:bg-accent/5 transition-colors">
            <span class="flex items-center gap-1.5">
              <ng-icon name="lucideEye" class="w-3 h-3" />
              {{ showExpected() ? 'Hide' : 'Show' }} Expected Result
            </span>
            <ng-icon [name]="showExpected() ? 'lucideChevronUp' : 'lucideChevronDown'" class="w-3 h-3" />
          </button>
          @if (showExpected()) {
            <div class="border-t border-accent/20 overflow-x-auto">
              <table class="w-full font-mono text-xs">
                <thead>
                  <tr class="bg-accent/5">
                    @for (col of expectedColumns(); track col) {
                      <th class="text-left py-2 px-3 font-medium text-muted-foreground border-b border-border whitespace-nowrap">{{ col }}</th>
                    }
                  </tr>
                </thead>
                <tbody>
                  @for (row of expectedResult(); track $index) {
                    <tr class="border-b border-border/30 last:border-0 hover:bg-accent/5 transition-colors">
                      @for (col of expectedColumns(); track col) {
                        <td class="py-1.5 px-3 whitespace-nowrap">{{ row[col] }}</td>
                      }
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>
      }
```

- [ ] **Step 3: Verify file compiles**

Run: `cd sqlab-client && npm run build`
Expected: `Application bundle generation complete`

- [ ] **Step 4: Commit**

```bash
git add sqlab-client/src/app/features/mission/mission.component.html
git commit -m "feat: add admin test mode UI with expected result panel"
```

---

### Task 8: Add "Test" link to admin mission list

**Files:**
- Modify: `sqlab-client/src/app/features/admin/admin-mission-list.component.html`

**Context:** Each mission card in the admin mission list needs a "Test" link so admins can quickly navigate to the mission page for testing.

- [ ] **Step 1: Add "Test" link after the "Edit" button**

Insert after the edit link (after line 70, before the delete section):

```html
                      <a routerLink="/mission/{{ mission.id }}"
                        class="flex items-center justify-center size-7 rounded hover:bg-accent/10 text-accent hover:text-accent transition-colors"
                        title="Test mission">
                        <ng-icon name="lucidePlay" class="size-3.5" />
                      </a>
```

- [ ] **Step 2: Verify file compiles**

Run: `cd sqlab-client && npm run build`
Expected: `Application bundle generation complete`

- [ ] **Step 3: Commit**

```bash
git add sqlab-client/src/app/features/admin/admin-mission-list.component.html
git commit -m "feat: add Test link to admin mission list"
```

---

### Task 9: Create backend unit test

**Files:**
- Create: `sqlab-api/src/test/java/com/sqlab/application/usecase/AdminValidateMissionServiceTest.java`

**Context:** Unit test for `AdminValidateMissionService`. Verifies that validation is delegated to `mission.validate()` with correct tuples, and that `MissionNotFoundException` is thrown for missing missions.

- [ ] **Step 1: Create the test class**

```java
package com.sqlab.application.usecase;

import com.sqlab.application.port.in.AdminValidateMissionUseCase;
import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.domain.exception.MissionNotFoundException;
import com.sqlab.domain.model.ExpectedTuple;
import com.sqlab.domain.model.Mission;
import com.sqlab.domain.model.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminValidateMissionServiceTest {

    @Mock
    private MissionRepository missionRepository;

    private AdminValidateMissionService service;

    @BeforeEach
    void setUp() {
        service = new AdminValidateMissionService(missionRepository);
    }

    @Test
    void shouldDelegateToMissionValidate() {
        UUID missionId = UUID.randomUUID();
        List<Map<String, Object>> tuples = List.of(Map.of("name", "Alice"));

        Mission mission = mock(Mission.class);
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(mission.validate(tuples)).thenReturn(ValidationResult.CORRECT);

        ValidationResult result = service.handle(
                new AdminValidateMissionUseCase.Command(missionId, tuples));

        assertThat(result.correct()).isTrue();
        verify(mission).validate(tuples);
    }

    @Test
    void shouldThrowWhenMissionNotFound() {
        UUID missionId = UUID.randomUUID();
        when(missionRepository.findById(missionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(
                new AdminValidateMissionUseCase.Command(missionId, List.of())))
                .isInstanceOf(MissionNotFoundException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `mvn test -pl sqlab-api -Dtest=AdminValidateMissionServiceTest`
Expected: `Tests run: 2, Failures: 0`

- [ ] **Step 3: Commit**

```bash
git add sqlab-api/src/test/java/com/sqlab/application/usecase/AdminValidateMissionServiceTest.java
git commit -m "test: add AdminValidateMissionService unit tests"
```

---

### Task 10: Create backend integration test for admin validate endpoint

**Files:**
- Create: `sqlab-api/src/test/java/com/sqlab/infrastructure/adapter/in/web/AdminValidateMissionIntegrationTest.java`

**Context:** Integration test verifying that `POST /api/missions/{id}/validate/admin` returns 200 with correct/incorrect validation for admin users, and 403 for non-admin users.

- [ ] **Step 1: Create the integration test**

```java
package com.sqlab.infrastructure.adapter.in.web;

import com.sqlab.application.port.out.MissionRepository;
import com.sqlab.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminValidateMissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MissionRepository missionRepository;

    private UUID missionId;
    private Mission mission;

    @BeforeEach
    void setUp() {
        missionId = UUID.randomUUID();
        ExpectedTuple expected = new ExpectedTuple(List.of(
                Map.of("name", "Alice")
        ), false);

        mission = new Mission(missionId, "Test", "Briefing", "Objective", null,
                "CREATE TABLE t (name TEXT)", "INSERT INTO t VALUES ('Alice')",
                List.of("SELECT"), 100, true, Theme.CRIMINAL, DifficultyLevel.BEGINNER,
                expected, null, null, null, null, null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnCorrectWhenAdminAndValidSolution() throws Exception {
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        String requestBody = """
                {"tuples": [{"name": "Alice"}]}
                """;

        mockMvc.perform(post("/api/missions/{id}/validate/admin", missionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnIncorrectWhenAdminAndWrongSolution() throws Exception {
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        String requestBody = """
                {"tuples": [{"name": "Bob"}]}
                """;

        mockMvc.perform(post("/api/missions/{id}/validate/admin", missionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenNotAdmin() throws Exception {
        mockMvc.perform(post("/api/missions/{id}/validate/admin", missionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tuples\": []}"))
                .andExpect(status().isForbidden());
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `mvn test -pl sqlab-api -Dtest=AdminValidateMissionIntegrationTest`
Expected: `Tests run: 3, Failures: 0`

- [ ] **Step 3: Commit**

```bash
git add sqlab-api/src/test/java/com/sqlab/infrastructure/adapter/in/web/AdminValidateMissionIntegrationTest.java
git commit -m "test: add integration tests for admin validate endpoint"
```

---

### Task 11: Full build verification

- [ ] **Step 1: Run backend full test suite**

```bash
cd sqlab-api && mvn test
```
Expected: `BUILD SUCCESS` — all existing tests + new tests pass

- [ ] **Step 2: Run frontend build**

```bash
cd sqlab-client && npm run build
```
Expected: `Application bundle generation complete`

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "chore: complete admin test mode feature"
```
