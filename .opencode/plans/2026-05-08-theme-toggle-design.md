# Theme Toggle — Design Doc

**Date:** 2026-05-08
**Project:** SQLab-Client (Angular 21)
**Status:** Approved design

---

## 1. Overview

Add a light/dark mode toggle button to the global header. The app currently supports dark mode only; this adds full light mode support with persistence and system preference detection.

---

## 2. Mechanism

### 2.1 CSS Strategy: `data-theme` on `<html>`

- **Dark mode is default** (no attribute, matches current behavior)
- **Light mode** activates via `data-theme="light"` on `<html>`
- Override color variables in a `html[data-theme="light"]` block in `styles.css`
- All components using Tailwind utility classes (`bg-background`, `text-foreground`, etc.) update automatically — zero template changes needed

### 2.2 Light Mode HSL Palette

Override these variables under `html[data-theme="light"]`:

| Token | Dark (current) | Light |
|---|---|---|
| `--color-background` | `hsl(230 25% 9%)` | `hsl(220 20% 97%)` |
| `--color-foreground` | `hsl(220 20% 92%)` | `hsl(220 15% 15%)` |
| `--color-card` | `hsl(230 22% 12%)` | `hsl(0 0% 100%)` |
| `--color-popover` | `hsl(230 22% 12%)` | `hsl(0 0% 100%)` |
| `--color-primary` | `hsl(165 80% 48%)` | `hsl(165 70% 35%)` |
| `--color-primary-foreground` | `hsl(230 25% 9%)` | `hsl(0 0% 100%)` |
| `--color-secondary` | `hsl(250 60% 62%)` | `hsl(250 55% 52%)` |
| `--color-muted` | `hsl(230 18% 16%)` | `hsl(220 15% 92%)` |
| `--color-muted-foreground` | `hsl(220 15% 55%)` | `hsl(220 10% 45%)` |
| `--color-accent` | `hsl(35 95% 60%)` | `hsl(35 90% 50%)` |
| `--color-accent-foreground` | `hsl(230 25% 9%)` | `hsl(0 0% 100%)` |
| `--color-destructive` | `hsl(0 72% 55%)` | `hsl(0 72% 50%)` |
| `--color-border` | `hsl(230 15% 20%)` | `hsl(220 15% 85%)` |
| `--color-input` | `hsl(230 18% 16%)` | `hsl(220 15% 90%)` |
| `--color-ring` | `hsl(165 80% 48%)` | `hsl(165 70% 40%)` |
| `--color-editor` | `hsl(230 25% 7%)` | `hsl(220 15% 94%)` |
| `--color-surface-raised` | `hsl(230 20% 14%)` | `hsl(0 0% 97%)` |

### 2.3 Custom Utilities in Light Mode

Update `gradient-mesh`, glow shadows, and the `* { border-color }` base layer to reference CSS variables so they respond to the theme switch.

---

## 3. ThemeService

**File:** `src/app/core/theme.service.ts`

```typescript
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly theme = signal<'dark' | 'light'>('dark');
  readonly isDark = computed(() => this.theme() === 'dark');
  readonly isLight = computed(() => this.theme() === 'light');

  toggle(): void { ... }
  setTheme(t: 'dark' | 'light'): void { ... }
  init(): void { ... }
}
```

### 3.1 `init()` logic:

1. Check `localStorage.getItem('sqlab-theme')`
2. If found, apply that theme
3. If not, check `window.matchMedia('(prefers-color-scheme: light)').matches`
4. If system prefers light, apply light
5. Otherwise, default to dark

### 3.2 `setTheme()`:

1. Update signal
2. Set/remove `document.documentElement.dataset.theme`
3. Persist to localStorage

### 3.3 Registration:

Add via `APP_INITIALIZER` in `app.config.ts` so theme is set before first paint.

---

## 4. Header Toggle Button

**File:** `src/app/shared/header/header.component.html`

Button placed before auth controls in `<nav>`:
- Dark mode → Sun icon (`lucideSun`)
- Light mode → Moon icon (`lucideMoon`)
- Styled like other header buttons: `rounded-lg hover:bg-muted/30 transition-colors`, focus ring
- Single click toggles — no dropdown

**File:** `src/app/shared/header/header.component.ts`
- Inject `ThemeService`
- Expose `isLight` for template binding

---

## 5. Flash Prevention

Add inline `<script>` in `index.html` `<head>` that runs before Angular boots:

```javascript
(function() {
  var theme = localStorage.getItem('sqlab-theme');
  if (!theme && window.matchMedia('(prefers-color-scheme: light)').matches) {
    theme = 'light';
  }
  if (theme === 'light') {
    document.documentElement.dataset.theme = 'light';
  }
})();
```

---

## 6. Files Changed

| File | Change |
|---|---|
| `src/styles.css` | Add `html[data-theme="light"]` block with light HSL overrides; update glow/gradient utilities to use CSS variables |
| `src/index.html` | Add flash-prevention script in `<head>` |
| `src/app/core/theme.service.ts` | NEW — Theme management service |
| `src/app/app.config.ts` | Register `APP_INITIALIZER`, add `lucideSun`, `lucideMoon` icons |
| `src/app/shared/header/header.component.ts` | Inject `ThemeService`, expose theme state |
| `src/app/shared/header/header.component.html` | Add toggle button before auth controls |

---

## 7. Not Changing

- No component templates or styles outside the header
- No backend changes
- No route changes
- Existing dark mode behavior is preserved exactly
