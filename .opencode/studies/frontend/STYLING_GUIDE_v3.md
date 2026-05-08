# SQLab — Styling Guide (v3)

> Authoritative reference for building and refactoring UI in this project.
> Every section is scoped with **Applies to:** so you know exactly which file the rules govern.

---

## 0. Foundations (global)

> Applies to: `src/index.css`, `tailwind.config.ts`, every component.

### 0.1 Color tokens (HSL only)

Defined in `src/index.css` under `:root`. Every color in every component MUST go through these tokens — no raw `#hex`, no `text-white`, no `bg-black`.

| Token              | Role                                  | HSL              |
| ------------------ | ------------------------------------- | ---------------- |
| `--background`     | App background                        | `230 25% 9%`     |
| `--foreground`     | Default text                          | `220 20% 92%`    |
| `--card`           | Elevated surfaces                     | `230 22% 12%`    |
| `--popover`        | Popovers / dropdowns                  | `230 22% 12%`    |
| `--primary`        | Brand teal (CTA, success, "correct")  | `165 80% 48%`    |
| `--secondary`      | Indigo (schema / data accents)        | `250 60% 62%`    |
| `--muted`          | Subdued surface / chip backgrounds    | `230 18% 16%`    |
| `--muted-foreground` | Subdued text / metadata             | `220 15% 55%`    |
| `--accent`         | Amber (XP, warnings, "modified")      | `35 95% 60%`     |
| `--destructive`    | Red (errors, "incorrect", sign out)   | `0 72% 55%`      |
| `--border`         | Standard borders                      | `230 15% 20%`    |
| `--ring`           | Focus ring (matches primary)          | `165 80% 48%`    |
| `--editor-bg`      | SQL editor background (darker)        | `230 25% 7%`     |
| `--surface-raised` | Slight elevation above `--card`       | `230 20% 14%`    |
| `--success`        | Alias of primary, for semantics       | `165 80% 48%`    |
| `--radius`         | Base radius                           | `0.75rem`        |

Tailwind class names: `bg-background`, `text-foreground`, `bg-card`, `bg-primary text-primary-foreground`, `bg-editor`, `bg-surface-raised`, `text-success`, etc.

### 0.2 Typography

| Family               | Class       | Use                                                |
| -------------------- | ----------- | -------------------------------------------------- |
| Space Grotesk 400–700 | `font-sans` | UI copy, titles, body text                         |
| JetBrains Mono 400/500 | `font-mono` | Numbers, labels, badges, code, all metadata        |

Never introduce a third family. Default body uses `font-sans` via `body { @apply font-sans }` in `src/index.css`.

Common micro-sizes (intentional, do not "round up"):
- `text-[10px]` — uppercase tracking labels, dot legends
- `text-[9px]` / `text-[8px]` — sublabels inside dense components (header chip, stat tile labels)
- `text-xs` — buttons, chips, links
- `text-sm` — body, editor text, action buttons
- `text-lg` — page subtitles
- `text-xl` / `text-2xl` — page titles

### 0.3 Utilities (custom, in `src/index.css`)

- `.gradient-mesh` — subtle 3-color radial mesh background (use on full-screen pages like MissionPage).
- `.glow-primary`, `.glow-success`, `.glow-error`, `.glow-accent` — colored box-shadows used on Verify button, error/success cards.
- `.card-shine` — hover sheen for elevated cards.

### 0.4 Class-merging utility

> Applies to: any component combining conditional classes.

Use `cn()` from `@/lib/utils` for conditional merging — never string-concatenate Tailwind classes by hand:

```tsx
import { cn } from "@/lib/utils";
<button className={cn("font-mono text-xs", isActive && "text-primary")} />
```

---

## 1. Layout variants

> Applies to: every page in `src/pages/*`.

Pick one — do not invent ad-hoc widths.

| Variant            | Container                                         | Used by                                |
| ------------------ | ------------------------------------------------- | -------------------------------------- |
| Centered content   | `max-w-5xl mx-auto px-5 py-8`                     | `Index.tsx`, `ProfilePage.tsx`         |
| Wider browser      | `max-w-6xl mx-auto px-5 py-8`                     | `LeaderboardPage.tsx`, `AdminPage.tsx` |
| Full-bleed workbench | `h-screen flex flex-col` + `gradient-mesh`     | `MissionPage.tsx`                      |
| Centered auth card | `min-h-screen flex items-center justify-center`   | `LoginPage.tsx`, `RegisterPage.tsx`    |

---

## 2. TopBar (`src/components/TopBar.tsx`)

> Applies to: `src/components/TopBar.tsx` only. Imported by every page.

### 2.1 Shell

```tsx
<header className="px-5 py-3 flex items-center justify-between
                   border-b border-border bg-background/80 backdrop-blur-sm shrink-0">
```

- **Three-slot flex row**: Logo (left) · contextual nav (center) · auth slot (right).
- **Glass effect**: `bg-background/80 backdrop-blur-sm` — never opaque.
- **`shrink-0`** is mandatory so it doesn't collapse inside `h-screen flex flex-col` pages.

### 2.2 Logo

Gradient tile (`from-primary to-secondary`) → `Database` icon → wordmark `SQ<span class="text-primary">Lab</span>`. Hover transitions title to `text-primary` via the parent `group` selector.

### 2.3 Contextual back-link

Rendered only when `useLocation().pathname !== "/"`:

```tsx
<Link to="/" className="font-mono text-xs text-muted-foreground hover:text-primary transition-colors">
  ← Missions
</Link>
```

### 2.4 Auth slot — unauthenticated

Single primary CTA, never an icon-only button:

```tsx
<Link to="/login" className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg
       bg-primary text-primary-foreground hover:bg-primary/90
       font-mono text-xs font-medium transition-colors">
  <LogIn className="w-3.5 h-3.5" /> Sign in
</Link>
```

### 2.5 Auth slot — authenticated (UserChip + dropdown)

**Trigger** — `<DropdownMenuTrigger asChild>` wrapping a `<button>`:
- `w-8 h-8` avatar tile, gradient resolved from `AVATAR_COLORS[profile.avatarSeed]`.
- Level badge: `absolute -bottom-0.5 -right-0.5 w-4 h-4 rounded-full bg-accent` with `border-2 border-background` and `text-[8px]` numeric.
- Username + `Lvl N` stack: hidden on mobile (`hidden sm:block`).
- `ChevronDown w-3 h-3 text-muted-foreground` affordance.

**Content** — `<DropdownMenuContent align="end" className="w-64 p-0 bg-card border-border">`. Two regions:

1. **Profile summary** (`p-4`):
   - Large `w-12 h-12 rounded-xl` avatar + `Level N · {totalXp} XP` line.
   - **XP bar**: `h-1.5 rounded-full bg-muted overflow-hidden` track with a `motion.div` fill `bg-gradient-to-r from-primary to-accent`, animated `width: ${(xpProgress/xpForNextLevel)*100}%`, duration `0.6`.
   - **Stat tiles** (`grid grid-cols-3 gap-2`): each `rounded-lg bg-muted/50 py-2 text-center`. Color rotation: Solved → `text-primary` (Trophy), XP → `text-accent` (Star), Left → `text-secondary` (Flame). Sublabel `font-mono text-[8px] text-muted-foreground`.

