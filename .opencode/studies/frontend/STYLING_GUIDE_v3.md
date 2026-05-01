# SQLab — Styling Guide (v3)

> Authoritative reference for building and refactoring UI in this project.
> Every section is scoped with **Applies to:** so you know exactly which file the rules govern.
>
> **Note:** This guide is written for React/shadcn-ui but has been adapted for Angular implementation.
> See `.opencode/studies/implementation-notes.md` for Angular-specific details.

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

## 5. Profile, Leaderboard, Admin

> Applies to: `src/pages/ProfilePage.tsx`, `src/pages/LeaderboardPage.tsx`, `src/pages/AdminPage.tsx`.

All use the **wider browser** layout (`max-w-6xl mx-auto px-5 py-8`) with `<TopBar />` on top. Stat tiles, gradient icon tiles, and chips follow the same patterns as the TopBar dropdown (see §2.5).

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
