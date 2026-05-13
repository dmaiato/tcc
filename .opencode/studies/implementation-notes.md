# Session Notes: SQLab Implementation

## Date: 2026-04-24

---

## 1. Frontend Auth Fix: username vs name

### Problem
The frontend registration form had a `name` field, but the backend expected `username`.

### Files Changed
- `sqlab-client/src/app/core/models/auth-response.model.ts` - Changed `RegisterRequest.name` → `username`
- `sqlab-client/src/app/features/register/register.component.ts` - Form and submit
- `sqlab-client/src/app/features/register/register.component.html` - Template
- `sqlab-client/src/app/core/models/user.model.ts` - User interface
- `sqlab-client/src/app/features/dashboard/dashboard.component.ts` - Name getter
- `sqlab-client/src/app/shared/header/header.component.ts` - Name getter

---

## 2. Styling System Implementation

### Files Created/Modified
- `sqlab-client/src/styles.css` - Complete design system

### Design System Components
- **HSL Color Tokens**: All 17 semantic tokens in HSL format
- **Font**: Space Grotesk (sans) + JetBrains Mono (mono)
- **Custom Utilities**: `.glow-success`, `.glow-error`, `.glow-primary`, `.glow-accent`, `.gradient-mesh`, `.card-shine`
- **Animations**: `fade-in`, `scale-in`, `pulse-glow`, `row-appear`

### Component Updates
- **Header** (`header.component.html`): Glass header with gradient logo `SQ` + `Lab`
- **Login** (`login.component.html`): Card with gradient-mesh background
- **Register** (`register.component.html`): Same pattern as login

### Layout Structure
- `app.html` - Root with gradient-mesh + header
- `body` - Has `bg-background` class in index.html

### Styling Patterns Used
```html
<!-- Icon tile gradient -->
<div class="w-10 h-10 rounded-xl bg-gradient-to-br from-primary to-secondary">

<!-- Glass header -->
<header class="px-5 py-3 ... bg-background/80 backdrop-blur-sm">

<!-- Auth card -->
<div class="rounded-xl border border-border bg-card p-6 card-shine">
```

### Text Colors
- Titles: `text-foreground` (bright off-white)
- Meta: `text-muted-foreground`
- Logo: Split `text-foreground` + `text-primary` (green)

---

## 3. Registration Backend Fix

### Problem
Backend `/register` returned only user data, no JWT token. After registration, user couldn't login automatically.

### Backend Changes
- `AuthDto.java` - Added `AuthResponseWithUser` record with token
- `AuthController.java` - Now generates token after registration
- `UserRepository.java` - Added `findByUsername()`
- `UserPersistenceAdapter.java` - Implemented `findByUsername()`
- `UserJpaRepository.java` - Added `findByUsername()`

### Frontend Changes
- `auth-response.model.ts` - Changed to match backend response (`token` not `accessToken`)
- `auth.service.ts` - Map `response.token` instead of `response.accessToken`

### API Contract
```json
// POST /api/auth/register
Request:  { "username": "", "email": "", "password": "" }
Response: { "token": "", "id": "", "username": "", "email": "" }

// POST /api/auth/login
Request:  { "email": "", "password": "" }
Response: { "token": "", "id": "", "username": "", "email": "" }
```

---

## 4. CORS Configuration