2. **Links** (`p-2 space-y-0.5`), separated by `<DropdownMenuSeparator className="bg-border" />`:
   - Each link/button: `flex items-center gap-2 w-full px-3 py-2.5 rounded-lg font-mono text-xs hover:bg-muted/50`.
   - Icon color encodes destination: `text-primary` (Profile), `text-accent` (Leaderboard, Admin), `text-destructive` (Sign out — also `hover:bg-destructive/10 hover:text-destructive`).

---

## 3. Mission Page — Build Guide (`src/pages/MissionPage.tsx`)

> **Scope.** This section is the canonical reference for the workbench screen and its three direct children:
> `src/components/SqlEditor.tsx`, `src/components/ResultsPane.tsx`, `src/components/DataViewer.tsx`.
> Read it top-to-bottom before editing any of those files. Every Tailwind class below is load-bearing — do not "clean up" classes you do not understand.

---

### 3.0 Mental model

The Mission page is a **non-scrolling, full-viewport workbench**. The browser window itself never scrolls; only the inner Mission/Schema content pane and the Results pane scroll. The page is built as **four vertically-stacked regions** inside a single `h-screen` flex column:

```
┌──────────────────────────────────────────────────────┐
│  TopBar                                  shrink-0    │  ← global header
├──────────────────────────────────────────────────────┤
│  Mission navigator strip                shrink-0    │  ← prev / counter / next
├──────────────────────────────────────────────────────┤
│  Main split                       flex-1 min-h-0    │
│  ┌────────────── 38% ─────────────┬───── 62% ─────┐ │
│  │ Tabs: Mission | Schema         │ SqlEditor     │ │
│  │ TabsContent (scrolls)          │ Action bar    │ │
│  │                                │ ResultsPane   │ │
│  └────────────────────────────────┴───────────────┘ │
└──────────────────────────────────────────────────────┘
```

The two iron rules that make this layout work:

1. **Every direct flex child** is either `shrink-0` (header strips) or `flex-1 min-h-0` (the body row).
2. **Every column** that wraps a scrollable child uses `min-h-0` on itself AND `overflow-auto` on the scroller. Without `min-h-0`, flex children inherit `min-height: auto` and refuse to shrink — your scroll area pushes the layout instead of overflowing.

If you forget `min-h-0` once, the workbench will silently grow past the viewport and the page will scroll. Search the file for `min-h-0` before opening a PR.

---

### 3.1 Page shell — exact HTML

```tsx
// src/pages/MissionPage.tsx
return (
  <div className="h-screen flex flex-col overflow-hidden gradient-mesh">
    <TopBar />

    {/* §3.2 navigator strip */}
    <div className="flex items-center justify-center gap-1 px-5 py-1.5 border-b border-border bg-background/50 shrink-0">
      …
    </div>

    {/* §3.3 main 38/62 split */}
    <div className="flex flex-1 min-h-0">
      <div className="w-[38%] border-r border-border flex flex-col min-h-0 bg-background/50">…</div>
      <div className="w-[62%] flex flex-col min-h-0 p-4 gap-3">…</div>
    </div>
  </div>
);
```

Class-by-class:

| Class | Why |
| --- | --- |
| `h-screen` | Pin the page to viewport height. Required for the no-scroll contract. |
| `flex flex-col` | Stack header / strip / main vertically. |
| `overflow-hidden` | Belt-and-braces: even if a child miscalculates, the page itself cannot scroll. |
| `gradient-mesh` | Custom utility from `index.css` — soft 3-color radial mesh that shows through `bg-background/50` panels. |

Do not add padding or margin to this wrapper. All spacing belongs to the inner regions.

---

### 3.2 Mission navigator strip

A thin, **centered** horizontal strip immediately under `<TopBar />`. It exists so users can move between adjacent missions without going back to the catalog.

```tsx
{prevMission && (
  <Link
    to={`/mission/${prevMission.id}`}
    className="font-mono text-xs text-muted-foreground hover:text-foreground transition-colors px-2 py-1 rounded hover:bg-muted"
  >
    ← Prev
  </Link>
)}
<span className="font-mono text-[10px] text-muted-foreground px-3">
  {idx + 1} / {missions.length}
</span>
{nextMission && (
  <Link
    to={`/mission/${nextMission.id}`}
    className="font-mono text-xs text-muted-foreground hover:text-foreground transition-colors px-2 py-1 rounded hover:bg-muted"
  >
    Next →
  </Link>
)}
```

Rules:

- Wrapper uses `justify-center` (visual balance under the centered TopBar). Never `justify-between`.
- Prev/Next render **conditionally** on `idx`. Do not render disabled placeholder links.
- Counter is intentionally **smaller** than the links (`text-[10px]` vs `text-xs`) so it reads as metadata, not a control.
- Wrapper is `shrink-0` — non-negotiable, otherwise it collapses when the editor grows.

---

### 3.3 Main split — 38 / 62

```tsx
<div className="flex flex-1 min-h-0">
  <div className="w-[38%] border-r border-border flex flex-col min-h-0 bg-background/50">
    {/* §3.4 tabbed info panel */}
  </div>
  <div className="w-[62%] flex flex-col min-h-0 p-4 gap-3">
    {/* §3.5 workbench */}
  </div>
</div>
```

- The split is **fixed-percentage**, not `flex-1`. The right column needs a predictable, wider area for the editor; `flex-1` would let content on the left fight for space.
- Both columns are `flex flex-col min-h-0` because each contains an internal scroll region (TabsContent on the left, ResultsPane on the right).
- The left column is `bg-background/50` so the gradient-mesh shows through. The right column is **transparent** — its `bg-editor` and `bg-card` children supply contrast.
- A single right `border-r border-border` separates the two columns. No gap, no shadow.

---

### 3.4 Left column — tabbed info panel

#### 3.4.1 Tabs scaffolding

Uses shadcn `<Tabs>`. The trick is that the **`Tabs` root itself must be a flex column** so its content can fill the remaining height and scroll independently.

```tsx
<Tabs defaultValue="mission" className="flex flex-col flex-1 min-h-0">
  <div className="px-4 pt-3 shrink-0">
    <TabsList className="w-full bg-muted/50 border border-border">
      <TabsTrigger
        value="mission"
        className="flex-1 gap-1.5 font-mono text-xs data-[state=active]:bg-primary/10 data-[state=active]:text-primary"
      >
        <ScrollText className="w-3.5 h-3.5" />
        Mission
      </TabsTrigger>
      <TabsTrigger
        value="schema"
        className="flex-1 gap-1.5 font-mono text-xs data-[state=active]:bg-secondary/10 data-[state=active]:text-secondary"
      >
        <Database className="w-3.5 h-3.5" />
        Schema
      </TabsTrigger>
    </TabsList>
  </div>

  <TabsContent value="mission" className="flex-1 overflow-auto px-5 pb-5 mt-0"> … </TabsContent>
  <TabsContent value="schema"  className="flex-1 overflow-auto px-5 pb-5 mt-0"> … </TabsContent>
</Tabs>
```

Required behaviors:

- **Color pairing is semantic, not decorative.** Mission = **primary (teal)** because it is "what to do". Schema = **secondary (indigo)** because it is "the data". Never swap.
- `TabsList` sits on `bg-muted/50` with a border so it reads as a grouped control, not a navbar.
- `TabsContent` MUST include `mt-0` — shadcn injects a top margin by default that breaks the scroll boundary.
- The wrapper around `TabsList` is `shrink-0`; the content panes own the scroll via `flex-1 overflow-auto`.

#### 3.4.2 Mission tab body — full markup

Four stacked motion blocks with **staggered delays** (`0`, `0.1`, `0.15`, `0.2`). Keep this rhythm when adding blocks; it's what makes the panel "land" instead of "appear".

```tsx
<TabsContent value="mission" className="flex-1 overflow-auto px-5 pb-5 mt-0">
  <div className="space-y-5 pt-4">

    {/* (1) Heading block */}
    <motion.div initial={{ opacity: 0, x: -10 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.3 }}>
      <div className="flex items-center gap-2 mb-2 flex-wrap">
        <span className={`font-mono text-[10px] uppercase tracking-widest px-2 py-0.5 rounded ${diff.bgColor} ${diff.color}`}>
          {diff.label}
        </span>
        <span className={`font-mono text-[10px] px-2 py-0.5 rounded ${cat.bgColor} ${cat.color}`}>
          {cat.icon} {cat.label}
        </span>
        <span className="font-mono text-[10px] text-accent">{mission.xp} XP</span>
        {completed && (
          <span className="font-mono text-[10px] text-primary ml-auto flex items-center gap-1">
            <CheckCircle2 className="w-3 h-3" /> Solved
          </span>
        )}
      </div>
      <div className="font-mono text-[10px] text-muted-foreground mb-1">
        Mission {String(idx + 1).padStart(2, "0")}
      </div>
      <h1 className="font-sans text-xl font-bold mb-1">{mission.title}</h1>
      <p className="font-sans text-sm text-muted-foreground">{mission.subtitle}</p>
    </motion.div>

    {/* (2) Objective card */}
    <motion.div
      initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.1 }}
      className="rounded-lg bg-card border border-border p-4"
    >
      <h2 className="font-mono text-[10px] text-muted-foreground uppercase tracking-wider mb-2">Objective</h2>
      <p className="font-sans text-sm leading-relaxed">{mission.description}</p>
    </motion.div>

    {/* (3) Tables Available — secondary color (data) */}
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.15 }}>
      <div className="rounded-lg bg-muted/30 border border-border/50 p-3">
        <h2 className="font-mono text-[10px] text-muted-foreground uppercase tracking-wider mb-2">Tables Available</h2>
        <div className="flex flex-wrap gap-2">
          {mission.schema.map((table) => (
            <span
              key={table.name}
              className="inline-flex items-center gap-1.5 font-mono text-xs px-2.5 py-1 rounded-md bg-secondary/10 text-secondary border border-secondary/20"
            >
              <Database className="w-3 h-3" />
              {table.name}
            </span>
          ))}
        </div>
        <p className="font-mono text-[10px] text-muted-foreground mt-2">
          Switch to the Schema tab to explore table data →
        </p>
      </div>
    </motion.div>

    {/* (4) Hint — accent color, collapsible */}
    {mission.hint && (
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.2 }}>
        <button
          onClick={() => setShowHint(!showHint)}
          className="font-mono text-[10px] text-accent hover:text-accent/80 transition-colors"
        >
          {showHint ? "Hide hint" : "💡 Show hint"}
        </button>
        {showHint && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            className="mt-2 rounded-lg bg-accent/5 border border-accent/20 p-3"
          >
            <p className="font-mono text-xs text-accent">{mission.hint}</p>
          </motion.div>
        )}
      </motion.div>
    )}
  </div>
</TabsContent>
```

Block-by-block rules:

| Block | Surface | Why this surface |
| --- | --- | --- |
| Heading | none | Sits directly on the panel — it IS the panel intro. |
| Objective | `bg-card border border-border` | Promoted as the most important block. |
| Tables | `bg-muted/30 border border-border/50` | Demoted to "reference info". The chips inside use **secondary**. |
| Hint | `bg-accent/5 border border-accent/20` | Amber = optional / advisory. Never primary or destructive. |

Pill specifications inside the heading:

- Difficulty: `font-mono text-[10px] uppercase tracking-widest px-2 py-0.5 rounded` + colors from `difficultyConfig[mission.difficulty]`.
- Category: `font-mono text-[10px] px-2 py-0.5 rounded` + colors from `categoryConfig[mission.category]`. **No `uppercase`** — categories include emoji, which uppercase corrupts visually.
- XP: `font-mono text-[10px] text-accent`. **No background** — XP is a value, not a chip.
- Solved: `ml-auto text-primary` with a `CheckCircle2 w-3 h-3`. `ml-auto` pushes it to the far right of the badges row.

#### 3.4.3 Schema tab body

```tsx
<TabsContent value="schema" className="flex-1 overflow-auto px-5 pb-5 mt-0">
  <div className="pt-4">
    <h2 className="font-mono text-[10px] text-muted-foreground uppercase tracking-wider mb-3">
      Database Schema — click a table to explore
    </h2>
    <DataViewer schema={mission.schema} sampleData={mission.sampleData} />
  </div>
</TabsContent>
```

Just an eyebrow heading + `<DataViewer />` (see §3.7). Do not add filters, search, or sub-tabs here — schema exploration is intentionally low-friction.

---

### 3.5 Right column — workbench

Three vertical regions in `w-[62%] flex flex-col min-h-0 p-4 gap-3`:

```tsx
<div className="w-[62%] flex flex-col min-h-0 p-4 gap-3">
  {/* (1) Editor — flex-1 */}
  <div className="flex-1 min-h-0">
    <SqlEditor value={query} onChange={setQuery} onSubmit={handleRun} />
  </div>

  {/* (2) Action bar — shrink-0 */}
  <div className="flex items-center justify-between shrink-0">
    {/* §3.5.1 left side */}
    {/* §3.5.2 buttons */}
  </div>

  {/* (3) Results — flex-1 */}
  <div className="flex-1 min-h-0 rounded-lg border border-border bg-card overflow-hidden">
    <ResultsPane result={result} runId={runId} />
  </div>
</div>
```

Layout rules:

- `p-4 gap-3` is the only padding/gap on this column. Children add their own internal padding.
- Editor and Results both use `flex-1 min-h-0` and therefore **share the remaining vertical space ~50/50**. To shift the ratio, use `flex-[2]` / `flex-[1]` — never hardcoded heights.
- The Results wrapper carries the **card chrome** (`rounded-lg border bg-card overflow-hidden`); `ResultsPane` itself does not draw a border. The editor is the inverse: `SqlEditor` draws its own chrome.

#### 3.5.1 Action bar — left side (status zone)

```tsx
<div className="flex items-center gap-3">
  <span className="font-mono text-[10px] text-muted-foreground">
    {query.length > 0 ? `${query.length} chars` : ""}
  </span>
  <AnimatePresence>
    {dbModified && (
      <motion.span
        initial={{ opacity: 0, scale: 0.8 }}
        animate={{ opacity: 1, scale: 1 }}
        exit={{ opacity: 0, scale: 0.8 }}
        className="inline-flex items-center gap-1 font-mono text-[10px] text-accent px-2 py-0.5 rounded-full bg-accent/10 border border-accent/20"
      >
        <HardDriveDownload className="w-3 h-3" />
        Modified
      </motion.span>
    )}
  </AnimatePresence>
</div>
```

