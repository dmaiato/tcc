# Theme Toggle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a light/dark mode toggle button to the app header with persistence and system preference detection.

**Architecture:** CSS-driven theme via `data-theme="light"` on `<html>`. Tailwind v4 `@theme` tokens (emitted in `@layer theme`) are overridden by an unlayered `html[data-theme="light"]` block. Glow/gradient utilities are refactored to use CSS variables so they respond to theme changes. A `ThemeService` manages toggle state via Angular Signals, with localStorage persistence and `prefers-color-scheme` fallback. A flash-prevention inline script in `index.html` handles the pre-bootstrap moment.

**Tech Stack:** Angular 21, Tailwind CSS v4, Angular Signals

---

### Task 1: Add custom CSS variables and refactor glow/gradient utilities

**Files:**
- Modify: `src/styles.css`

- [ ] **Step 1: Add `:root` CSS variables for glow and gradient colors**

Before the `@layer utilities` section, add these `:root` variables:

```css
@layer base {
  * {
    border-color: hsl(var(--color-border));
  }
  body {
    background-color: hsl(var(--color-background));
    color: hsl(var(--color-foreground));
    font-family: var(--font-sans);
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }
  ::selection {
    background: hsl(165 80% 48% / 0.3);
  }
}

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

- [ ] **Step 2: Refactor glow utilities to use CSS variables**

Replace the hardcoded HSL values in glow utilities with `var()` references:

```css
@layer utilities {
  .glow-success {
    box-shadow: 0 0 20px var(--glow-success-1), 0 0 60px var(--glow-success-2);
  }
  .glow-error {
    box-shadow: 0 0 20px var(--glow-error-1), 0 0 60px var(--glow-error-2);
  }
  .glow-primary {
    box-shadow: 0 0 15px var(--glow-primary);
  }
  .glow-accent {
    box-shadow: 0 0 15px var(--glow-accent);
  }
  .gradient-mesh {
    background:
      radial-gradient(ellipse at 20% 50%, var(--mesh-secondary) 0%, transparent 50%),
      radial-gradient(ellipse at 80% 20%, var(--mesh-primary) 0%, transparent 50%),
      radial-gradient(ellipse at 50% 80%, var(--mesh-accent) 0%, transparent 50%);
  }
  .card-shine {
    position: relative;
    overflow: hidden;
  }
  .card-shine::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 50%;
    height: 100%;
    background: linear-gradient(90deg, transparent, hsl(0 0% 100% / 0.03), transparent);
    transition: left 0.5s ease;
  }
  .card-shine:hover::before {
    left: 100%;
  }
}
```

- [ ] **Step 3: Add smooth theme transition to body**

Add transition CSS after the existing body styles in `@layer base`:

```css
body {
  background-color: hsl(var(--color-background));
  color: hsl(var(--color-foreground));
  font-family: var(--font-sans);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  transition: background-color 0.3s ease, color 0.3s ease;
}
@media (prefers-reduced-motion: reduce) {
  body {
    transition: none;
  }
}
```

- [ ] **Step 4: Add the `html[data-theme="light"]` light mode overrides**

At the very end of `styles.css` (after all `@layer` blocks), add the unlayered light mode block:

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

Note: This block is intentionally **unlayered** (no `@layer` wrapper) so it overrides `@layer theme` tokens via cascade layer priority rules.

---

### Task 2: Add flash-prevention script to index.html

**Files:**
- Modify: `src/index.html`

- [ ] **Step 1: Add inline script in `<head>`**

Add before the `</head>` closing tag:

```html
<script>
  (function() {
    var t = localStorage.getItem('sqlab-theme');
    if (!t && window.matchMedia('(prefers-color-scheme: light)').matches) t = 'light';
    if (t === 'light') document.documentElement.dataset.theme = 'light';
  })();
</script>
```

This runs synchronously before any rendering, preventing a flash of dark mode when the user prefers light.

---

### Task 3: Create ThemeService

**Files:**
- Create: `src/app/core/theme.service.ts`

- [ ] **Step 1: Create the service file**

```typescript
import { Injectable, signal, computed } from '@angular/core';

export type Theme = 'dark' | 'light';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly theme = signal<Theme>('dark');
  readonly isDark = computed(() => this.theme() === 'dark');
  readonly isLight = computed(() => this.theme() === 'light');

  constructor() {
    this.init();
  }

  private init(): void {
    let t = localStorage.getItem('sqlab-theme') as Theme | null;
    if (!t) {
      t = window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
    }
    this.apply(t);
  }

  toggle(): void {
    this.apply(this.isDark() ? 'light' : 'dark');
  }

  private apply(t: Theme): void {
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

---

### Task 4: Add theme toggle button to header

**Files:**
- Modify: `src/app/shared/header/header.component.ts`
- Modify: `src/app/shared/header/header.component.html`

- [ ] **Step 1: Inject ThemeService in header component**

In `header.component.ts`, add the import and injection:

```typescript
import { Component, inject, signal, HostListener, ElementRef, computed } from '@angular/core';
import { ThemeService } from '../../core/theme.service';
```

Add after existing injections:
```typescript
readonly themeService = inject(ThemeService);
isLight = computed(() => this.themeService.isLight());
```

- [ ] **Step 2: Add the toggle button in the header template**

In `header.component.html`, add the toggle button before the `<nav>` element (after the back-link `@if` block):

```html
  @if (showBackLink) {
    <a routerLink="/" class="font-mono text-xs text-muted-foreground hover:text-primary transition-colors">
      ← Missions
    </a>
  }

  <button (click)="themeService.toggle()" type="button"
    class="p-2 rounded-lg hover:bg-muted/30 transition-colors outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background shrink-0"
    [attr.aria-label]="isLight() ? 'Switch to dark mode' : 'Switch to light mode'">
    @if (isLight()) {
      <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"></path>
      </svg>
    } @else {
      <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="4"></circle>
        <path d="M12 2v2"></path>
        <path d="M12 20v2"></path>
        <path d="m4.93 4.93 1.41 1.41"></path>
        <path d="m17.66 17.66 1.41 1.41"></path>
        <path d="M2 12h2"></path>
        <path d="M20 12h2"></path>
        <path d="m6.34 17.66-1.41 1.41"></path>
        <path d="m19.07 4.93-1.41 1.41"></path>
      </svg>
    }
  </button>

  <nav class="flex items-center gap-2">
```

---

### Task 5: Verify build

- [ ] **Step 1: Build the project**

```bash
cd sqlab-client && npx ng build --configuration development
```

Expected: Build succeeds with no errors (`Application bundle generation complete`).

- [ ] **Step 2: Quick visual check**

Open `dist/sqlab-client/browser/index.html` or run `npx ng serve` to verify the toggle appears in the header, toggles `data-theme` on `<html>`, and persists across reload.
