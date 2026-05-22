# Admin Test Mode Design

**Date:** 2026-05-20
**Status:** Approved

## Problem

Admins need to test mission solutions (write SQL, submit for validation, see feedback) without the system recording progress or awarding XP. Currently, every successful validation call creates a `progress` record and awards XP — there is no way to test a solution "for free."

## Solution

Add an **admin-only validation endpoint** that runs the same domain validation logic but **never** creates progress, awards XP, or checks scenario locks. The frontend detects admin role and adapts the mission page behavior accordingly — no separate route, no duplicated workbench.

---

## Backend

### New Use Case — `AdminValidateMissionUseCase`

**File:** `application/port/in/AdminValidateMissionUseCase.java`

```java
public interface AdminValidateMissionUseCase {
    ValidationResult handle(Command command);

    record Command(UUID missionId, List<Map<String, Object>> submittedTuples) {}
}
```

- Porta de entrada isolada — sem referência a userId, progress ou XP
- Reusa o DTO `ValidationResult` do domínio existente

### New Service — `AdminValidateMissionService`

**File:** `application/usecase/AdminValidateMissionService.java`

```java
@Component
public class AdminValidateMissionService implements AdminValidateMissionUseCase {
    private final MissionRepository missionRepository;

    @Override
    public ValidationResult handle(Command command) {
        Mission mission = missionRepository.findById(command.missionId())
                .orElseThrow(() -> new MissionNotFoundException(command.missionId()));
        return mission.validate(command.submittedTuples());
    }
}
```

- Dependência única: `MissionRepository`
- Sem lock check (admin ignora progressão de cenários)
- Sem progress, sem XP
- `MissionNotFoundException` já tratada pelo `GlobalExceptionHandler` (404)

### New Endpoint — `POST /api/missions/{missionId}/validate/admin`

**File:** `infrastructure/adapter/in/web/MissionController.java`

```java
@PostMapping("/{missionId}/validate/admin")
public MissionDto.ValidationResponse adminValidate(
        @PathVariable UUID missionId,
        @RequestBody MissionDto.ValidationRequest request) {
    ValidationResult result = adminValidateMissionUseCase.handle(
            new AdminValidateMissionUseCase.Command(missionId, request.tuples()));
    return new MissionDto.ValidationResponse(result.correct(), result.feedback());
}
```

- Segue a convenção de **sufixo** `/admin` já estabelecida por `GET /api/missions/{id}/admin`
- Request body: `{ "tuples": [...] }` — mesmo DTO `ValidationRequest` existente
- Response: `{ "correct": boolean, "feedback": string }` — mesmo DTO `ValidationResponse` existente

### SecurityConfig — Proteção do Endpoint

```java
// SecurityConfig.java
.requestMatchers(HttpMethod.POST, "/api/missions/*/validate/admin").hasRole("ADMIN")
```

- Apenas usuários com role `ADMIN` podem chamar este endpoint
- `JwtAuthenticationEntryPoint` retorna 401 para tokens inválidos/expirados
- SecurityConfig retorna 403 para `STUDENT` role

### GET da Missão para Admin

Reusa o endpoint já existente:

```
GET /api/missions/{id}/admin
```

- Já existe, já protegido por `hasRole("ADMIN")`
- Retorna DDL, DML, techniques, expectedResult — sem lock check
- Não precisa de alteração

---

## Frontend

### MissionService — Novo Método

```typescript
// mission.service.ts
adminValidateMission(id: string, tuples: Record<string, unknown>[]) {
  return this.http.post<ValidationResponse>(
    `${this.apiUrl}/missions/${id}/validate/admin`,
    { tuples }
  );
}
```

### MissionComponent — Mudanças

| Item | Detalhe |
|------|---------|
| `isAdmin` | Reusa `authService.isAdmin` (já existe) |
| `isTestMode` | `signal(true)` — default `true` quando admin |
| `expectedResult` | `signal<{ rows: Record<string, unknown>[] } \| null>(null)` |
| `loadMission()` | Se admin → chama `getMissionAdmin(id)` em vez de `getMission(id)` |
| `submitSolution()` | Se admin + testMode → chama `adminValidateMission()` |