### SecurityConfig.java
Added CORS to allow Angular frontend:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:4200"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    // ...
}
```

---

## 5. Build Commands

### Frontend
```bash
cd sqlab-client && npm run build
```

### Backend
```bash
cd sqlab-api && mvn compile
```

---

## 6. Known Issues / TODO

- [x] Dashboard styling implemented
- [ ] Login/Register password confirm validation in backend
- [ ] Error handling could be more user-friendly
- [ ] Connect header stats to API (userLevel, totalXp, solvedMissions)
- [ ] Connect dashboard stats to API

---

## 8. Dashboard Implementation

## 7. Header Refactoring (per STYLING_GUIDE_v3 §8)

> **Note:** STYLING_GUIDE_v3 is written for React with shadcn/ui, but the principles were adapted to Angular. The Angular header implementation follows the same anatomy and patterns.

### Overview
Refactored the Angular header to match the React design guide's TopBar anatomy. Built a custom dropdown since Angular CDK was not installed.

### Files Changed
- `sqlab-client/src/app/shared/header/header.component.ts` - Added dropdown state, navigation methods
- `sqlab-client/src/app/shared/header/header.component.html` - Full structural refactor

### Implementation Details

#### Shell (§8.1)
```html
<header class="px-5 py-3 flex items-center justify-between border-b border-border bg-background/80 backdrop-blur-sm shrink-0">
```

#### Logo (§8.2)
- Database icon in gradient tile (`w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-secondary`)
- Wordmark: "SQ" plain + "Lab" in primary color
- Hover: wordmark turns primary

#### UserChip Trigger (§8.5)
- Avatar (w-8 h-8) + level badge (w-4 h-4 rounded-full bg-accent)
- Username + "Lvl X" meta (hidden on mobile < sm)
- ChevronDown icon
- hover:bg-muted/50
- focus-visible:ring

#### Custom Dropdown (§8.7)
- `w-64 bg-card border-border`
- Align end (flush with trigger right edge)
- Click-outside to close (HostListener on document)

#### Dropdown Region A: Profile Summary (§8.7.A)
- Big avatar (w-12 h-12 rounded-xl)
- Username + "Level X · Y XP" meta
- XP progress bar (§8.8) - static width 0% (mocked)
- Stat tiles grid (§8.9) - 3 columns, color rotation primary→accent→secondary:
  - Solved: text-primary, icon-primary (trophy)
  - XP: text-accent, icon-accent (lightning)
  - Left: text-secondary, icon-secondary (target)

#### Dropdown Region B: Navigation (§8.7.B)
- View Full Profile (icon: user, text-primary)
- Leaderboard (icon: trophy, text-accent)
- Mission Control (icon: shield, text-accent)
- Sign out (icon: log-out, text-destructive, hover:bg-destructive/10)

### Focus Ring Compliance (§14)
All interactive elements now have:
- `focus-visible:outline-none`
- `focus-visible:ring-2 focus-visible:ring-ring`
- `focus-visible:ring-offset-2 focus-visible:ring-offset-background`

### Mocked Values (TODO: Connect to API)
```typescript
// header.component.ts
get userLevel(): number { return 1; }  // TODO: derive from XP or fetch
get totalXp(): number { return 0; }       // TODO: fetch from API
get solvedMissions(): number { return 0; }  // TODO: fetch from API
get totalMissions(): number { return 20; }  // TODO: fetch from API
get missionsRemaining(): number { return this.totalMissions - this.solvedMissions; }
```

### Navigation Routes (Stubs)
- `/profile` - TODO: implement profile page
- `/leaderboard` - TODO: implement leaderboard page
- `/admin` - TODO: implement admin page

---

## 8. Dashboard Implementation

### Files Created
- `sqlab-client/src/app/core/models/mission.model.ts` - Mission types
- `sqlab-client/src/app/core/mission.service.ts` - Mission API service

### Files Modified
- `sqlab-client/src/app/features/dashboard/dashboard.component.ts` - Component logic
- `sqlab-client/src/app/features/dashboard/dashboard.component.html` - Template
- `sqlab-client/src/app/features/dashboard/dashboard.component.css` - Removed unused styles

### Implementation Details

#### Layout (§7.B)
- Uses `max-w-6xl mx-auto` for mission browser
- Section header with gradient icon tile (trophy)

#### Filter Dropdowns
- Theme dropdown: Criminal, Finance, Astronomy, Cybersecurity, Biology
- Difficulty dropdown: Beginner, Intermediate, Advanced, Expert
- Click-outside to close (HostListener on document)
- Individual `$event.stopPropagation()` on buttons to prevent bubbling

#### Mission Cards
- Grid layout: `grid-cols-1 sm:grid-cols-2 lg:grid-cols-3`
- Badge order: difficulty first, then theme
- Difficulty colors: Beginner (primary), Intermediate (accent), Advanced/Expert (destructive)
- Theme badge: neutral (muted)
- Completed indicator: checkmark overlay

#### Filtering
- Uses Angular signals (`selectedTheme`, `selectedDifficulty`)
- `filteredMissions` computed signal
- Clear filters button (appears when filters active)

#### Known Issues
- Missions not loading - backend enum mismatch (Theme/Difficulty)
  - Frontend expected PRD values (`SQL`, `JOIN`, `EASY`, etc.)
  - Backend uses (`CRIMINAL`, `FINANCE`, `BEGINNER`, etc.)
  - **FIXED**: Updated frontend model to match backend enums

### Mocked Values (TODO: Connect to API)
All stats come from backend via MissionService. May need verification when backend is running.

### Navigation Routes (Stubs)
- `/missions/:id` - TODO: implement mission page

---

## 9. Mission Page Implementation

### Overview
Implemented the mission page workbench following STYLING_GUIDE_v3 principles, adapted for Angular. The page provides a full-viewport SQL editor with mission details, schema explorer, and results pane.

### Files Modified

#### Layout & Container
- `sqlab-client/src/app/app.html` - Changed to `h-screen flex flex-col overflow-hidden gradient-mesh` for proper viewport locking
- `sqlab-client/src/app/features/mission/mission.component.html` - Simplified parent to only handle spacing (`p-4 gap-3`)

#### Components (Each manages own container styling)

**sql-editor** (`sql-editor.component.html`)
- Height: `h-[180px]`
- Container: rounded border + bg-editor
- Title bar: traffic lights (red/amber/green dots), shortcut hint
- Textarea: flex-1, font-mono, placeholder styling

**action-bar** (`action-bar.component.html`)
- Status zone (left): char count, "Modified" pill
- Buttons (right): Restore → Run → Verify order
- Verify button has three states via `verifyClasses` getter

**results-pane** (`results-pane.component.html`)
- Container: `flex-1 min-h-0 rounded-lg border border-border bg-card`
- Empty state: centered text "Output will appear here"
- Error state: destructive card with glow
- Data: table with staggered row animations

**mission-tabs** (`mission-tabs.component.html/ts`)
- Tabs: Mission (primary color) / Schema (secondary color)
- Icons added to tab buttons
- Mission content: badges (difficulty, theme, XP, solved), objective, tables available, hint
- Schema tab: derives schema from DDL via `parseDDL()` method

**data-viewer** (`data-viewer.component.html`)
- Accordion-style table list
- Type icons: hash (numeric), type (text), key (other)
- Sample data expansion

### Header Contextual Back-Link
- `header.component.ts` - Added `currentPath` signal and `showBackLink` computed
- Shows "← Missions" when not on root page

### DDL Parsing
- `mission-tabs.component.ts` - Added `parseDDL()` method
- Extracts CREATE TABLE statements with column names and types
- Filters out constraints (NOT NULL, PRIMARY KEY, etc.)

### Signal Inputs (Angular 18+)
- Converted `@Input()` to `input()` signal-based inputs for proper reactivity
- `results-pane.component.ts` - Uses `result = input<QueryResult>()` pattern

### QueryResult Handling
- `pglite.service.ts` - Returns error as part of result object instead of throwing
- `mission.component.ts` - Handles result.error and sets queryError accordingly

### Styling Patterns
```html
<!-- Parent only handles spacing -->
<div class="w-[62%] flex flex-col min-h-0 p-4 gap-3">
  <app-sql-editor />
  <app-action-bar />
  <app-results-pane />