- Char counter is **silent until the user types** (empty string when `query.length === 0`). Do not render `0 chars`.
- "Modified" pill is **amber (accent)** because schema mutation is a *warning state*, not an error. It animates in/out via `<AnimatePresence>` + `scale 0.8 → 1`.

#### 3.5.2 Action bar — buttons

Order is fixed: **Restore → Run → Verify**, left to right, escalating commitment. Encoded by border weight + glow.

```tsx
<div className="flex items-center gap-2">
  {/* Restore — muted, destructive-ish but recoverable */}
  <motion.button
    whileTap={{ scale: 0.95 }}
    onClick={handleRestore}
    disabled={restoring}
    className="font-mono text-xs font-medium px-3 py-2 rounded-md border border-border bg-muted/30 text-muted-foreground hover:text-accent hover:border-accent/30 hover:bg-accent/5 transition-all flex items-center gap-1.5 disabled:opacity-40 disabled:cursor-not-allowed"
    title="Restore all tables to their original state"
  >
    {restoring ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <RotateCcw className="w-3.5 h-3.5" />}
    {restoring ? "Restoring…" : "Restore DB"}
  </motion.button>

  {/* Run — neutral, repeatable */}
  <motion.button
    whileTap={{ scale: 0.97 }}
    onClick={handleRun}
    className="font-mono text-sm font-medium px-5 py-2 rounded-md border border-border bg-muted/50 text-foreground hover:bg-muted hover:border-foreground/30 transition-all flex items-center gap-2"
  >
    <Play className="w-3.5 h-3.5" />
    Run Query
  </motion.button>

  {/* Verify — primary, scored action */}
  <motion.button
    whileTap={{ scale: 0.97 }}
    onClick={handleVerify}
    disabled={verifying || query.trim().length === 0}
    className={`font-mono text-sm font-medium px-5 py-2 rounded-md border-2 transition-all flex items-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed ${
      verifyResult === "success"
        ? "border-primary text-primary glow-primary bg-primary/5"
        : verifyResult === "fail"
        ? "border-destructive text-destructive glow-error bg-destructive/5"
        : "border-primary/50 text-primary hover:border-primary hover:glow-primary"
    }`}
  >
    {verifying ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <CheckCircle2 className="w-3.5 h-3.5" />}
    {verifying ? "Verifying…" : verifyResult === "success" ? "Verified ✓" : verifyResult === "fail" ? "Incorrect" : "Verify Result"}
  </motion.button>
</div>
```

Anatomy table:

| Button | Size | Border | Surface | Icon | Loading copy |
| --- | --- | --- | --- | --- | --- |
| Restore | `text-xs px-3 py-2` | `border` | `bg-muted/30` | `RotateCcw` | "Restoring…" |
| Run | `text-sm px-5 py-2` | `border` | `bg-muted/50` | `Play` | — |
| Verify | `text-sm px-5 py-2` | **`border-2`** | none / `bg-primary/5` | `CheckCircle2` | "Verifying…" |

- **`border-2` is reserved for Verify.** That extra weight is the visual cue for "this is the action that scores you". Do not put `border-2` on Run or Restore.
- Verify has **exactly three visual states** picked by ternary — never additive. Switching to additive classes will let `success` and `fail` styles overlap during transitions.
- Disabled handling is uniform: `disabled:opacity-40 disabled:cursor-not-allowed`. Verify is disabled when `verifying || query.trim().length === 0`. Restore is disabled while `restoring`.

---

### 3.6 SqlEditor (`src/components/SqlEditor.tsx`)

> Applies to: `src/components/SqlEditor.tsx` only.

Full component shell:

```tsx
<div className="h-full flex flex-col bg-editor rounded-lg border border-border overflow-hidden">
  {/* Title bar */}
  <div className="flex items-center justify-between px-4 py-2.5 border-b border-border bg-muted/30">
    <div className="flex items-center gap-2">
      <div className="w-2 h-2 rounded-full bg-destructive/60" />
      <div className="w-2 h-2 rounded-full bg-accent/60" />
      <div className="w-2 h-2 rounded-full bg-primary/60" />
    </div>
    <span className="font-mono text-[10px] text-muted-foreground">⌘+Enter to run</span>
  </div>

  {/* Textarea */}
  <textarea
    ref={textareaRef}
    value={value}
    onChange={(e) => onChange(e.target.value)}
    onKeyDown={handleKeyDown}
    className="flex-1 w-full bg-transparent font-mono text-sm text-foreground p-4 resize-none outline-none leading-relaxed placeholder:text-muted-foreground/40"
    placeholder="Write your SQL query here..."
    spellCheck={false}
    autoCapitalize="off"
    autoCorrect="off"
  />
</div>
```

Specifics:

- Outer surface uses the **dedicated `bg-editor` token** (darker than `bg-card`). This is the only place in the app that uses `bg-editor`.
- The three "traffic-light" dots are decorative — `w-2 h-2 rounded-full` colored `bg-destructive/60` / `bg-accent/60` / `bg-primary/60` (red / amber / teal). Do not wire interaction. Do not change the order.
- Right-side hint is `font-mono text-[10px] text-muted-foreground` reading literally `⌘+Enter to run`.
- Textarea: `bg-transparent` (so `bg-editor` shows through), `outline-none` (the editor frame IS the focus indicator — do not add a focus ring), `resize-none` (the parent owns sizing), `placeholder:text-muted-foreground/40` for low-contrast hint text.
- Required JS behavior: auto-focus on mount, `Tab` inserts two spaces (no focus loss), `⌘/Ctrl+Enter` calls `onSubmit`.

---

### 3.7 DataViewer (`src/components/DataViewer.tsx`)

> Applies to: `src/components/DataViewer.tsx` only.

Vertical accordion of tables. **One expansion at a time** — single `expandedTable: string | null` state. Do not switch to `Set` semantics; users should focus on one table.

Per-table card:

```tsx
<div className="rounded-lg border border-border bg-card overflow-hidden transition-all">
  {/* Header — clickable */}
  <button
    onClick={() => setExpandedTable(isExpanded ? null : table.name)}
    className="w-full flex items-center gap-3 px-4 py-3 hover:bg-muted/30 transition-colors group"
  >
    <motion.div animate={{ rotate: isExpanded ? 90 : 0 }} transition={{ duration: 0.2 }}>
      <ChevronRight className="w-4 h-4 text-muted-foreground group-hover:text-primary transition-colors" />
    </motion.div>
    <Table2 className="w-4 h-4 text-primary" />
    <span className="font-mono text-sm font-medium text-primary">{table.name}</span>
    <span className="font-mono text-[10px] text-muted-foreground ml-auto">
      {table.columns.length} cols · {rows.length} rows
    </span>
  </button>

  {/* Expansion */}
  <AnimatePresence>
    {isExpanded && (
      <motion.div
        initial={{ height: 0, opacity: 0 }}
        animate={{ height: "auto", opacity: 1 }}
        exit={{ height: 0, opacity: 0 }}
        transition={{ duration: 0.25, ease: "easeInOut" }}
        className="overflow-hidden"
      >
        {/* Columns block */}
        <div className="px-4 pb-2 border-t border-border/50">
          <div className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground py-2">Columns</div>
          <div className="grid gap-1">
            {table.columns.map((col) => (
              <div key={col.name} className="flex items-center gap-2 px-3 py-1.5 rounded-md bg-muted/30">
                <span className="text-muted-foreground">{typeIcon(col.type)}</span>
                <span className="font-mono text-xs font-medium">{col.name}</span>
                <span className="font-mono text-[10px] text-muted-foreground ml-auto px-1.5 py-0.5 rounded bg-muted">
                  {col.type}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Sample data table */}
        {rows.length > 0 && (
          <div className="px-4 pb-3 border-t border-border/50">
            <div className="font-mono text-[10px] uppercase tracking-wider text-muted-foreground py-2">Sample Data</div>
            <div className="rounded-md border border-border overflow-x-auto">
              <table className="w-full font-mono text-xs">
                <thead>
                  <tr className="bg-muted/50">
                    {table.columns.map((col) => (
                      <th key={col.name} className="text-left py-2 px-3 font-medium text-muted-foreground border-b border-border whitespace-nowrap">
                        {col.name}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {rows.map((row, i) => (
                    <tr key={i} className="border-b border-border/30 last:border-0 hover:bg-muted/20 transition-colors">
                      {table.columns.map((col) => (
                        <td key={col.name} className="py-1.5 px-3 whitespace-nowrap">
                          {row[col.name] === null
                            ? <span className="text-muted-foreground/40">NULL</span>
                            : String(row[col.name])}
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </motion.div>
    )}
  </AnimatePresence>
</div>
```

Type-icon picker (keep this list narrow — add new types here, not at call sites):

```tsx
const typeIcon = (type: string) => {
  const t = type.toUpperCase();
  if (t.includes("INT") || t.includes("DECIMAL")) return <Hash className="w-3 h-3" />;
  if (t.includes("TEXT") || t.includes("VARCHAR")) return <Type className="w-3 h-3" />;
  return <Key className="w-3 h-3" />;
};
```

Color choice clarification (this trips people up):

- **Inside DataViewer**, table icons + names are `text-primary` because each table sits on its own dedicated `bg-card`.
- **In §3.4.2's "Tables Available" chips**, the same names are `text-secondary` because they sit on a muted reference panel.

That is intentional, not a bug. Surface dictates color, not entity.

---

### 3.8 ResultsPane (`src/components/ResultsPane.tsx`)

> Applies to: `src/components/ResultsPane.tsx` only.

Three **mutually exclusive** states — render exactly one, never overlap.

#### State 1 — Empty (`result === null`)

```tsx
<div className="h-full flex items-center justify-center">
  <span className="font-mono text-xs text-muted-foreground/50">Output will appear here</span>
</div>
```

Centered placeholder. The `/50` opacity is what makes it read as "waiting" instead of "disabled".

#### State 2 — Error (`result.error`)

```tsx
<AnimatePresence>
  <motion.div initial={{ opacity: 0, y: 5 }} animate={{ opacity: 1, y: 0 }} className="h-full p-4">
    <div className="rounded-lg border border-destructive/30 bg-destructive/5 p-4 glow-error">
      <span className="font-mono text-[10px] text-destructive uppercase tracking-wider block mb-2">Error</span>
      <p className="font-mono text-sm leading-relaxed text-foreground">{result.error}</p>
    </div>
  </motion.div>
</AnimatePresence>
```

- Card chrome is destructive (`border-destructive/30`, `bg-destructive/5`, `glow-error`).
- Eyebrow ("Error") is `text-destructive`.
- Body copy is **`text-foreground`**, not `text-destructive` — error chrome is red, but the *message* must stay legible. This is a recurring rookie mistake.

#### State 3 — Success / rows

```tsx
<div className="h-full overflow-auto p-4">
  {result.success && (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}
      className="rounded-lg border border-primary/30 bg-primary/5 px-4 py-2.5 mb-3 glow-success"
    >
      <span className="font-mono text-xs text-primary font-medium">✓ Query correct — mission complete!</span>
    </motion.div>
  )}

  <div className="rounded-lg border border-border overflow-hidden">
    <table className="w-full font-mono text-xs">
      {showHeaders && (
        <thead>
          <tr className="bg-muted/50">
            {result.columns.map((col) => (
              <th key={col} className="text-left py-2 px-3 font-medium text-muted-foreground border-b border-border">
                {col}
              </th>
            ))}
          </tr>
        </thead>
      )}
      <tbody>
        {result.rows.slice(0, visibleRows).map((row, i) => (
          <motion.tr
            key={i}
            initial={{ opacity: 0, x: -5 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.15 }}
            className="border-b border-border/50 last:border-0 hover:bg-muted/20 transition-colors"
          >
            {row.map((cell, j) => (
              <td key={j} className="py-2 px-3">
                {cell === null ? <span className="text-muted-foreground/40">NULL</span> : String(cell)}
              </td>
            ))}
          </motion.tr>
        ))}
      </tbody>
    </table>
  </div>
</div>
```

Animation timing — these constants live inside the component, do **not** tune them per page:

```ts
useEffect(() => {
  if (!result || result.error) { setShowHeaders(true); setVisibleRows(0); return; }
  setShowHeaders(false);
  setVisibleRows(0);
  const ht = setTimeout(() => setShowHeaders(true), 60);          // headers reveal at 60ms
  const timers: ReturnType<typeof setTimeout>[] = [];
  result.rows.forEach((_, i) => {
    timers.push(setTimeout(() => setVisibleRows(i + 1), 140 + i * 50)); // rows: 140ms + 50ms each
  });
  return () => { clearTimeout(ht); timers.forEach(clearTimeout); };
}, [result, runId]);
```

Table chrome rules:

- Outer `rounded-lg border border-border overflow-hidden` provides the frame; the wrapper around `ResultsPane` (in §3.5) adds a second `bg-card` layer behind it.
- Header row: `bg-muted/50`, cells `text-left py-2 px-3 font-medium text-muted-foreground border-b border-border`.
- Body rows: `border-b border-border/50 last:border-0 hover:bg-muted/20`. The `last:border-0` removes the trailing divider so the table doesn't double up its own bottom border.
- NULLs render as `<span className="text-muted-foreground/40">NULL</span>` — same token as DataViewer. Do not use `text-destructive` or italics.

---

### 3.9 Mission Page invariants checklist

When editing `MissionPage.tsx` or any of its children, every PR must still satisfy:

- [ ] Outer wrapper is exactly `h-screen flex flex-col overflow-hidden gradient-mesh`.
- [ ] Every direct child of the outer wrapper has `shrink-0` or `flex-1 min-h-0`.
- [ ] Split is `w-[38%]` / `w-[62%]` — not `flex-1`, not `lg:w-1/2`.
- [ ] Tabs: Mission = primary (teal), Schema = secondary (indigo).
- [ ] Action bar order is Restore → Run → Verify, no exceptions.
- [ ] Verify has exactly three visual states (idle / success / fail) chosen by **ternary**, not class concatenation.
- [ ] Only Verify uses `border-2`. Run and Restore use `border`.
- [ ] All numeric / metadata text is `font-mono`; all prose is `font-sans`.
- [ ] No raw colors (`text-white`, `bg-[#…]`, `border-gray-…`). Tokens only.
- [ ] No hardcoded heights on editor or results — they share vertical space via `flex-1 min-h-0`.
- [ ] Animations use `framer-motion` (no `transition-*` for mount animations).
- [ ] NULL renders as `<span className="text-muted-foreground/40">NULL</span>` everywhere.

---

## 4. Auth pages

> Applies to: `src/pages/LoginPage.tsx`, `src/pages/RegisterPage.tsx`.

Centered single-card layout: `min-h-screen flex items-center justify-center gradient-mesh px-5`. Card uses `bg-card border border-border rounded-2xl p-8 w-full max-w-md`. Inputs use shadcn `<Input />` and `<Label />`. Primary submit button uses the default shadcn `Button` (variant `default`, which already maps to `bg-primary text-primary-foreground`).

---

## 5. Profile Page — Build Guide (`src/pages/ProfilePage.tsx`)

> Applies to: `src/pages/ProfilePage.tsx` only. Sibling page-types (Leaderboard, Admin) reuse the same shell + tile patterns — see §5.8.

### 5.0 Mental model

The Profile page is the **player's trophy room**. Every block answers one question:

| Block | Question it answers | Visual register |
|---|---|---|
| Identity card (§5.2) | "Who am I, and how strong am I?" | Hero, gradient avatar, large name, XP bar |
| Stat strip (§5.3) | "What is my run summary?" | 4 quiet tiles, mono numerals |
| Learned techniques (§5.4) | "What can I do?" | Primary-tinted chips, celebratory |
| Yet-to-discover (§5.5) | "What's next?" | Muted chips, locked, hover reveals source |
| Mission Log (§5.6) | "Where have I been?" | Compact list, ✓ / ○ markers |

Reading order is top-down, **never side-by-side mixed registers**: identity is hero, then summary, then two parallel lists, then history. The two technique lists are deliberately the **only** side-by-side pairing — they answer mirror questions ("known" vs "unknown") and must occupy the same row.

### 5.1 Page shell — exact HTML

```tsx
<div className="min-h-screen flex flex-col gradient-mesh">
  <TopBar />
  <div className="flex-1 px-6 md:px-10 py-8">
    <div className="max-w-4xl mx-auto space-y-8">
      {/* 1. Identity card     §5.2 */}
      {/* 2. Two-up techniques §5.4 + §5.5  (grid md:grid-cols-2 gap-6) */}
      {/* 3. Mission Log       §5.6 */}
    </div>
  </div>
</div>
```

**Invariants:**
- Outer wrapper is `min-h-screen` (NOT `h-screen`) — Profile scrolls naturally; do not trap it like the MissionPage workbench.
- Background is `gradient-mesh` (defined in `index.css`) — same as auth pages, distinct from the flat `bg-background` of MissionPage.
- Content cap is **`max-w-4xl`** (768px), narrower than Leaderboard/Admin's `max-w-6xl`. Profile is a personal page; the narrower column improves readability of the mission log.
- Vertical rhythm between blocks is `space-y-8` (32px). Inside a block, use `space-y-4` or grid `gap-6`.
- Horizontal padding scales: `px-6` mobile, `md:px-10` desktop.

### 5.2 Identity card

```tsx
<motion.div
  initial={{ opacity: 0, y: -10 }}
  animate={{ opacity: 1, y: 0 }}
  className="rounded-xl border border-border bg-card p-6 md:p-8"
>
  <div className="flex flex-col sm:flex-row items-start sm:items-center gap-6">
    {/* Avatar (§5.2.1) */}
    {/* Name + XP (§5.2.2) */}
  </div>
  {/* Stat grid (§5.3) — separated by border-t */}
</motion.div>
```

Container rules:
- `rounded-xl` (12px) — larger than the inner cards (`rounded-lg`) to mark hero status.
- `bg-card` over `gradient-mesh` reads as a raised pane. **Do not** add a shadow — depth comes from the gradient backdrop.
- Padding scales: `p-6` mobile, `md:p-8` desktop. The card breathes more than secondary cards (`p-5`).
- Mount animation: only this card animates from `y: -10`. Subsequent blocks animate from `y: 10` with staggered delays (0.1s, 0.2s, 0.3s) — they *fall in* while the identity card *drops down*.

#### 5.2.1 Avatar tile

```tsx
<button onClick={cycleAvatar} className="relative group shrink-0" title="Click to change avatar">
  <div className={`w-20 h-20 rounded-2xl bg-gradient-to-br ${avatarColor}
                   flex items-center justify-center
                   text-3xl font-bold text-primary-foreground
                   transition-transform group-hover:scale-105`}>
    {initial}
  </div>
  <div className="absolute -bottom-1.5 -right-1.5
                  w-7 h-7 rounded-full bg-accent text-accent-foreground
                  flex items-center justify-center text-xs font-bold
                  border-2 border-card">
    {level}
  </div>
</button>
```

- `w-20 h-20` (80px) — exactly **double** the TopBar UserChip avatar (40px). The Profile is the canonical, large form.
- `rounded-2xl` (16px) — softer than the TopBar's `rounded-lg`. Hero context allows more friendliness.
- Gradient direction is always `bg-gradient-to-br` (top-left → bottom-right). Six seed-mapped gradients live in `AVATAR_COLORS` (file-top constant). **Never** inline a new gradient — extend the map.
- The level badge is `w-7 h-7` (TopBar uses `w-5`), positioned at `-bottom-1.5 -right-1.5` and ringed with `border-2 border-card` so it visually punches *through* the card edge.
- Hover affordance: only the inner gradient tile scales (`group-hover:scale-105`); the badge stays put. This communicates "click the face, not the number."
- Cursor stays default (`button` element) — hand cursor is implicit; do not add `cursor-pointer`.

#### 5.2.2 Name, level line, and XP bar

```tsx
<div className="flex-1 min-w-0">
  {/* Name row — edit / display swap */}
  <div className="flex items-center gap-2 mb-1">
    {editing ? (
      <div className="flex items-center gap-2">
        <input
          value={nameInput}
          onChange={(e) => setNameInput(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSaveName()}
          className="font-sans text-xl font-bold bg-muted rounded-lg px-3 py-1
                     outline-none focus:ring-1 ring-primary text-foreground w-48"
          autoFocus
          maxLength={16}
        />
        <button onClick={handleSaveName} className="text-primary hover:text-primary/80">
          <Check className="w-4 h-4" />
        </button>
      </div>
    ) : (
      <div className="flex items-center gap-2">
        <h1 className="font-sans text-xl font-bold">{profile.username}</h1>
        <button onClick={enterEdit} className="text-muted-foreground hover:text-foreground transition-colors">
          <Pencil className="w-3.5 h-3.5" />
        </button>
      </div>
    )}
  </div>

  {/* Sub-line: level + total XP, mono */}
  <p className="font-mono text-xs text-muted-foreground mb-3">
    Level {level} · {totalXp} XP total
  </p>

  {/* XP bar (max-w-md so it never stretches the full card) */}
  <div className="flex items-center gap-3 max-w-md">
    <div className="flex-1 h-2 rounded-full bg-muted overflow-hidden">
      <motion.div
        className="h-full rounded-full bg-gradient-to-r from-primary to-accent"
        initial={{ width: 0 }}
        animate={{ width: `${(xpProgress / xpForNextLevel) * 100}%` }}
        transition={{ duration: 0.8 }}
      />
    </div>
    <span className="font-mono text-[10px] text-muted-foreground shrink-0">
      {xpProgress}/{xpForNextLevel} XP to Level {level + 1}
    </span>
  </div>
</div>
```