**submitSolution — variante admin:**

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
  const validateCall = this.isAdmin() && this.isTestMode()
    ? this.missionService.adminValidateMission(mission.id, normalizedRows)
    : this.missionService.validateMission(mission.id, normalizedRows);

  validateCall.subscribe({
    next: (response) => {
      this.validationResult.set(response);
      if (response.correct) {
        const msg = this.isTestMode() ? 'Validation correct' : 'Mission complete!';
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

### MissionComponent Template — Adições Condicionais

No nav strip, quando `isAdmin()`:
- Badge "Test Mode" (estilo accent/amber, indicando que é um modo especial)
- Link "← Admin" ao lado de "← Missions" (retorna para `/admin/missions`)
- Expected result em painel collapsível (entre o editor e o results pane, ou no results pane quando admin)

**Expected Result Panel:**

```html
@if (isAdmin() && isTestMode() && expectedResult()) {
  <div class="rounded-lg border border-accent/30 bg-accent/5 p-4">
    <button (click)="showExpected.set(!showExpected())"
            class="font-mono text-[10px] text-accent">
      {{ showExpected() ? 'Hide' : 'Show' }} Expected Result
    </button>
    @if (showExpected()) {
      <div class="mt-2 rounded border border-border overflow-x-auto">
        <table class="w-full font-mono text-xs">
          <thead>
            <tr class="bg-muted/50">
              @for (col of expectedColumns(); track col) {
                <th class="text-left py-2 px-3 font-medium text-muted-foreground">{{ col }}</th>
              }
            </tr>
          </thead>
          <tbody>
            @for (row of expectedResult()?.rows; track $index) {
              <tr class="border-b border-border/50 last:border-0">
                @for (col of expectedColumns(); track col) {
                  <td class="py-2 px-3">{{ row[col] }}</td>
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

### Admin Mission List — Link "Test"

Cada card de missão no `admin-mission-list.component.html` ganha um link/button "Test" que navega para `/mission/:id`:

```html
<a [routerLink]="['/mission', mission.id]"
   class="font-mono text-xs text-accent hover:text-accent/80 transition-colors">
  Test
</a>
```

---

## Edge Cases

| Caso | Comportamento |
|------|---------------|
| Admin sem token | 401 — `JwtAuthenticationEntryPoint` |
| Admin sem role ADMIN | 403 — `SecurityConfig` |
| Missão não existe | 404 — `MissionNotFoundException` → `GlobalExceptionHandler` |
| Admin navega direto pra `/mission/:id` | Funciona — `loadMission()` detecta admin, chama endpoint admin |
| Estudante tenta chamar `POST /.../validate/admin` | 403 — `SecurityConfig` |
| Admin testa missão de cenário sem desbloquear | Funciona — sem lock check no endpoint admin |
| Admin submete sem executar query | `canSubmit` já impede (queryResult === null) |
| Admin com token expirado durante sessão | 401 → `auth-error.interceptor` → logout → redirect `/login` |

---

## Testing

### Backend

| Escopo | O que testar |
|--------|-------------|
| Unitário — `AdminValidateMissionService` | Mock `MissionRepository`, verifica que `mission.validate()` é chamado com os tuples corretos; verifica que `MissionNotFoundException` é lançada |
| Integração — endpoint | SecurityContext com role ADMIN → 200 + response correto; sem role ADMIN → 403 |

### Frontend

| Escopo | O que testar |
|--------|-------------|
| `adminValidateMission()` no service | Chamada HTTP correta para `POST /api/missions/{id}/validate/admin` |
| `submitSolution()` admin | Chama `adminValidateMission()` em vez de `validateMission()` quando admin + testMode |
| Toast message | "Validation correct" para admin, "Mission complete!" para student |

---

## Non-Goals

- Sem toggle student/admin view na mesma sessão (admin pode logar como student se quiser)
- Sem auditoria de quem testou o quê
- Sem preview de expected result para não-admin
- Sem testes end-to-end (testes unitários + integração são suficientes)
