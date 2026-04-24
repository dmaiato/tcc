# SQLab Styling Guide — v2

> **Senior review note (changes from v1):**
> v1 was a solid first pass but read like a tour of *some* code rather than a contract for *all* code. This revision tightens the rules, removes claims that don't match the codebase, fills in gaps (success token, radius math, `cn()` helper, layout variants, a11y), and corrects two outright bugs. Items marked **🔧 fixed** were inaccurate in v1; **➕ added** are new sections; **⚠️ note** are caveats juniors miss.

---

## 1. Design Philosophy

A **gamified, dark-mode SQL training console**:

- **Cyber/terminal feel** — deep slate-blue background, neon-teal primary, monospace numerics.
- **Editorial gamification** — gradient avatars, soft glows, level badges, animated XP bars.
- **Density with breathing room** — small font sizes (`text-xs`, occasional `text-[10px]`), tight padding, but generous `rounded-xl` corners and subtle borders.

**Rule #1 — token discipline.** Never write raw color classes (`text-white`, `bg-black`, `bg-slate-900`, `text-red-500`) or hex/rgb literals in components. Always go through semantic tokens (`bg-background`, `text-foreground`, `text-primary`, `border-destructive/30`, …). The whole system depends on this — theming, opacity composition, and future light mode all break the moment a component hardcodes a color.

⚠️ **note:** `text-[9px]` looks cool but fails WCAG. Reserve sub-11px sizes for non-essential meta (badges, decorative counters), never for interactive labels or anything a screen reader user needs to read aloud.

---

## 2. Color System (HSL Tokens)

All colors live in `src/index.css` as CSS variables in **HSL space**, stored as **bare numbers** (no `hsl()` wrapper). This is what lets Tailwind compose alpha via `/` syntax (`bg-primary/10`, `border-primary/30`).

### 2.1 Core palette (authoritative — copy from `src/index.css`)

```css
:root {
  --background: 230 25% 9%;       /* page body */
  --foreground: 220 20% 92%;      /* default text */

  --card: 230 22% 12%;            /* lifted surface */
  --card-foreground: 220 20% 92%;

  --popover: 230 22% 12%;
  --popover-foreground: 220 20% 92%;

  --primary: 165 80% 48%;         /* neon teal — brand */
  --primary-foreground: 230 25% 9%;

  --secondary: 250 60% 62%;       /* violet */
  --secondary-foreground: 0 0% 100%;

  --muted: 230 18% 16%;           /* subtle panel bg */
  --muted-foreground: 220 15% 55%;

  --accent: 35 95% 60%;           /* warm orange/gold */
  --accent-foreground: 230 25% 9%;

  --destructive: 0 72% 55%;
  --destructive-foreground: 0 0% 100%;

  --border: 230 15% 20%;
  --input: 230 18% 16%;           /* form input bg */
  --ring: 165 80% 48%;            /* focus ring = primary */

  --radius: 0.75rem;              /* drives rounded-lg/md/sm scale */

  --success: 165 80% 48%;         /* alias of primary, used by ResultsPane */
  --success-glow: 165 80% 48%;    /* reserved for emphasis glows */
  --editor-bg: 230 25% 7%;        /* darker than --background, code surfaces */
  --surface-raised: 230 20% 14%;  /* between --card and --muted */
}
```

### 2.2 Semantic intent (🔧 fixed — added missing tokens)

| Token              | Use for                                                     |
|--------------------|-------------------------------------------------------------|
| `background`       | Page body                                                   |
| `foreground`       | Default text                                                |
| `card`             | Panels, dropdowns, dialogs                                  |
| `popover`          | Floating menus, tooltips with bodies                        |
| `muted`            | Inactive surfaces, subtle row backgrounds                   |
| `muted-foreground` | Secondary/meta text (labels, timestamps)                    |
| `primary`          | Brand actions, success, XP bar fill, focus ring             |
| `secondary`        | Alternate gradient stop, secondary stats                    |
| `accent`           | Highlights, level badges, gold/orange callouts              |
| `destructive`      | Errors, delete/sign-out actions                             |
| `border`           | All hairline borders (auto-applied to `*` in base layer)    |
| `input`            | Form input backgrounds                                      |
| `ring`             | Focus outlines (handled by shadcn `focus-visible:ring-ring`)|
| `success`          | Verified/passing states (mission solve, query OK)           |
| `editor` / `editor-bg` | SQL editor canvas background                            |
| `surface-raised`   | Slightly lifted surface above `card`                        |

### 2.3 Wiring tokens to Tailwind