</div>

<!-- Each component manages own container -->
<div class="h-[180px] rounded-lg border border-border ...">
  <!-- content -->
</div>
```

### Known Issues Fixed
- Query results not appearing → Fixed by using signal inputs for reactivity
- "Query correct" showing on every query → Removed success flag from executeQuery
- Icons appearing broken → Added `stroke-linecap="round" stroke-linejoin="round"` to all SVGs
- Schema tab not working → Added DDL parsing to extract table/column info
- "NOT NULL" parsing as column type → Fixed regex to stop at constraints
- Empty state not centered → Added `flex items-center justify-center`
- SQL editor too short → Set fixed height `h-[180px]`

---

## 10. Toast Component Implementation

### Overview
Created a standardized toast notification system for user feedback.

### Files Created
- `sqlab-client/src/app/shared/toast/toast.service.ts` - Toast service with signal-based state
- `sqlab-client/src/app/shared/toast/toast.component.ts` - Toast display component

### ToastService API
```typescript
// Methods available
toastService.success(message: string, duration?: number): void
toastService.error(message: string, duration?: number): void
toastService.info(message: string, duration?: number): void
toastService.dismiss(id: string): void
```

### Toast Design Standardized
- Background: `bg-muted/90` (neutral gray)
- Border: `border-border`
- All icons: white (`style="color: white"`)
- All text: white (`style="color: white"`)
- Layout: icon (left) - text (center) - close button (right)
- Min-width: 280px for consistency
- Uses Lucide icons: lucideCheck, lucideXCircle, lucideInfo, lucideX

### Files Modified
- `sqlab-client/src/app/app.config.ts` - Added lucideXCircle, lucideInfo to provideIcons

---

## 11. Result Validation Fix

### Problem
Mission verification always returned `correct: false` even with correct queries because pglite returns JavaScript Date objects while backend stored date strings.

### Files Modified
- `sqlab-client/src/app/features/mission/mission.component.ts` - Added `normalizeRows()` method

### Solution
```typescript
private normalizeRows(rows: Record<string, unknown>[]): Record<string, unknown>[] {
  return rows.map(row => {
    const normalized: Record<string, unknown> = {};
    for (const [key, value] of Object.entries(row)) {
      if (value instanceof Date) {
        normalized[key] = value.toISOString().split('T')[0]; // "2022-11-01"
      } else {
        normalized[key] = value;
      }
    }
    return normalized;
  });
}
```

### Usage
- Called in `submitSolution()` before sending to validation API

---

## 12. Schema Auto-Refresh

### Problem
Schema tab showed static DDL-parsed schema, didn't update when user modified database.

### Files Modified
- `sqlab-client/src/app/core/pglite.service.ts` - Added `getSchema()` method
- `sqlab-client/src/app/features/mission/mission.component.ts` - Added comparison logic

### Implementation
1. **PgliteService.getSchema()**: Queries `information_schema.columns` to get current DB schema
2. **MissionComponent.refreshSchemaIfNeeded()**: Compares DB schema with displayed schema
3. **MissionComponent.schemasEqual()**: Deep comparison (tables + columns with types)
4. Triggers after `executeQuery()` and `resetDatabase()`
5. Shows toast "Schema updated" when changes detected

### Schema Comparison Logic
```typescript
private schemasEqual(a: Table[], b: Table[]): boolean {
  if (a.length !== b.length) return false;
  const sortedA = [...a].sort((x, y) => x.name.localeCompare(y.name));
  const sortedB = [...b].sort((x, y) => x.name.localeCompare(y.name));
  for (let i = 0; i < sortedA.length; i++) {
    const colsA = sortedA[i].columns.map(c => `${c.name}:${c.type}`).sort().join(',');
    const colsB = sortedB[i].columns.map(c => `${c.name}:${c.type}`).sort().join(',');
    if (colsA !== colsB) return false;
  }
  return true;
}
```

### Icon Registration
- `lucideXCircle` - Error toast icon
- `lucideInfo` - Info toast icon
- Added to `provideIcons()` in `app.config.ts`

---

## 13. Profile Page Implementation

### Overview
Built user profile page (route `/profile`) with real API data, XP progress bar, stat grid, skills chips, and mission progress table.

### Backend Changes

**New endpoints (`UserController`):**
- `GET /api/users/me` — now returns `level` (computed from XP) and `createdAt`
- `GET /api/users/me/progress` — returns mission progress list
- `GET /api/users/me/skills` — returns aggregated skill tags

**Level formula (server-authoritative):**
```java
computeLevel(xp) = floor(sqrt(xp / 100)) + 1
```

**Level added to `UserDto.ProfileResponse`:**
```java
public record ProfileResponse(UUID id, String username, String email, int xp, int level,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt) {}
```

**Files Modified:**
- `infrastructure/adapter/in/web/dto/UserDto.java` — Added `level` field + `@JsonFormat`
- `infrastructure/adapter/in/web/dto/ProgressDto.java` — Added `@JsonFormat` on `completedAt`
- `infrastructure/adapter/in/web/UserController.java` — Added `getProfile()`, `getProgress()`, `getSkills()`
- `domain/model/User.java` — Made `createdAt` final, removed `@RequiredArgsConstructor` (Lombok conflict)
- `application/usecase/RegisterUserService.java` — Passes `LocalDateTime.now()` instead of `null`

**Lombok fix for `createdAt`:**
- `User` had both `@AllArgsConstructor` + `@RequiredArgsConstructor` — with mixed final/non-final fields, the generated all-args constructor could skip non-final `createdAt` during constructor resolution
- Fix: made `createdAt` final, removed `@RequiredArgsConstructor`, kept `@AllArgsConstructor` alone
- `RegisterUserService` now passes `LocalDateTime.now()` instead of `null`

### Frontend Changes

**Models (`user.model.ts`):**
```typescript
export interface UserResponse {
  id: string; username: string; email: string;
  xp: number; level: number;
  createdAt?: string;
}
```

**ProfileService (`core/profile.service.ts`):**
- `fetchProfile()` — `forkJoin` over 3 endpoints (user, progress, skills)
- `xpProgress()` — returns `{ current, next, percentage }` computed from user XP

**ProfileComponent (`features/profile/`):**
- Uses signals: `profile`, `progress`, `skills`, `loading`, `error`
- `@let prof = profile()` in template for clean signal access
- Layout per STYLING_GUIDE_v3 §5: `max-w-6xl mx-auto px-5 py-8`
- XP bar with animated fill
- Stat grid: Solved / XP / Joined date
- Skills displayed as tag chips
- Mission progress table (name, completed, date)

**Header (`shared/header/`):**
- Added `refreshProfile()` — fetches `/users/me` on each `NavigationEnd`
- `file:///C:/Users/David/Documents/coding/tcc/sqlab-client/src/app/shared/header/header.component.ts` — profile fetch on navigation
- Stats now show real data (solved count, XP, level)