Rules:
- The `<h1>` lives **here** — there is one and only one `<h1>` per page (SEO + a11y). Use `text-xl font-bold` (not `text-2xl`); the gradient avatar is doing the heavy hero lifting visually.
- Name is `font-sans`, sub-line and XP counter are `font-mono`. The split is consistent with the rest of the app: prose / chrome → sans; numbers / metadata → mono.
- Edit/display swap MUST be in the same DOM slot — no layout shift when toggling. The input width (`w-48`) approximates the longest allowed name (`maxLength={16}` × ~12px).
- XP bar height is `h-2` (TopBar uses `h-1.5`). Profile is the canonical larger form.
- XP bar is capped at `max-w-md` (28rem). Stretching it edge-to-edge inside an 8xl card looks like a loading skeleton — keep it visually anchored to the name.
- Bar fill animation is `duration: 0.8` (slow, ceremonial). The TopBar bar uses `0.6`. Profile is where the player lingers; the longer fill is felt, not seen.

### 5.3 Stat strip (4 tiles)

```tsx
<div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-6 pt-6 border-t border-border">
  <StatCard icon={<Trophy className="w-4 h-4" />} value={completedCount}             label="Solved"     color="text-primary" />
  <StatCard icon={<Star    className="w-4 h-4" />} value={totalXp}                   label="Total XP"   color="text-accent"  />
  <StatCard icon={<Flame   className="w-4 h-4" />} value={totalMissions - completedCount} label="Remaining" color="text-secondary" />
  <StatCard icon={<Zap     className="w-4 h-4" />} value={learned.length}            label="Techniques" color="text-primary" />
</div>
```

```tsx
const StatCard = ({ icon, value, label, color }) => (
  <div className="text-center rounded-lg bg-muted/30 border border-border py-3 px-2">
    <div className={`flex items-center justify-center gap-1.5 ${color}`}>
      {icon}
      <span className="font-mono text-lg font-semibold">{value}</span>
    </div>
    <p className="font-mono text-[9px] text-muted-foreground mt-0.5">{label}</p>
  </div>
);
```

- The strip is **inside** the identity card, separated only by `border-t border-border` + `mt-6 pt-6`. Do NOT promote it to a sibling card — it's a footer to the identity, not its own block.
- Surface is `bg-muted/30` (semi-transparent muted) — quieter than `bg-card`, so tiles read as recessed wells inside the card.
- Color story (fixed, do not shuffle):
  - `text-primary` for **achievement** counters (Solved, Techniques)
  - `text-accent` for **economy** counters (XP)
  - `text-secondary` for **remaining work** counters (Remaining)
- All values are `font-mono text-lg font-semibold` — numbers; labels are `text-[9px]` mono uppercase-feel without actual uppercase. Resist the urge to bump label size — the tile's job is the **number**.
- Grid is `grid-cols-2` on mobile, `sm:grid-cols-4` from 640px. Never 3-up — it leaves a lonely tile.

### 5.4 Learned techniques card

```tsx
<motion.div
  initial={{ opacity: 0, y: 10 }}
  animate={{ opacity: 1, y: 0 }}
  transition={{ delay: 0.1 }}
  className="rounded-xl border border-border bg-card p-5"
>
  <div className="flex items-center gap-2 mb-4">
    <BookOpen className="w-4 h-4 text-primary" />
    <h2 className="font-sans text-sm font-semibold">Learned Techniques</h2>
    <span className="ml-auto font-mono text-[10px] text-muted-foreground">
      {learned.length}/{allTechniques.length}
    </span>
  </div>

  {learned.length === 0 ? (
    <p className="font-mono text-xs text-muted-foreground py-4 text-center">
      Complete missions to learn SQL techniques!
    </p>
  ) : (
    <div className="flex flex-wrap gap-2">
      {learned.map((tech) => (
        <motion.span
          key={tech}
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          className="inline-flex items-center gap-1.5 font-mono text-xs px-3 py-1.5
                     rounded-lg bg-primary/10 text-primary border border-primary/20"
        >
          <Zap className="w-3 h-3" />
          {tech}
        </motion.span>
      ))}
    </div>
  )}
</motion.div>
```