In `tailwind.config.ts` every token is exposed as a Tailwind color so you can write `bg-primary`, `border-accent/30`, etc.:

```ts
colors: {
  border: "hsl(var(--border))",
  input: "hsl(var(--input))",
  ring: "hsl(var(--ring))",
  background: "hsl(var(--background))",
  foreground: "hsl(var(--foreground))",
  primary:     { DEFAULT: "hsl(var(--primary))",     foreground: "hsl(var(--primary-foreground))" },
  secondary:   { DEFAULT: "hsl(var(--secondary))",   foreground: "hsl(var(--secondary-foreground))" },
  destructive: { DEFAULT: "hsl(var(--destructive))", foreground: "hsl(var(--destructive-foreground))" },
  muted:       { DEFAULT: "hsl(var(--muted))",       foreground: "hsl(var(--muted-foreground))" },
  accent:      { DEFAULT: "hsl(var(--accent))",      foreground: "hsl(var(--accent-foreground))" },
  popover:     { DEFAULT: "hsl(var(--popover))",     foreground: "hsl(var(--popover-foreground))" },
  card:        { DEFAULT: "hsl(var(--card))",        foreground: "hsl(var(--card-foreground))" },
  success: "hsl(var(--success))",
  editor: "hsl(var(--editor-bg))",
  "surface-raised": "hsl(var(--surface-raised))",
}
```

**Why bare HSL numbers?** Tailwind generates `hsl(var(--primary) / <alpha-value>)` — letting you compose `bg-primary/10`, `border-primary/30`, etc. Storing colors as `hsl(165 80% 48%)` would break that.

---

## 3. Typography

Two Google fonts loaded at the top of `src/index.css`:

```css
@import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap');
```

Mapped in `tailwind.config.ts`:

```ts
fontFamily: {
  sans: ["'Space Grotesk'", "sans-serif"],   // headings, UI labels, prose
  mono: ["'JetBrains Mono'", "monospace"],   // numbers, meta, code, hints
}
```

### Conventions

- **`font-sans`** — page titles (`text-2xl font-bold tracking-tight`), button text, usernames, prose.
- **`font-mono`** — anything numeric (XP, level, counts), uppercase labels, timestamps, hint/code text. Often paired with `text-xs` or `text-[10px]`.
- `tracking-tight` on display headings; default tracking on body.
- Body sizes: `text-xs` (12px) and `text-sm` (14px) dominate. Page titles at `text-2xl`. Section subtitles at `text-xs font-mono text-muted-foreground`.

⚠️ **note:** the body font is set in the base layer (`body { @apply ... font-sans }`) — you only need to add `font-sans` when you've explicitly switched to `font-mono` and want to switch back inside a child.

---

## 4. Global Base Layer

```css
@layer base {
  * { @apply border-border; }                  /* default border color */
  body { @apply bg-background text-foreground font-sans antialiased; }
  ::selection { background: hsl(var(--primary) / 0.3); }
}
```

🔧 **fixed (v1 bug):** v1 documented `::selection` with a hardcoded `hsl(165 80% 48% / 0.3)`. That violates Rule #1. The codebase has been updated to use `hsl(var(--primary) / 0.3)`; the guide now matches.

The `* { @apply border-border }` reset is what makes `<div className="border" />` automatically pick up the brand border color — you almost never need to write `border-border` explicitly.

---

## 5. Custom Utilities (Glows & Mesh)

Defined in `@layer utilities` of `src/index.css`:

```css
.glow-success { box-shadow: 0 0 20px hsl(165 80% 48% / 0.3), 0 0 60px hsl(165 80% 48% / 0.1); }
.glow-error   { box-shadow: 0 0 20px hsl(0 72% 55% / 0.3),  0 0 60px hsl(0 72% 55% / 0.1); }
.glow-primary { box-shadow: 0 0 15px hsl(165 80% 48% / 0.2); }
.glow-accent  { box-shadow: 0 0 15px hsl(35 95% 60% / 0.2); }

.gradient-mesh {
  background:
    radial-gradient(ellipse at 20% 50%, hsl(250 60% 62% / 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, hsl(165 80% 48% / 0.06) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 80%, hsl(35 95% 60% / 0.04) 0%, transparent 50%);
}

.card-shine { position: relative; overflow: hidden; }
.card-shine::before {
  content: ''; position: absolute; top: 0; left: -100%;
  width: 50%; height: 100%;
  background: linear-gradient(90deg, transparent, hsl(0 0% 100% / 0.03), transparent);
  transition: left 0.5s ease;
}
.card-shine:hover::before { left: 100%; }
```