**Route (`app.routes.ts`):**
```typescript
{ path: 'profile', canActivate: [authGuard],
  loadComponent: () => import('./features/profile/profile.component')
    .then(m => m.ProfileComponent) },
```

**AuthService:**
- User creation after login/register now creates with `xp: 0, level: 1`

### Known Issues / TODO
- [x] Missions `/missions/:id` page implemented
- [ ] Error handling improvements

---

## 16. Light/Dark Mode Theme Toggle + Cursor Pointer

### Date: 2026-05-08

### Overview
Added a light/dark mode theme toggle button to the app header with persistence and system preference detection. Applied `cursor: pointer` globally to all clickable elements. Adjusted light theme contrast.

### Files Changed

#### Theme System
- `sqlab-client/src/styles.css`
  - Added CSS custom properties for glow/gradient colors (`--glow-*`, `--mesh-*`)
  - Added `html[data-theme="light"]` block with all 17 color token overrides
  - Body color transition (respects `prefers-reduced-motion`)
  - Added `:not(:disabled)` cursor-pointer rule for buttons, links, `[role="button"]`, clickable inputs, select in `@layer base`

- `sqlab-client/src/index.html` — Flash-prevention inline script in `<head>` reads localStorage and sets `data-theme` before Angular bootstraps

- `sqlab-client/src/app/core/theme.service.ts` — Created ThemeService with Angular Signals (`theme`, `isDark`, `isLight`), `toggle()`, `init()` checking localStorage → `prefers-color-scheme` → default `'dark'`

