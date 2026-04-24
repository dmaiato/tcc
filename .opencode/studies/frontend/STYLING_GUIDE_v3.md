# SQLab Styling Guide — v3

> **Senior review note (changes from v2):**
> v2 was tight on tokens but vague on the **TopBar** (the single most-reused component) and didn't make it obvious which page/component each section governed. v3 fixes both: every section now opens with an **Applies to:** line listing the exact files it controls, and §8 is a full anatomical breakdown of `<TopBar />` — every sub-element, every state, complete code. Items marked **🆕 v3** are new in this revision.

---

## How to read this guide

Every section starts with:

> **Applies to:** `path/to/file.tsx`, `path/to/other.tsx`

That tells you exactly which files in the codebase the rules govern. If you're editing a file not listed in any "Applies to" block, you're either (a) building something new — pick the closest section as your template, or (b) editing infrastructure (config, tokens) — see §2–§6.

---

## 1. Design Philosophy

> **Applies to:** *every* file under `src/` — this is the contract.

A **gamified, dark-mode SQL training console**:

- **Cyber/terminal feel** — deep slate-blue background, neon-teal primary, monospace numerics.
- **Editorial gamification** — gradient avatars, soft glows, level badges, animated XP bars.
- **Density with breathing room** — small font sizes (`text-xs`, occasional `text-[10px]`), tight padding, but generous `rounded-xl` corners and subtle borders.

**Rule #1 — token discipline.** Never write raw color classes (`text-white`, `bg-black`, `bg-slate-900`, `text-red-500`) or hex/rgb literals in components. Always go through semantic tokens (`bg-background`, `text-foreground`, `text-primary`, `border-destructive/30`, …). The whole system depends on this — theming, opacity composition, and future light mode all break the moment a component hardcodes a color.

⚠️ **note:** `text-[9px]` looks cool but fails WCAG. Reserve sub-11px sizes for non-essential meta (badges, decorative counters), never for interactive labels or anything a screen reader user needs to read aloud.

---

## 2. Color System (HSL Tokens)

> **Applies to:** `src/index.css` (definitions), `tailwind.config.ts` (mappings). Consumed everywhere.

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

### 2.2 Semantic intent

> **Applies to:** every component that renders color.

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

> **Applies to:** `tailwind.config.ts`.

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

> **Applies to:** `src/index.css` (font import + body default), `tailwind.config.ts` (family mapping). Consumed by every text-rendering component.

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

> **Applies to:** `src/index.css` (`@layer base`). Implicit on every element.

```css
@layer base {
  * { @apply border-border; }                  /* default border color */
  body { @apply bg-background text-foreground font-sans antialiased; }
  ::selection { background: hsl(var(--primary) / 0.3); }
}
```

The `* { @apply border-border }` reset is what makes `<div className="border" />` automatically pick up the brand border color — you almost never need to write `border-border` explicitly.

---

## 5. Custom Utilities (Glows & Mesh)

> **Applies to:** `src/index.css` (`@layer utilities`). Consumed by page wrappers, result panes, and interactive cards.

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
- `.gradient-mesh` — full-page wrappers (`Index`, `Login`, `Register`, `Profile`, `Mission`, `Admin`, `Leaderboard`) for the ambient teal/violet/gold glow.
- `.glow-primary` — pinned/active cards, primary CTAs, active tabs.
- `.glow-success` / `.glow-error` — `ResultsPane`, test outcomes.
- `.card-shine` — interactive list cards (mission browser on `Index`).

---

## 6. Animation Tokens

> **Applies to:** `tailwind.config.ts` (keyframes + animation map). Consumed by any component using `animate-fade-in`, `animate-scale-in`, etc., and by `framer-motion` consumers.

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

## 7. Layout Variants

> **Applies to:** the outermost JSX of each page in `src/pages/*`.

The codebase uses **four** distinct page shells. Pick one — don't invent a fifth.

### 7.A — Centered content page

> **Applies to:** `src/pages/ProfilePage.tsx`, `src/pages/LeaderboardPage.tsx`, `src/pages/AdminPage.tsx`.

```tsx
<div className="min-h-screen flex flex-col gradient-mesh">
  <TopBar />
  <main className="flex-1 px-6 py-8">
    <div className="max-w-5xl mx-auto">{/* content */}</div>
  </main>
</div>
```

