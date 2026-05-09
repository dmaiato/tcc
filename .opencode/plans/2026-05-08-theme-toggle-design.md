# Theme Toggle — Design Doc (Revised)

**Date:** 2026-05-08
**Project:** SQLab-Client (Angular 21)
**Status:** Approved

---

## 1. Overview

Add a light/dark mode toggle button to the global header. Dark mode is the default; light mode can be toggled manually with persistence and system preference fallback.

---

## 2. Mechanism

### 2.1 CSS Strategy: `data-theme` on `<html>`, Unlayered Override

**Dark mode is default** (no attribute, matches current behavior).
**Light mode** activates via `data-theme="light"` on `<html>`.

#### How Tailwind v4 Works (verified from compiled output):

Tailwind v4 emits `@theme` values inside cascade layers:
```css
@layer theme {
  :root, :host {
    --color-background: hsl(230 25% 9%);
    /* ... all color tokens */
  }
}
@layer base { /* base styles + our border-color rule */ }
@layer utilities { /* glow-success, gradient-mesh, etc. */ }
```

**Key rule**: Unlayered CSS overrides ALL layered CSS regardless of specificity. So adding `html[data-theme="light"]` as an **unlayered** block will correctly override the `@layer theme` variables.

### 2.2 Glow/Gradient Utilities Must Be Parameterized

Current glow/gradient utilities use hardcoded HSL — they won't respond to `data-theme`. Fix: extract the HSL values into CSS variables.

**New variables on `:root`:**
```css
:root {
  --glow-primary: hsl(165 80% 48% / 0.2);
  --glow-success-1: hsl(165 80% 48% / 0.3);
  --glow-success-2: hsl(165 80% 48% / 0.1);
  --glow-error-1: hsl(0 72% 55% / 0.3);
  --glow-error-2: hsl(0 72% 55% / 0.1);
  --glow-accent: hsl(35 95% 60% / 0.2);
  --mesh-secondary: hsl(250 60% 62% / 0.08);
  --mesh-primary: hsl(165 80% 48% / 0.06);
  --mesh-accent: hsl(35 95% 60% / 0.04);
}
```

**Utilities reference variables:**
```css
.glow-success { box-shadow: 0 0 20px var(--glow-success-1), 0 0 60px var(--glow-success-2); }
.gradient-mesh { background: radial-gradient(... var(--mesh-secondary) ...) ...; }
```

Then `html[data-theme="light"]` overrides with lighter-compatible values.

### 2.3 Light Mode HSL Palette

Add unlayered block at end of `styles.css`:

```css
html[data-theme="light"] {
  --color-background: hsl(220 20% 97%);
  --color-foreground: hsl(220 15% 15%);
  --color-card: hsl(0 0% 100%);
  --color-card-foreground: hsl(220 15% 15%);
  --color-popover: hsl(0 0% 100%);
  --color-popover-foreground: hsl(220 15% 15%);
  --color-primary: hsl(165 70% 35%);
  --color-primary-foreground: hsl(0 0% 100%);
  --color-secondary: hsl(250 55% 52%);
  --color-secondary-foreground: hsl(0 0% 100%);
  --color-muted: hsl(220 15% 92%);
  --color-muted-foreground: hsl(220 10% 45%);
  --color-accent: hsl(35 90% 50%);
  --color-accent-foreground: hsl(0 0% 100%);
  --color-destructive: hsl(0 72% 50%);
  --color-destructive-foreground: hsl(0 0% 100%);
  --color-border: hsl(220 15% 85%);
  --color-input: hsl(220 15% 90%);
  --color-ring: hsl(165 70% 40%);
  --color-editor: hsl(220 15% 94%);
  --color-surface-raised: hsl(0 0% 97%);

  --glow-success-1: hsl(165 70% 35% / 0.25);
  --glow-success-2: hsl(165 70% 35% / 0.08);
  --glow-error-1: hsl(0 72% 50% / 0.25);
  --glow-error-2: hsl(0 72% 50% / 0.08);
  --glow-primary: hsl(165 70% 35% / 0.15);
  --glow-accent: hsl(35 90% 50% / 0.15);
  --mesh-secondary: hsl(250 55% 52% / 0.06);
  --mesh-primary: hsl(165 70% 35% / 0.08);
  --mesh-accent: hsl(35 90% 50% / 0.04);
}
```

---

## 3. ThemeService

**File:** `src/app/core/theme.service.ts`

```typescript
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly theme = signal<'dark' | 'light'>('dark');
  readonly isDark = computed(() => this.theme() === 'dark');
  readonly isLight = computed(() => this.theme() === 'light');

  constructor() {
    this.init();
  }

  private init(): void {
    let t = localStorage.getItem('sqlab-theme') as 'dark' | 'light' | null;
    if (!t) {
      t = window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
    }
    this.apply(t);
  }

  toggle(): void {
    this.apply(this.isDark() ? 'light' : 'dark');
  }

  private apply(t: 'dark' | 'light'): void {
    this.theme.set(t);
    if (t === 'light') {
      document.documentElement.dataset.theme = 'light';
    } else {
      delete document.documentElement.dataset.theme;
    }
    localStorage.setItem('sqlab-theme', t);
  }
}
```

No `APP_INITIALIZER` needed — the constructor runs early enough, and the flash-prevention script in `index.html` handles the pre-Angular bootstrap moment.

---

## 4. Flash Prevention Script

**File:** `src/index.html` — add before `</head>`:

```html
<script>
  (function() {
    var t = localStorage.getItem('sqlab-theme');
    if (!t && window.matchMedia('(prefers-color-scheme: light)').matches) t = 'light';
    if (t === 'light') document.documentElement.dataset.theme = 'light';
  })();
</script>
```

---

## 5. Header Toggle Button

**File:** `src/app/shared/header/header.component.ts`

- Inject `ThemeService`
- Add `isLight = computed(() => this.themeService.isLight())`

**File:** `src/app/shared/header/header.component.html`

Add before the auth controls `<nav>` block:
```html
<button (click)="themeService.toggle()" type="button"
  class="p-2 rounded-lg hover:bg-muted/30 transition-colors outline-none focus-visible:ring-2 focus-visible:ring-ring"
  [attr.aria-label]="isLight() ? 'Switch to dark mode' : 'Switch to light mode'">
  @if (isLight()) {
    <!-- Moon icon -->
    <svg ...>...</svg>
  } @else {
    <!-- Sun icon -->
    <svg ...>...</svg>
  }
</button>
```

---

## 6. Smooth Transition

Add to `styles.css` base layer:
```css
body {
  transition: background-color 0.3s ease, color 0.3s ease;
}
*, *::before, *::after {
  transition: border-color 0.3s ease, background-color 0.3s ease, color 0.3s ease;
}
```

Limit to `prefers-reduced-motion` respect.

---

## 7. Files Changed

| File | Change |
|---|---|
| `src/styles.css` | Add `:root` glow/gradient CSS variables; refactor glow/gradient utilities; add `html[data-theme="light"]` block; add transition styles |
| `src/index.html` | Add flash-prevention `<script>` in `<head>` |
| `src/app/core/theme.service.ts` | **NEW** — Theme management with signal, toggle, persistence |
| `src/app/shared/header/header.component.ts` | Inject `ThemeService`, add `isLight` computed |
| `src/app/shared/header/header.component.html` | Add theme toggle button before auth controls |

---

## 8. Not Changing

- No other component templates or styles — CSS variable cascade handles everything
- No backend changes
- No route changes
- Existing dark mode behavior preserved identically