- `sqlab-client/src/app/app.config.ts` — Registered `lucideSun`, `lucideMoon` in `provideIcons`

#### Header
- `sqlab-client/src/app/shared/header/header.component.ts` — Injected ThemeService, added `isLight` computed, imported NgIconsModule; removed dead code (`showBackLink`, `currentPath`)
- `sqlab-client/src/app/shared/header/header.component.html` — Added round toggle button in `<nav>` with sun/moon Lucide icons and `text-accent` color; removed `@if (showBackLink)` back-link block

#### Back-link Relocation
- `sqlab-client/src/app/features/mission/mission.component.html` — Added "← Missions" back-link in both loading and loaded navigator strips

#### Light Theme Contrast Adjustments
- `sqlab-client/src/styles.css` — Light theme foreground darkened to `hsl(220 15% 10%)`, muted-foreground to `hsl(220 10% 30%)` for better readability
- Note: accent colors (primary, secondary, accent, destructive) were adjusted then reverted to original values per user preference

#### Mission Page Container Colors (Light Theme)
Elements changed from muted backgrounds to `bg-card` (white in light mode):
- Mission & Schema tab header bar: `bg-muted/50` → `bg-card`
- Tables Available container: `bg-muted/30` → `bg-card`  
- SQL editor container: `bg-editor` → `bg-card`
- Restore/Run/Verify buttons: `bg-muted/*` → `bg-card`

### Light Theme Color Tokens (Current)
| Token | Value |
|---|---|
| `--color-background` | hsl(220 20% 97%) |
| `--color-foreground` | hsl(220 15% 10%) |
| `--color-card` | hsl(0 0% 100%) |
| `--color-muted` | hsl(220 15% 92%) |
| `--color-muted-foreground` | hsl(220 10% 30%) |
| `--color-border` | hsl(220 15% 85%) |
| `--color-primary` | hsl(165 70% 35%) |
| `--color-secondary` | hsl(250 55% 52%) |
| `--color-accent` | hsl(35 90% 50%) |

### Key Decisions
- Used `data-theme="light"` on `<html>` with unlayered CSS overrides (higher priority than `@layer theme`)
- `dataset['theme']` (bracket notation) required in TypeScript — `data-theme` not a known DOMStringMap property
- Glow/gradient utilities refactored to CSS vars so they respond to theme changes
- `cursor: pointer` applied globally via CSS rule, not per-element — respects `:disabled`
- Text color consistency attempt was reverted — colored text (primary/secondary/accent) preserved for visual design

---

## 14. Mission Model: Added objective + hint fields

### Date: 2026-05-08

### Problem
Mission model had no separate narrative context (`briefing` served both as description and objective) and no hint system for users.

### Field Semantics
| Field | Role | Required |
|-------|------|----------|
| `title` | Mission name | ✅ |
| `briefing` | Narrative context / story | ✅ (repurposed) |
| `objective` | The explicit task to complete | ✅ NEW |
| `hint` | Key SQL command(s) to solve the mission | ❌ NEW |

### Backend Changes

**Schema (V1__init_schema.sql):**
- Added `objective TEXT NOT NULL` and `hint TEXT` columns to `missions` table

**Seed data (V2__seed_missions.sql):**
- Added `objective` and `hint` values to all 8 seed missions
- Column list updated: `id, title, briefing, objective, hint, ddl_script, ...`

**Domain model (`Mission.java`):**
- Added `objective` and `hint` fields
- Switched from `@RequiredArgsConstructor` to `@AllArgsConstructor`

