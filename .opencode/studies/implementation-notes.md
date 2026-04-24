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

- [ ] Dashboard styling not implemented
- [ ] Login/Register password confirm validation in backend
- [ ] Error handling could be more user-friendly

---

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