⚠️ **note:** these utilities currently inline raw HSL values (so they don't auto-react to token changes). That's a deliberate trade-off — `box-shadow` can't easily compose alpha from a Tailwind token. If you ever swap the brand hue, update these definitions in lockstep with the tokens.

**Where to use:**
- `.gradient-mesh` — full-page wrappers (`Index`, `Login`, `Register`, `Profile`, `Mission`, `Admin`) for the ambient teal/violet/gold glow.
- `.glow-primary` — pinned/active cards, primary CTAs, active tabs.
- `.glow-success` / `.glow-error` — result panes, test outcomes.
- `.card-shine` — interactive list cards (mission browser).

---

## 6. Animation Tokens

Defined in `tailwind.config.ts`. Use **sparingly** — entry animations only, never decorative loops on persistent UI (the one exception: `pulse-glow` for live status indicators).

```ts
keyframes: {
  "fade-in":    { "0%": { opacity:"0", transform:"translateY(10px)" }, "100%": { opacity:"1", transform:"translateY(0)" } },
  "scale-in":   { "0%": { transform:"scale(0.95)", opacity:"0" }, "100%": { transform:"scale(1)", opacity:"1" } },
  "pulse-glow": { "0%,100%": { opacity:"1" }, "50%": { opacity:"0.7" } },
  "row-appear": { from: { opacity:"0", transform:"translateY(4px)" }, to: { opacity:"1", transform:"translateY(0)" } },
},
animation: {
  "fade-in":    "fade-in 0.4s ease-out",
  "scale-in":   "scale-in 0.3s ease-out",
  "pulse-glow": "pulse-glow 2s ease-in-out infinite",
  "row-appear": "row-appear 0.2s ease-out forwards",
}
```

For richer page-level motion, use **`framer-motion`**:

```tsx
<motion.div initial={{ opacity: 0, y: -8 }} animate={{ opacity: 1, y: 0 }}>
```

Stagger lists with `transition={{ delay: i * 0.05 }}`. Cap delays at ~8 items so the last entry doesn't feel laggy.

---

## 7. Layout Variants (➕ added)

v1 implied a single page shell. The codebase actually uses **three** distinct layouts. Pick the right one:

### 7.A — Centered content page (Profile, Leaderboard, Admin)

```tsx
<div className="min-h-screen flex flex-col gradient-mesh">
  <TopBar />
  <main className="flex-1 px-6 py-8">
    <div className="max-w-5xl mx-auto">{/* content */}</div>
  </main>
</div>
```

### 7.B — Wider browser/landing (Index)

```tsx
<div className="min-h-screen flex flex-col gradient-mesh">
  <TopBar />
  <main className="flex-1 px-6 py-8">
    <div className="max-w-6xl mx-auto">{/* mission browser */}</div>
  </main>
</div>
```

### 7.C — Full-bleed workbench (MissionPage)

```tsx
<div className="h-screen flex flex-col overflow-hidden gradient-mesh">
  <TopBar />
  {/* resizable panels, no max-width, no scroll on root */}
</div>
```

### 7.D — Centered card (Login, Register)

```tsx
<div className="min-h-screen flex items-center justify-center gradient-mesh px-4">
  <div className="w-full max-w-md">{/* form card */}</div>
</div>
```

**Rule:** if a page has a max width, it's `max-w-5xl` for content, `max-w-6xl` for grids, `max-w-md` for auth forms. Don't invent new widths.

---

## 8. Building-Block Patterns

### 8.1 Section header with gradient icon tile

```tsx
<div className="flex items-center gap-3 mb-2">
  <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-accent to-primary
                  flex items-center justify-center">
    <Trophy className="w-5 h-5 text-accent-foreground" />
  </div>
  <div>
    <h1 className="text-2xl font-bold tracking-tight">Leaderboard</h1>
    <p className="font-mono text-xs text-muted-foreground">Top SQL operatives</p>
  </div>
</div>
```

Approved gradient pairs (don't invent new ones without a reason):

| Gradient                          | Use for                          |
|-----------------------------------|----------------------------------|
| `from-primary to-secondary`       | Default brand decoration         |
| `from-accent to-primary`          | Leaderboard, achievements        |
| `from-secondary to-primary`       | Profile, identity                |
| `from-destructive to-accent`      | Warnings, hero/danger CTAs       |
| `from-primary to-accent`          | Progress fills (XP bars)         |

### 8.2 Avatar with level badge

```tsx
<div className="relative">
  <div className={`w-8 h-8 rounded-lg bg-gradient-to-br ${avatarColor}
                   flex items-center justify-center text-sm font-bold
                   text-primary-foreground`}>
    {initial}
  </div>
  <div className="absolute -bottom-0.5 -right-0.5 w-4 h-4 rounded-full
                  bg-accent text-accent-foreground flex items-center justify-center
                  text-[8px] font-bold border-2 border-background">
    {level}
  </div>
</div>
```

`avatarColor` should resolve to one of the approved gradients above — never a raw Tailwind color.

### 8.3 XP / progress bar

```tsx
<div className="flex-1 h-1.5 rounded-full bg-muted overflow-hidden"
     role="progressbar" aria-valuenow={value} aria-valuemin={0} aria-valuemax={max}>
  <motion.div
    className="h-full rounded-full bg-gradient-to-r from-primary to-accent"
    initial={{ width: 0 }}
    animate={{ width: `${(value / max) * 100}%` }}
    transition={{ duration: 0.6 }}
  />
</div>
```

🔧 **fixed:** v1 omitted ARIA. Add `role="progressbar"` + `aria-value*` so screen readers announce XP progression.

### 8.4 Stat tile (3-column grid)

```tsx
<div className="grid grid-cols-3 gap-2">
  <div className="text-center rounded-lg bg-muted/50 py-2">
    <div className="flex items-center justify-center gap-1 text-primary">
      <Trophy className="w-3 h-3" />
      <span className="font-mono text-xs font-semibold">{count}</span>
    </div>
    <p className="font-mono text-[10px] text-muted-foreground mt-0.5">Solved</p>
  </div>
  {/* repeat with text-accent / text-secondary for variety */}
</div>
```

🔧 **fixed:** raised label from `text-[8px]` to `text-[10px]`. Eight-pixel text is unreadable on most displays and fails a11y audits.

### 8.5 Card / panel

```tsx
<div className="rounded-xl border bg-card p-4">
  {/* content */}
</div>
```

For an interactive card add `hover:bg-muted/30 transition-colors card-shine`.

⚠️ **note:** `border` (no color) is correct here — the base layer applies `border-border` via `* { @apply border-border }`. Only write `border-border` explicitly if you've previously overridden it.

### 8.6 Top bar (sticky glass header)

```tsx
<header className="px-5 py-3 flex items-center justify-between
                   border-b bg-background/80 backdrop-blur-sm shrink-0">
```

`bg-background/80 backdrop-blur-sm` is the canonical "glass" recipe. Don't substitute `bg-card` — the translucency over `gradient-mesh` is the whole point.

### 8.7 Primary button (inline, not shadcn)

For micro-CTAs in headers/dropdowns where the full shadcn `<Button>` is overkill:

```tsx
<Link
  to="/login"
  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg
             bg-primary text-primary-foreground hover:bg-primary/90
             font-mono text-xs font-medium transition-colors
             focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
>
  <LogIn className="w-3.5 h-3.5" /> Sign in
</Link>
```

🔧 **fixed:** v1's snippet had `className` but no `to` (broken `<Link>`) and no focus ring. Both are now included. **Always preserve focus styles on inline buttons** — shadcn handles this for you, hand-rolled buttons do not.

For full-size form CTAs, use the shadcn `<Button>` from `@/components/ui/button` instead of rolling your own.

### 8.8 Destructive ghost button

```tsx
<button
  type="button"
  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg
             text-xs font-mono transition-colors
             hover:bg-destructive/10 hover:text-destructive
             focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-destructive focus-visible:ring-offset-2 focus-visible:ring-offset-background"
>
  <LogOut className="w-3.5 h-3.5" /> Sign out
</button>
```

---

## 9. Spacing, Sizing & Radii

### Radius scale (driven by `--radius: 0.75rem`)

| Class         | Computed   | Use for                              |
|---------------|------------|--------------------------------------|
| `rounded-sm`  | `0.5rem`   | Tags, very small chips               |
| `rounded-md`  | `0.625rem` | Inputs, dropdown items               |
| `rounded-lg`  | `0.75rem`  | Default — buttons, small surfaces    |
| `rounded-xl`  | `0.75rem`* | Cards, icon tiles (Tailwind default) |
| `rounded-full`| —          | Pills, progress bars, avatars        |

\* `rounded-xl` uses Tailwind's default (`0.75rem`) because we didn't override `xl` in the config — it happens to match `--radius`. If we ever change `--radius`, `xl` will *not* track it. Fine for now; flag it if they diverge.

### Padding / gap conventions

- **Pages:** `px-6 py-8`.
- **Cards:** `p-4` (occasionally `p-5` for hero cards).
- **Compact buttons:** `px-3 py-1.5` or `px-2 py-1.5`.
- **Gaps:** `gap-2` / `gap-2.5` / `gap-3` dominate; `gap-1.5` inside buttons.
- **Icon sizing:** `w-3 h-3` (inline meta) → `w-3.5 h-3.5` (dropdown items) → `w-4 h-4` (buttons) → `w-5 h-5` (section icons).

---

## 10. Iconography

Always **`lucide-react`** — never mix in Heroicons, react-icons, or inline SVGs. Pair icon color with the surrounding text token:

```tsx
<Trophy className="w-3 h-3 text-primary" />
<Star   className="w-3 h-3 text-accent" />
<Flame  className="w-3 h-3 text-secondary" />
```

Keep stroke widths default. Don't pass `color="..."` — use Tailwind text color so theming/opacity utilities apply.

---

## 11. shadcn/ui Components

Components in `src/components/ui/*` are styled exclusively with semantic tokens. When customizing:

1. **Don't hardcode colors.** Extend the component's `cva` variants instead.
2. Add new variants like `premium` using token gradients:

```tsx
// src/components/ui/button.tsx — extend the existing buttonVariants
variant: {
  // ...existing variants
  premium: "bg-gradient-to-r from-primary to-accent text-primary-foreground hover:opacity-90",
}
```

⚠️ **note (🔧 fixed from v1):** the `premium` variant is **not** currently in `buttonVariants` — v1 implied it was. Treat the snippet above as the recipe to add it the first time you need it; don't reference `<Button variant="premium">` until you've actually extended the cva.

3. Always preserve focus styles: `focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2`.

### Import helper

Use the `cn()` utility from `@/lib/utils` for conditional class merging — it handles Tailwind class de-duplication via `tailwind-merge`:

```tsx
import { cn } from "@/lib/utils";
<div className={cn("rounded-xl border bg-card p-4", isActive && "glow-primary border-primary/30")} />
```

Never concatenate class strings with template literals when conditions are involved — you'll get duplicate/conflicting classes that don't merge.

---

## 12. Light-mode / Dark-mode

The app is **dark-only**. All tokens are defined under `:root`. The Tailwind config still declares `darkMode: ["class"]` so that future light-mode work won't require a config change — when you add light mode, define overrides under `.light { ... }` and toggle the class on `<html>`. **Do not branch component code on theme** — always go through tokens.

---

## 13. Checklist Before Committing UI

- [ ] No raw color classes (`text-white`, `bg-slate-X`, `text-red-500`, hex/rgb literals).
- [ ] Numeric/meta text uses `font-mono`; titles use `font-sans` (or omitted, since base layer is sans).
- [ ] Borders use the base-layer default (`border` alone) or an intentional override (`border-primary/30`).
- [ ] Surfaces are `bg-background` / `bg-card` / `bg-muted` / `bg-muted/50` / `bg-popover` only.
- [ ] Interactive elements have `transition-colors`, a `hover:` state, and a `focus-visible:` ring.
- [ ] Icon tiles use a gradient from the approved pairs in §8.1.
- [ ] Animations are entry-only (`fade-in`, `scale-in`) or `framer-motion` with subtle `y: -8` / `opacity: 0` start.
- [ ] No text below `text-[10px]`; ARIA attributes on progress/status widgets.
- [ ] Conditional classes go through `cn()`.
- [ ] Page picks the correct layout variant from §7 — no bespoke wrappers.

---

## 14. TL;DR Cheat Sheet

```text
Page wrapper:    min-h-screen flex flex-col gradient-mesh
Containers:      max-w-5xl (content) | max-w-6xl (grid) | max-w-md (auth)
Card:            rounded-xl border bg-card p-4
Header glass:    bg-background/80 backdrop-blur-sm border-b
Title:           text-2xl font-bold tracking-tight
Meta:            font-mono text-xs text-muted-foreground
Primary CTA:     bg-primary text-primary-foreground hover:bg-primary/90
                 + focus-visible:ring-2 focus-visible:ring-ring
Icon tile:       w-10 h-10 rounded-xl bg-gradient-to-br from-accent to-primary
Progress fill:   bg-gradient-to-r from-primary to-accent  (+ ARIA)
Stat surface:    bg-muted/50 rounded-lg
Conditional:     cn("base", flag && "extra")
```

If you can recite this section from memory, you can ship UI in this codebase.