- Card is `p-5` (smaller than identity's `p-6/8`) — it is a secondary block.
- Header pattern (reused in §5.5 and §5.6): `<icon> <h2> <ml-auto counter>`. Always `<h2>` here, never `<h3>` — these are top-level page sections.
- Empty state: centered mono copy, `py-4` so the card doesn't collapse to nothing. Never render the chip container at all when empty (no zero-height flex-wrap).
- Chip token (mandatory, do not invent variants):
  - shape: `rounded-lg px-3 py-1.5`
  - color: `bg-primary/10 text-primary border border-primary/20` (10/20 alpha pair — same as TopBar dropdown chips)
  - typography: `font-mono text-xs`
  - icon: `Zap`, `w-3 h-3`, **always** prefixed (no chip without an icon)
- Chips animate `scale: 0.9 → 1` on mount. No `delay` per chip — they pop together; the per-card stagger handled by the parent's `transition.delay`.

### 5.5 Yet-to-discover card (mirror of §5.4)

```tsx
<motion.div
  initial={{ opacity: 0, y: 10 }}
  animate={{ opacity: 1, y: 0 }}
  transition={{ delay: 0.2 }}
  className="rounded-xl border border-border bg-card p-5"
>
  <div className="flex items-center gap-2 mb-4">
    <Lock className="w-4 h-4 text-muted-foreground" />
    <h2 className="font-sans text-sm font-semibold">Yet to Discover</h2>
    <span className="ml-auto font-mono text-[10px] text-muted-foreground">
      {notLearned.length} remaining
    </span>
  </div>

  {notLearned.length === 0 ? (
    <p className="font-mono text-xs text-primary py-4 text-center">
      🎉 You've mastered all techniques!
    </p>
  ) : (
    <div className="flex flex-wrap gap-2">
      {notLearned.map((tech) => {
        const source = techniqueSource(tech);
        return (
          <div key={tech} className="group relative">
            <span className="inline-flex items-center gap-1.5 font-mono text-xs px-3 py-1.5
                             rounded-lg bg-muted/50 text-muted-foreground border border-border">
              <Lock className="w-3 h-3" />
              {tech}
            </span>
            {source && (
              <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-1.5
                              px-2.5 py-1 rounded-md bg-popover border border-border
                              font-mono text-[9px] text-muted-foreground whitespace-nowrap
                              opacity-0 group-hover:opacity-100 transition-opacity
                              pointer-events-none z-10">
                Unlocked in: {source}
              </div>
            )}
          </div>
        );
      })}
    </div>
  )}
</motion.div>
```

Mirror rules vs §5.4 — these MUST match so the two cards read as a pair:
- Same dimensions, padding, radius, header pattern, chip shape.
- Only **two axes** flip:
  1. Header icon: `BookOpen / text-primary` → `Lock / text-muted-foreground`.
  2. Chip color: `bg-primary/10 text-primary border-primary/20` → `bg-muted/50 text-muted-foreground border-border`. The chip icon mirrors: `Zap` → `Lock`.
- Counter copy diverges intentionally: "X/Y" (progress) vs "N remaining" (work-to-do framing).
- Tooltip is **CSS-only** (no Radix `Tooltip`) — it is decorative/contextual, not critical info, and we want it to live inside the natural document flow without portal cost. Pattern: `group` on wrapper, `opacity-0 group-hover:opacity-100 transition-opacity` on the floating element, `pointer-events-none` so it never blocks chip hit-area. Position is `bottom-full left-1/2 -translate-x-1/2 mb-1.5` and surface `bg-popover border border-border`.
- Empty state turns **celebratory** (`text-primary`, emoji). This is the only state where the muted card wears a primary color — earned by total mastery.
- Animation delay is `0.2s` (vs `0.1s` for §5.4) so the pair cascades left → right.

### 5.6 Mission Log

```tsx
<motion.div
  initial={{ opacity: 0, y: 10 }}
  animate={{ opacity: 1, y: 0 }}
  transition={{ delay: 0.3 }}
  className="rounded-xl border border-border bg-card p-5"
>
  <h2 className="font-sans text-sm font-semibold mb-4">Mission Log</h2>
  <div className="space-y-2">
    {missions.map((m) => {
      const done = completedMissions.has(m.id);
      return (
        <div
          key={m.id}
          className={`flex items-center justify-between px-3 py-2 rounded-lg border transition-colors ${
            done ? "border-primary/20 bg-primary/5" : "border-border bg-muted/20"
          }`}
        >
          <div className="flex items-center gap-2 min-w-0">
            <span className={`font-mono text-[10px] ${done ? "text-primary" : "text-muted-foreground"}`}>
              {done ? "✓" : "○"}
            </span>
            <span className={`font-sans text-xs truncate ${done ? "text-foreground" : "text-muted-foreground"}`}>
              {m.title}
            </span>
          </div>
          <div className="flex items-center gap-2 shrink-0">
            {m.techniques.map((t) => (
              <span
                key={t}
                className={`font-mono text-[9px] px-1.5 py-0.5 rounded ${
                  done ? "bg-primary/10 text-primary" : "bg-muted text-muted-foreground"
                }`}
              >
                {t}
              </span>
            ))}
            <span className="font-mono text-[10px] text-accent ml-1">{m.xp} XP</span>
          </div>
        </div>
      );
    })}
  </div>
</motion.div>
```

- Row state is binary: `done` (primary tint) vs `not done` (muted). There is no "in-progress" row — the engine has no concept of partial completion.
- Status glyphs are `✓` (U+2713) and `○` (U+25CB), **not** lucide icons. Text glyphs ensure perfect baseline alignment with the title text.
- Title uses `truncate` + `min-w-0` on the parent flex item — without `min-w-0`, the truncation silently no-ops in flex children.
- Trailing meta cluster (`shrink-0`) holds **two kinds** of chips:
  1. Technique mini-chips: `text-[9px] px-1.5 py-0.5 rounded` (no `-lg` — these are smaller than the §5.4/§5.5 chips and must read as metadata, not features).
  2. XP value: `text-accent`, no chip background — it's a numeric, not a tag.
- Row gap is `space-y-2` (8px). Tighter than top-level `space-y-8` because rows belong to one block.
- Mount delay is `0.3s` — last in the cascade.

### 5.7 Avatar system reference

Two file-top constants are the **single source of truth**:

```tsx
const AVATAR_SEEDS = ["hero", "mage", "rogue", "sage", "knight", "druid"];
const AVATAR_COLORS: Record<string, string> = {
  default: "from-primary to-secondary",
  hero:    "from-destructive to-accent",
  mage:    "from-secondary to-primary",
  rogue:   "from-accent to-destructive",
  sage:    "from-primary to-accent",
  knight:  "from-secondary to-destructive",
  druid:   "from-primary to-secondary",
};
```

- Adding a seed = add to **both** maps. `cycleAvatar` walks `AVATAR_SEEDS` modulo length.
- Every gradient is a token-pair (`from-X to-Y`) — never a hex, never `from-blue-500`.
- The same map is consumed by `ProfileCard.tsx` (the small in-app avatar). Keeping a single source means the small/large avatars match colors exactly.

### 5.8 Sibling page-types (Leaderboard, Admin)

These pages reuse the Profile shell **except**:
- Content cap widens to `max-w-6xl` (Leaderboard tables, Admin forms need horizontal room).
- They do **not** carry the identity card; their hero is a page title row (`<h1 className="font-sans text-2xl font-bold">…</h1>` + sub-line).
- Tile, chip, and row patterns are identical — re-import the same look from §5.3, §5.4, §5.6 rather than re-deriving.

### 5.9 Profile Page invariants checklist

Before merging changes to `ProfilePage.tsx`:

- [ ] Outer wrapper is `min-h-screen flex flex-col gradient-mesh` (not `h-screen`, not `bg-background`).
- [ ] Exactly one `<h1>`, inside the identity card.
- [ ] Section headers are `<h2 className="font-sans text-sm font-semibold">`.
- [ ] All numeric values render in `font-mono`. All names/titles/headers in `font-sans`.
- [ ] No new chip variants — chips are either "earned" (primary 10/20) or "locked" (muted/50 + border).
- [ ] No raw colors. Avatar gradients come from `AVATAR_COLORS`; tile colors come from `text-primary | text-accent | text-secondary`.
- [ ] Mount animations are staggered: identity `y: -10`, then `y: 10` with delays `0.1, 0.2, 0.3`.
- [ ] Tooltip on locked chips is CSS-only (`group` + `opacity-0 group-hover:opacity-100`), never Radix.
- [ ] Edit-name input occupies the same slot as the displayed name — no layout shift on toggle.
- [ ] Empty states render centered mono copy, never a zero-height container.

---

## 6. Anti-patterns (do not ship)

- ❌ Mixing `font-sans` and `font-mono` for the same kind of content within one screen.
- ❌ `border-2` on non-Verify CTAs — that thickness is reserved for the "this is the action that scores you" button.
- ❌ Using `bg-primary` as a passive surface — primary is for actions, success, and brand. Surfaces are `bg-card`, `bg-muted`, `bg-surface-raised`.
- ❌ Adding a third tab to the left panel without redesigning the trigger color scheme (currently a 2-color story).
- ❌ Replacing `framer-motion` row/tab animations with CSS transitions — the staggered timing depends on JS.
- ❌ Removing `min-h-0` from any flex column inside MissionPage. It WILL break scroll.
- ❌ Coloring Verify error copy `text-destructive` — chrome is red, copy stays `text-foreground`.
- ❌ Adding padding to the outer `h-screen` wrapper — spacing belongs to inner regions only.