**JPA Entity (`MissionJpaEntity.java`):**
- Added `objective` (TEXT, NOT NULL) and `hint` (TEXT) columns

**Mapper (`MissionMapper.java`):**
- Maps `entity.getObjective()` and `entity.getHint()` in `toDomain()`

**DTO (`MissionDto.java`):**
- `MissionResponse` now includes `objective` and `hint`
- `MissionSummary` unchanged (list view doesn't need these)

**Controller (`MissionController.java`):**
- `toResponse()` maps `m.getObjective()` and `m.getHint()`

### Frontend Changes

**Model (`mission.model.ts`):**
- `Mission` interface: added `objective: string`, `hint?: string`

**Mission tabs (`mission-tabs.component.ts`):**
- Local `Mission` interface: added `objective: string` (already had `hint?: string`)

**Mission tabs template (`mission-tabs.component.html`):**
- Subtitle: `m.briefing` (unchanged — now the narrative)
- Objective card: changed from `m.briefing` to `m.objective`
- Hint section already existed in template (collapsible amber card)

---

## 15. JWT Auth: 401 Redirect on Invalid/Expired Token

### Date: 2026-05-08

### Problem
Backend returned **403 Forbidden** (Spring Security default) for invalid/expired tokens instead of 401. The frontend interceptor checked for `error.status === 401` so it never triggered. Errors also propagated to components after logout, causing confusing error UIs.

### Backend Changes

**New: `JwtAuthenticationEntryPoint.java`**
```java
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write("{\"status\":401,\"message\":\"Invalid or expired token\"}");
    }
}
```

**Modified: `SecurityConfig.java`**
- Injected `JwtAuthenticationEntryPoint`
- Added `.exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint))`

### Frontend Changes

**Modified: `auth-error.interceptor.ts`**
- Simplified: removed dead refresh-token logic (refresh token stored as `''` — always returned false)
- Now catches both `401` and `403` on non-auth endpoints
- Calls `authService.logout()` (clears localStorage, navigates to `/login`)
- Returns `EMPTY` instead of re-throwing — components never see the auth error
```typescript
catchError((error: HttpErrorResponse) => {
    if ((error.status === 401 || error.status === 403) && !req.url.includes('/auth/')) {
        authService.logout();
        return EMPTY;
    }
    return throwError(() => error);
});
```

---

## 17. Mission Content Refactor: Creative Overhaul

### Date: 2026-05-09

### Problem
Seed missions were dry, generic exercises. The `briefing` and `objective` fields were nearly identical — both essentially described the SQL task. Missions lacked narrative immersion and failed to make learning feel like solving real problems.

### Design Principle (Briefing vs Objective)
| Field | Role | Example |
|-------|------|---------|
| `briefing` | **Narrative / stakes / atmosphere** — paints a scene, gives the "why" | A body was found at 3AM. The detective needs the nightclub log. |
| `objective` | **Precise SQL task** — what columns, what conditions, what technique | List all records from the nightclub_log table. |

Both fields are in Brazilian Portuguese. The `briefing` reads like a cinematic setup; the `objective` reads like a technical specification.

### What Changed
- **8 → 10 missions** (added BIOLOGY theme — was unused)
- Every mission rewritten with distinct cinematic briefings
- Distribution: CRIMINAL (3), FINANCE (2), ASTRONOMY (2), CYBERSECURITY (1), BIOLOGY (2)

### New Mission Table

| # | Title | Theme | Diff | Teaches | Briefing vibe |
|---|-------|-------|------|---------|---------------|
| 1 | O Último Gole | CRIMINAL | B | SELECT | Noir detective, body at the Blue Moon Cabaret |
| 2 | Madrugada Suspeita | CRIMINAL | B | SELECT, WHERE | Detective wants night owls, cigarette smoke |
| 3 | Teia de Mentiras | CRIMINAL | I | SELECT, INNER JOIN | Interrogations, a web of lies |
| 4 | O Gavião e o Urubu | FINANCE | B | SELECT, WHERE (boolean) | Embezzlement, trembling accountant |
| 5 | Sinais do Cosmos | ASTRONOMY | I | SELECT, ORDER BY | Radio telescope, alien signal mystery |
| 6 | A Fortuna do Submundo | FINANCE | A | GROUP BY, HAVING, SUM, AVG | Money laundering by branch |
| 7 | Mapa Estelar | ASTRONOMY | A | GROUP BY, HAVING, COUNT | Celestial mapping, pre-1950 discoveries |
| 8 | O Fantasma na Matrix | CYBERSECURITY | E | GROUP BY, HAVING, COUNT | 3AM breach, internal threat |
| 9 | O Surto | BIOLOGY | I | INNER JOIN, WHERE | Outbreak, severe symptoms triage |
| 10 | A Cura em Gotas | BIOLOGY | E | UPDATE, WHERE, SELECT | Expired meds during epidemic |

### File Changed
- `sqlab-api/src/main/resources/db/migration/V2__seed_missions.sql` — Complete rewrite of all 10 seed missions

### Key Decisions
- All table names, column names, and data remained self-contained per mission (no cross-mission dependencies)
- `expected_result` JSON validated against actual DML output
- Difficulty mapped to SQL concepts: B → SELECT/filter, I → JOIN/ORDER, A → GROUP BY/aggregation, E → HAVING/subquery + DML
- Briefings written in Brazilian Portuguese with vivid sensory details (time, weather, character reactions, objects)
- Objectives intentionally clinical — stripped of narrative, focused on columns/tables/conditions

---

## 18. Scenario Mechanic + Code Review Fixes

### Date: 2026-05-09

### Overview
Added ordered mission groupings (scenarios) with sequential unlocking and continuous narrative. 19 tasks created 21 files across the full stack. Followed by a senior dev code review that identified 10 issues (4 critical, 6 important, minor) — all fixed.

### Design Principles
- **No join table** — `scenario_id` + `order_index` directly on `missions` table, KISS
- **No ScenarioLockService** — unlock check is inline in services (5 lines)
- **Backend-enforced lock** — 403 with `{"code":"MISSION_LOCKED"}` for locked missions
- **Sequential unlocking** — must complete mission N to unlock N+1 within a scenario
- **Standalone missions unchanged** — scenario fields nullable, no impact on non-scenario missions

### Database Changes

**V1 migration** (`V1__init_schema.sql`):
- New `scenarios` table: `id UUID PK`, `title VARCHAR(100)`, `description TEXT`, `theme VARCHAR(20)`, `created_at TIMESTAMP`
- ALTER `missions`: added `scenario_id UUID FK → scenarios(id) ON DELETE CASCADE`, `order_index INTEGER`
- `UNIQUE (scenario_id, order_index)` — prevents duplicate positions
- `CHECK (scenario_id IS NULL OR order_index IS NOT NULL)` — prevents orphaned order_index

**V2 migration** (`V2__seed_missions.sql`):
- All 10 missions changed from `gen_random_uuid()` to fixed UUIDs (`000...001` to `000...010`)
- Seed scenario "Noite no Blue Moon" (UUID `000...0a1`, theme CRIMINAL)
- UPDATE missions 1-3 to belong to this scenario with `order_index` 1, 2, 3

### Backend Architecture

**New files (11):**
- `domain/model/Scenario.java` — Domain entity (id, title, description, theme)
- `domain/exception/MissionLockedException.java` — carries missionId, scenarioId, scenarioTitle
- `domain/exception/ScenarioNotFoundException.java` — 404 for missing scenarios
- `application/port/in/GetScenariosUseCase.java` — Use case interface
- `application/port/out/ScenarioRepository.java` — Output port (findAll, findById)
- `application/usecase/GetScenariosService.java` — Service implementation
- `infrastructure/.../entity/ScenarioJpaEntity.java` — JPA entity
- `infrastructure/.../repository/ScenarioJpaRepository.java` — Spring Data repo
- `infrastructure/.../mapper/ScenarioMapper.java` — Entity → domain mapper
- `infrastructure/.../ScenarioPersistenceAdapter.java` — Port adapter
- `infrastructure/.../web/ScenarioController.java` — REST controller (`/api/scenarios`, `/api/scenarios/{id}`)
- `infrastructure/.../web/dto/ScenarioDto.java` — ScenarioSummary, ScenarioMissionItem, ScenarioDetail

**Modified files (12):**
- `domain/model/Mission.java` — Added scenarioId, orderIndex, scenarioTitle fields
- `MissionJpaEntity.java` — Added scenario_id, order_index columns + @ManyToOne to ScenarioJpaEntity
- `MissionMapper.java` — Maps scenario fields
- `MissionJpaRepository.java` — Added `findByScenarioIdOrderByOrderIndex`, `findByScenarioIdAndOrderIndex`, `countByScenarioId`
- `MissionPersistenceAdapter.java` — Implements new repo methods + `isPreviousMissionCompleted` (now checks `completed` flag)
- `MissionRepository.java` — Added `findByScenarioIdOrderByOrderIndex`, `isPreviousMissionCompleted`, `countByScenarioId`
- `ProgressRepository.java` — Added `findCompletedMissionIdsByUserId` (batch query)
- `ProgressJpaRepository.java` — Added `existsByUserIdAndMissionIdAndCompleted`
- `ProgressPersistenceAdapter.java` — Implements batch progress fetch
- `GetMissionsUseCase.java` — Added `userId` to `FindByIdQuery`, added `MissionDetail` record + `handleDetail()` method
- `GetMissionsService.java` — Lock check (throws 403), computes `scenarioTotalMissions` via `countByScenarioId`
- `ValidateMissionService.java` — Lock check before validating
- `MissionController.java` — Passes userId, uses `handleDetail()` (no direct out-port dependency)
- `MissionDto.java` — MissionResponse (+scenarioId, scenarioTitle, scenarioOrderIndex, scenarioTotalMissions), MissionSummary (+scenarioId), @JsonInclude(NON_NULL)
- `GlobalExceptionHandler.java` — Handles MissionLockedException (403 with scenarioId), ScenarioNotFoundException (404)
- `GetScenariosService.java` — Uses `ScenarioNotFoundException` instead of `NoSuchElementException`

### Frontend Changes

**New files (2 single-file components):**
- `features/scenario/scenario-list.component.ts` — Grid of scenario cards with progress bars, theme badges
- `features/scenario/scenario-detail.component.ts` — Narrative description, progress bar, vertical mission list with ✅/▶/🔒 status icons

**Modified files (8):**
- `core/models/mission.model.ts` — Added ScenarioDetail, ScenarioMissionItem, ScenarioSummary types; Mission/MissionSummary gain optional scenario fields
- `core/mission.service.ts` — Added `getScenarios()`, `getScenario()`
- `app.routes.ts` — Added `/scenarios` and `/scenarios/:id` with authGuard
- `shared/header/header.component.html` — Added "Dashboard" and "Scenarios" nav links between logo and theme toggle
- `features/dashboard/dashboard.component.html` — Scenario mission cards link to `/scenarios/:id`, show "Scenario" badge
- `features/mission/mission.component.ts` — Handles 403 MISSION_LOCKED (lock screen with scenarioId for "Back to Scenario" navigation), loads scenario missions for prev/next
- `features/mission/mission.component.html` — Lock screen (lock icon + message + "Back to Scenario" button), scenario breadcrumb in nav strip

### Frontend Lock Flow
1. User navigates to `/mission/:id` for a locked (non-completed, non-first) scenario mission
2. Backend returns 403 with `{"code":"MISSION_LOCKED","scenarioId":"...","message":"..."}`
3. Frontend catches the 403 → shows lock screen with lock icon, explanation message, and "Back to Scenario" button
4. Scenario detail page prevents clicking LOCKED missions (cursor: not-allowed, click guard on `navigateToMission`)

### Routes and Navigation
| Path | Component | Guard | Description |
|------|-----------|-------|-------------|
| `/scenarios` | ScenarioListComponent | authGuard | Scenario catalog with progress |
| `/scenarios/:id` | ScenarioDetailComponent | authGuard | Scenario detail + mission list |
| `/mission/:id` | MissionComponent | authGuard | Lock screen if mission locked |

### Code Review: 10 Issues Fixed

#### Critical (4)
1. **scenarioId missing from 403 response** — Added to `MissionLockedException` + `GlobalExceptionHandler` response body. Frontend now navigates to specific scenario, not just `/scenarios` list.
2. **N+1 queries in ScenarioController** — Replaced per-mission `existsByUserIdAndMissionId()` with batch `findCompletedMissionIdsByUserId()` — single query per endpoint.
3. **Loading all missions just for `.size()`** — Added `countByScenarioId()` to repository, used in `GetMissionsService` instead of loading full mission list.
4. **Architecture violation (controller using out-port)** — Moved `scenarioTotalMissions` to `GetMissionsService.handleDetail()`, removed `MissionRepository` from `MissionController`.

#### Important (4)
5. **500 instead of 404 for missing scenario** — Created `ScenarioNotFoundException`, added to `GlobalExceptionHandler` 404 handler.
6. **No null guard on `@AuthenticationPrincipal`** — Added `userId != null ? UUID.fromString(userId) : null` in `ScenarioController`.
7. **Progress check ignoring `completed` flag** — Changed to `existsByUserIdAndMissionIdAndCompleted(..., true)` in `MissionPersistenceAdapter`.
8. **`NoSuchElementException` instead of domain exception** — Replaced with `ScenarioNotFoundException`.

#### Minor (2)
9. **Null fields serializing for standalone missions** — Added `@JsonInclude(NON_NULL)` to `MissionResponse`/`MissionSummary`.
10. **Redundant signal calls in template** — Changed `mission()` to `mission` alias inside `@if as` block.

### Compilation
- Backend compiles cleanly: `BUILD SUCCESS` (62 source files, JDK 25, Maven 3.9.12)
- Pre-existing issue: `$JAVA_HOME` pointed to JDK 21, fixed by running `JAVA_HOME=/path/to/zulu25-jdk/current mvn compile`
```