### 7.B — Wider browser/landing

> **Applies to:** `src/pages/Index.tsx`.

```tsx
<div className="min-h-screen flex flex-col gradient-mesh">
  <TopBar />
  <main className="flex-1 px-6 py-8">
    <div className="max-w-6xl mx-auto">{/* mission browser */}</div>
  </main>
</div>
```

### 7.C — Full-bleed workbench

> **Applies to:** `src/pages/MissionPage.tsx`.

```tsx
<div className="h-screen flex flex-col overflow-hidden gradient-mesh">
  <TopBar />
  {/* resizable panels, no max-width, no scroll on root */}
</div>
```

### 7.D — Centered card

> **Applies to:** `src/pages/LoginPage.tsx`, `src/pages/RegisterPage.tsx`.

```tsx
<div className="min-h-screen flex items-center justify-center gradient-mesh px-4">
  <div className="w-full max-w-md">{/* form card */}</div>
</div>
```

**Rule:** if a page has a max width, it's `max-w-5xl` for content, `max-w-6xl` for grids, `max-w-md` for auth forms. Don't invent new widths.

---

## 8. The TopBar — Full Anatomy 🆕 v3

> **Applies to:** `src/components/TopBar.tsx` (the only file). Rendered by every layout in §7 except `LoginPage` / `RegisterPage` (auth shells use 7.D and intentionally omit it).

The `<TopBar />` is the single most-reused component in the app and the visual anchor of every authenticated page. This section documents **every sub-element** in render order.

### 8.0 At a glance

```
┌────────────────────────────────────────────────────────────────────────┐
│  [LogoTile] SQLab          ← Missions          [SignIn] | [UserChip ▾] │
│   (left)                    (center, optional)            (right)      │
└────────────────────────────────────────────────────────────────────────┘
```

Three slots, separated by `flex items-center justify-between`:

| Slot   | Content                                | Always present? |
|--------|----------------------------------------|-----------------|
| Left   | Logo tile + wordmark (`<Link to="/">`) | ✅ yes          |
| Center | "← Missions" back-link                 | Only when `pathname !== "/"` |
| Right  | Either `Sign in` button **or** `UserChip` dropdown | ✅ yes (one of the two) |

### 8.1 The shell

```tsx
<header className="px-5 py-3 flex items-center justify-between
                   border-b border-border bg-background/80 backdrop-blur-sm shrink-0">
  {/* slots */}
</header>
```

**Rules — do not deviate:**

| Property         | Value                          | Why                                           |
|------------------|--------------------------------|-----------------------------------------------|
| Padding          | `px-5 py-3`                    | Tighter than page padding (`px-6 py-8`) so the bar feels structural, not content. |
| Element          | `<header>`                     | Semantic landmark for screen readers.         |
| Background       | `bg-background/80 backdrop-blur-sm` | The "glass" recipe. The translucency over `gradient-mesh` is the whole point — never substitute `bg-card`. |
| Border           | `border-b border-border`       | Single hairline at bottom only.               |
| Sizing           | `shrink-0`                     | Critical for layout 7.C (workbench) — without it the resizable panels eat the bar. |
| Position         | Static (no `sticky`)           | All layouts use `flex-col` page shells; the bar is the first child and stays at top naturally. |

### 8.2 Slot 1 — Logo (LogoTile + wordmark)

```tsx
<Link to="/" className="flex items-center gap-2.5 group">
  <motion.div
    initial={{ opacity: 0, scale: 0.9 }}
    animate={{ opacity: 1, scale: 1 }}
    className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-secondary
               flex items-center justify-center"
  >
    <Database className="w-4 h-4 text-primary-foreground" />
  </motion.div>
  <h1 className="font-sans text-lg font-bold tracking-tight
                 group-hover:text-primary transition-colors">
    SQ<span className="text-primary">Lab</span>
  </h1>
</Link>
```

| Sub-part      | Spec                                                                          |
|---------------|-------------------------------------------------------------------------------|
| Tile size     | `w-8 h-8` (32 px) — this matches the user avatar exactly, intentionally.      |
| Tile radius   | `rounded-lg` (not `xl`) — small enough that it reads as an icon, not a card.  |
| Tile gradient | `from-primary to-secondary` — the **only** gradient permitted on the logo.    |
| Tile icon     | `lucide-react/Database`, `w-4 h-4`, `text-primary-foreground`.                |
| Wordmark      | `font-sans text-lg font-bold tracking-tight`. The "Lab" half is `text-primary` — **never** color the "SQ" half. |
| Hover         | `group-hover:text-primary` on the wordmark only — the tile does not animate on hover. |
| Entrance      | `framer-motion` scale+fade, ~250 ms (default `motion` transition). Don't add per-page entrance — the bar's own animation is enough. |

### 8.3 Slot 2 — Back-link (conditional)

Rendered only on non-home routes. Drives users back to mission selection.

```tsx
{!isHome && (
  <Link to="/" className="font-mono text-xs text-muted-foreground hover:text-primary transition-colors">
    ← Missions
  </Link>
)}
```

| Spec                  | Value                                          |
|-----------------------|------------------------------------------------|
| Determined by         | `useLocation().pathname === "/"`               |
| Font                  | `font-mono text-xs` (it's meta, not a CTA).    |
| Color                 | `text-muted-foreground` → `hover:text-primary`.|
| Glyph                 | Literal `←` character (NOT a `lucide` chevron — keeps the terminal aesthetic). |

### 8.4 Slot 3a — Logged-out state: `Sign in` button

```tsx
<Link
  to="/login"
  className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg
             bg-primary text-primary-foreground hover:bg-primary/90
             font-mono text-xs font-medium transition-colors
             focus-visible:outline-none focus-visible:ring-2
             focus-visible:ring-ring focus-visible:ring-offset-2
             focus-visible:ring-offset-background"
>
  <LogIn className="w-3.5 h-3.5" /> Sign in
</Link>
```

This is the canonical inline primary button (see also §10.7). **Always include focus-visible styles** — handcrafted buttons don't get them from shadcn.

### 8.5 Slot 3b — Logged-in state: `UserChip` trigger

The clickable button that opens the dropdown.

```tsx
<DropdownMenuTrigger asChild>
  <button className="flex items-center gap-2.5 px-2 py-1.5 rounded-lg
                     hover:bg-muted/50 transition-colors outline-none">
    {/* avatar with badge — see 8.6 */}
    <div className="hidden sm:block text-left">
      <p className="font-sans text-xs font-semibold leading-tight">{username}</p>
      <p className="font-mono text-[9px] text-muted-foreground">Lvl {level}</p>
    </div>
    <ChevronDown className="w-3 h-3 text-muted-foreground" />
  </button>
</DropdownMenuTrigger>
```

| Sub-part          | Spec                                                                |
|-------------------|---------------------------------------------------------------------|
| Layout            | `flex items-center gap-2.5 px-2 py-1.5 rounded-lg`                  |
| Hover             | `hover:bg-muted/50` — subtle, no color shift.                       |
| Username/lvl text | Hidden below `sm` (`hidden sm:block`) — avatar alone on mobile.     |
| Username font     | `font-sans text-xs font-semibold leading-tight`                     |
| Level meta        | `font-mono text-[9px] text-muted-foreground` (decorative meta — exempt from §1's 11px rule). |
| Chevron           | `lucide/ChevronDown w-3 h-3 text-muted-foreground` — NEVER rotate it. |

### 8.6 Avatar with level badge (used inside §8.5)

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

| Sub-part      | Spec                                                                |
|---------------|---------------------------------------------------------------------|
| Avatar size   | `w-8 h-8` in TopBar; `w-12 h-12 rounded-xl` inside the dropdown header (8.7.A). Never `rounded-full` — that breaks the cyber aesthetic. |
| Avatar color  | One of the gradients in the `AVATAR_COLORS` map at the top of `TopBar.tsx`. **Add new seeds there only**, never inline. |
| Initial       | `username.charAt(0).toUpperCase()`                                  |
| Badge         | `w-4 h-4 rounded-full bg-accent`. The `border-2 border-background` is what punches it visually out of the avatar — don't drop it. |
| Badge digit   | `text-[8px]` is OK — purely decorative; the level is also announced in 8.5's text. |

### 8.7 Dropdown content

Opened from §8.5. Rendered as a single `<DropdownMenuContent>` with three regions stacked vertically.

```tsx
<DropdownMenuContent align="end" className="w-64 p-0 bg-card border-border">
  {/* A — profile summary */}
  {/* <DropdownMenuSeparator className="bg-border" /> */}
  {/* B — link list */}
</DropdownMenuContent>
```

| Spec        | Value                                                              |
|-------------|--------------------------------------------------------------------|
| Width       | `w-64` (256 px) — fixed, never `w-auto`.                           |
| Padding     | `p-0` on the outer; each region pads itself.                       |
| Background  | `bg-card border-border` — explicit token override of the default popover surface, because the dropdown sits over `gradient-mesh` and needs more contrast than `popover`. |
| Alignment   | `align="end"` — flush with the trigger's right edge.               |

#### 8.7.A — Region A: profile summary block

```tsx
<div className="p-4">
  {/* big avatar + name + xp meta */}
  <div className="flex items-center gap-3 mb-3">
    <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${avatarColor}
                     flex items-center justify-center text-lg font-bold
                     text-primary-foreground`}>
      {initial}
    </div>
    <div>
      <p className="font-sans text-sm font-semibold">{username}</p>
      <p className="font-mono text-[10px] text-muted-foreground">
        Level {level} · {totalXp} XP
      </p>
    </div>
  </div>

  {/* xp bar — see 8.8 */}
  {/* stat tiles — see 8.9 */}
</div>
```

The avatar grows from `w-8 h-8 rounded-lg` (header) to `w-12 h-12 rounded-xl` (dropdown). Same gradient, same initial — identity continuity matters.

#### 8.7.B — Region B: link list

```tsx
<div className="p-2 space-y-0.5">
  <Link to="/profile"     className={linkRow}><User    className="w-3.5 h-3.5 text-primary" /> View Full Profile</Link>
  <Link to="/leaderboard" className={linkRow}><Trophy  className="w-3.5 h-3.5 text-accent" />  Leaderboard</Link>
  <Link to="/admin"       className={linkRow}><Shield  className="w-3.5 h-3.5 text-accent" />  Mission Control</Link>
  <button onClick={logout} className={destructiveRow}>
    <LogOut className="w-3.5 h-3.5 text-destructive" /> Sign out
  </button>
</div>

// where:
const linkRow        = "flex items-center gap-2 w-full px-3 py-2.5 rounded-lg font-mono text-xs text-foreground hover:bg-muted/50 transition-colors";
const destructiveRow = "flex items-center gap-2 w-full px-3 py-2.5 rounded-lg font-mono text-xs text-foreground hover:bg-destructive/10 hover:text-destructive transition-colors";
```

**Rules for adding a new link:**
1. Use `<Link>` from `react-router-dom` for in-app routes; `<a>` only for external.
2. Icon = `lucide-react`, `w-3.5 h-3.5`, colored to match the link's *intent* (`text-primary` for neutral, `text-accent` for highlighted, `text-destructive` for danger).
3. Reuse `linkRow` / `destructiveRow` class strings — don't fork them.
4. Order: navigation links first (top to bottom by importance), destructive actions last.

### 8.8 XP / progress bar (used in dropdown Region A)

```tsx
<div
  className="flex-1 h-1.5 rounded-full bg-muted overflow-hidden"
  role="progressbar"
  aria-valuenow={xpProgress}
  aria-valuemin={0}
  aria-valuemax={xpForNextLevel}
>
  <motion.div
    className="h-full rounded-full bg-gradient-to-r from-primary to-accent"
    initial={{ width: 0 }}
    animate={{ width: `${(xpProgress / xpForNextLevel) * 100}%` }}
    transition={{ duration: 0.6 }}
  />
</div>
```

**Always include the ARIA attributes** — XP progression is meaningful state, not decoration.

### 8.9 Stat tile (3-column grid, used in dropdown Region A)

```tsx
<div className="grid grid-cols-3 gap-2">
  <div className="text-center rounded-lg bg-muted/50 py-2">
    <div className="flex items-center justify-center gap-1 text-primary">
      <Trophy className="w-3 h-3" />
      <span className="font-mono text-xs font-semibold">{solved}</span>
    </div>
    <p className="font-mono text-[10px] text-muted-foreground mt-0.5">Solved</p>
  </div>
  {/* repeat with text-accent (XP) and text-secondary (remaining) */}
</div>
```

Color rotation is fixed: **primary → accent → secondary**. Don't reorder them across pages — visual rhythm relies on it.

### 8.10 Data dependencies

The TopBar pulls from two Zustand stores:

```tsx
const { profile, totalXp, level, xpProgress, xpForNextLevel, completedMissions } = useGameStore();
const { user, logout } = useAuthStore();
```

If you add a new stat to the dropdown, **derive it inside the component** from these stores — don't add new props to `<TopBar />`. The bar takes no props by design; that's what keeps it droppable into every layout.

---

## 9. Reusable Building-Block Patterns

> **Applies to:** any new page or component. These are the patterns the TopBar itself is composed of, hoisted out for use elsewhere.

### 9.1 Section header with gradient icon tile

> **Applies to:** top of any page body — `LeaderboardPage`, `ProfilePage`, `AdminPage`, `MissionPage` (header strip).

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
| `from-primary to-secondary`       | Default brand decoration (logo)  |
| `from-accent to-primary`          | Leaderboard, achievements        |
| `from-secondary to-primary`       | Profile, identity                |
| `from-destructive to-accent`      | Warnings, hero/danger CTAs       |
| `from-primary to-accent`          | Progress fills (XP bars)         |

### 9.2 Card / panel

> **Applies to:** `ProfileCard`, `MissionFilters`, mission rows on `Index`, any future panel.

```tsx
<div className="rounded-xl border bg-card p-4">
  {/* content */}
</div>
```

For an interactive card add `hover:bg-muted/30 transition-colors card-shine`.

⚠️ **note:** `border` (no color) is correct here — the base layer applies `border-border` via `* { @apply border-border }`. Only write `border-border` explicitly if you've previously overridden it.

### 9.3 Inline primary button (non-shadcn)

> **Applies to:** `TopBar` Sign-in CTA, micro-CTAs in headers/dropdowns. For full-size form CTAs, use shadcn `<Button>` from `@/components/ui/button` instead.

```tsx
<Link
  to="/login"
  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg
             bg-primary text-primary-foreground hover:bg-primary/90
             font-mono text-xs font-medium transition-colors
             focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring
             focus-visible:ring-offset-2 focus-visible:ring-offset-background"
>
  <LogIn className="w-3.5 h-3.5" /> Sign in
</Link>
```

### 9.4 Destructive ghost button

> **Applies to:** Sign-out row in TopBar dropdown, delete actions in `AdminPage`.

```tsx
<button
  type="button"
  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg
             text-xs font-mono transition-colors
             hover:bg-destructive/10 hover:text-destructive
             focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-destructive
             focus-visible:ring-offset-2 focus-visible:ring-offset-background"
>
  <LogOut className="w-3.5 h-3.5" /> Sign out
</button>
```

---

## 10. Spacing, Sizing & Radii

> **Applies to:** every component. The constants below are the only ones in regular use.

### Radius scale (driven by `--radius: 0.75rem`)

| Class         | Computed   | Use for                              |
|---------------|------------|--------------------------------------|
| `rounded-sm`  | `0.5rem`   | Tags, very small chips               |
| `rounded-md`  | `0.625rem` | Inputs, dropdown items               |
| `rounded-lg`  | `0.75rem`  | Default — buttons, small surfaces, **logo tile**, **avatar tile (header size)** |
| `rounded-xl`  | `0.75rem`* | Cards, **icon tiles in section headers**, **avatar in dropdown** |
| `rounded-full`| —          | Pills, progress bars, level badge    |

\* `rounded-xl` uses Tailwind's default (`0.75rem`) because we didn't override `xl` in the config — it happens to match `--radius`. If we ever change `--radius`, `xl` will *not* track it. Fine for now; flag it if they diverge.

### Padding / gap conventions

- **Pages:** `px-6 py-8`.
- **TopBar:** `px-5 py-3` (intentionally tighter than pages).
- **Cards:** `p-4` (occasionally `p-5` for hero cards).
- **Compact buttons:** `px-3 py-1.5` or `px-2 py-1.5`.
- **Gaps:** `gap-2` / `gap-2.5` / `gap-3` dominate; `gap-1.5` inside buttons.
- **Icon sizing:** `w-3 h-3` (inline meta) → `w-3.5 h-3.5` (dropdown items) → `w-4 h-4` (buttons, logo) → `w-5 h-5` (section icons).

---

## 11. Iconography

> **Applies to:** every component that renders an icon.

Always **`lucide-react`** — never mix in Heroicons, react-icons, or inline SVGs. Pair icon color with the surrounding text token:

```tsx
<Trophy className="w-3 h-3 text-primary" />
<Star   className="w-3 h-3 text-accent" />
<Flame  className="w-3 h-3 text-secondary" />
```

Keep stroke widths default. Don't pass `color="..."` — use Tailwind text color so theming/opacity utilities apply.

---

## 12. shadcn/ui Components

> **Applies to:** everything under `src/components/ui/*` and any consumer.

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

⚠️ The `premium` variant is **not** currently in `buttonVariants`. Treat the snippet above as the recipe to add it the first time you need it; don't reference `<Button variant="premium">` until you've actually extended the cva.

3. Always preserve focus styles: `focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2`.

### Import helper

> **Applies to:** any component composing conditional class names.

Use the `cn()` utility from `@/lib/utils` for conditional class merging — it handles Tailwind class de-duplication via `tailwind-merge`:

```tsx
import { cn } from "@/lib/utils";
<div className={cn("rounded-xl border bg-card p-4", isActive && "glow-primary border-primary/30")} />
```

Never concatenate class strings with template literals when conditions are involved — you'll get duplicate/conflicting classes that don't merge.

---

## 13. Light-mode / Dark-mode

> **Applies to:** `src/index.css` (`:root`) and `tailwind.config.ts` (`darkMode`).

The app is **dark-only**. All tokens are defined under `:root`. The Tailwind config still declares `darkMode: ["class"]` so that future light-mode work won't require a config change — when you add light mode, define overrides under `.light { ... }` and toggle the class on `<html>`. **Do not branch component code on theme** — always go through tokens.

---

## 14. Checklist Before Committing UI

> **Applies to:** code review of any PR.

- [ ] No raw color classes (`text-white`, `bg-slate-X`, `text-red-500`, hex/rgb literals).
- [ ] Numeric/meta text uses `font-mono`; titles use `font-sans` (or omitted, since base layer is sans).
- [ ] Borders use the base-layer default (`border` alone) or an intentional override (`border-primary/30`).
- [ ] Surfaces are `bg-background` / `bg-card` / `bg-muted` / `bg-muted/50` / `bg-popover` only.
- [ ] Interactive elements have `transition-colors`, a `hover:` state, and a `focus-visible:` ring.
- [ ] Icon tiles use a gradient from the approved pairs in §9.1.
- [ ] Animations are entry-only (`fade-in`, `scale-in`) or `framer-motion` with subtle `y: -8` / `opacity: 0` start.
- [ ] No text below `text-[10px]`; ARIA attributes on progress/status widgets.
- [ ] Conditional classes go through `cn()`.
- [ ] Page picks the correct layout variant from §7 — no bespoke wrappers.
- [ ] If editing `TopBar.tsx`: new dropdown links reuse the `linkRow`/`destructiveRow` patterns from §8.7.B; no new props added.

---

## 15. TL;DR Cheat Sheet

```text
Page wrapper:    min-h-screen flex flex-col gradient-mesh
Containers:      max-w-5xl (content) | max-w-6xl (grid) | max-w-md (auth)
Card:            rounded-xl border bg-card p-4
TopBar shell:    px-5 py-3 border-b bg-background/80 backdrop-blur-sm shrink-0
TopBar logo:     w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-secondary
TopBar avatar:   w-8 h-8 rounded-lg + w-4 h-4 rounded-full bg-accent badge
Dropdown:        w-64 p-0 bg-card border-border, align="end"
Title:           text-2xl font-bold tracking-tight
Meta:            font-mono text-xs text-muted-foreground
Primary CTA:     bg-primary text-primary-foreground hover:bg-primary/90
                 + focus-visible:ring-2 focus-visible:ring-ring
Icon tile:       w-10 h-10 rounded-xl bg-gradient-to-br from-accent to-primary
Progress fill:   bg-gradient-to-r from-primary to-accent  (+ ARIA)
Stat surface:    bg-muted/50 rounded-lg, color rotation primary→accent→secondary
Conditional:     cn("base", flag && "extra")
```

If you can recite this section from memory, you can ship UI in this codebase.
