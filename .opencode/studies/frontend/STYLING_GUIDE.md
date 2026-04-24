# SQLab Styling Guide

A complete walkthrough of how to recreate the visual system used across this project — colors, typography, Tailwind setup, semantic tokens, and reusable HTML/JSX patterns. Follow this top-to-bottom and you'll be able to reproduce the look of any page in the app.

---

## 1. Design Philosophy

The app is a **gamified, dark-mode SQL training console**. The aesthetic blends:

- **Cyber/terminal feel** — dark slate-blue background, neon-teal accents, monospace numerics.
- **Editorial gamification** — gradient avatars, soft glows, level badges, XP bars.
- **Density with breathing room** — small font sizes (`text-xs`, `text-[9px]`), tight padding, but generous use of `rounded-xl` corners and subtle borders.

**Rule #1:** Never write raw color classes like `text-white`, `bg-black`, `bg-slate-900`. Always use semantic tokens (`bg-background`, `text-foreground`, `text-primary`, …). The whole system depends on this.

---

## 2. Color System (HSL Tokens)

All colors live in `src/index.css` as CSS variables in **HSL space** (no `hsl()` wrapper, just the three numbers). This lets Tailwind compose opacity via `/` syntax (e.g. `bg-primary/10`).

### 2.1 Core palette

```css
/* src/index.css */
:root {
  --background: 230 25% 9%;     /* deep slate-blue, near black */
  --foreground: 220 20% 92%;    /* off-white text */

  --card: 230 22% 12%;          /* slightly lifted surface */
  --card-foreground: 220 20% 92%;

  --popover: 230 22% 12%;
  --popover-foreground: 220 20% 92%;

  --primary: 165 80% 48%;       /* neon teal — main brand */
  --primary-foreground: 230 25% 9%;

  --secondary: 250 60% 62%;     /* violet */
  --secondary-foreground: 0 0% 100%;

  --muted: 230 18% 16%;         /* subtle panel bg */
  --muted-foreground: 220 15% 55%;

  --accent: 35 95% 60%;         /* warm orange/gold */
  --accent-foreground: 230 25% 9%;

  --destructive: 0 72% 55%;     /* red */
  --destructive-foreground: 0 0% 100%;

  --border: 230 15% 20%;
  --input: 230 18% 16%;
  --ring: 165 80% 48%;          /* focus ring = primary */

  --radius: 0.75rem;            /* default rounded-lg */

  --success: 165 80% 48%;
  --editor-bg: 230 25% 7%;      /* even darker for code */
  --surface-raised: 230 20% 14%;
}
```

### 2.2 Semantic intent

| Token         | Use for                                            |
|---------------|----------------------------------------------------|
| `background`  | Page body                                          |
| `foreground`  | Default text                                       |
| `card`        | Panels, dropdowns, dialogs                         |
| `muted`       | Inactive surfaces, subtle row backgrounds          |
| `muted-foreground` | Secondary/meta text (labels, timestamps)      |
| `primary`     | Brand actions, success, XP bar fill                |
| `secondary`   | Alternate gradient stop, secondary stats           |
| `accent`      | Highlights, level badges, gold/orange callouts     |
| `destructive` | Errors, sign-out, delete actions                   |
| `border`      | All hairline borders                               |
| `ring`        | Focus outlines (handled by shadcn)                 |

### 2.3 Wiring tokens to Tailwind

In `tailwind.config.ts`, every token is exposed as a Tailwind color so you can write `bg-primary`, `text-accent`, etc.:

```ts
colors: {
  border: "hsl(var(--border))",
  background: "hsl(var(--background))",
  foreground: "hsl(var(--foreground))",
  primary: {
    DEFAULT: "hsl(var(--primary))",
    foreground: "hsl(var(--primary-foreground))",
  },
  // ...same shape for secondary, destructive, muted, accent, popover, card
  success: "hsl(var(--success))",
  editor: "hsl(var(--editor-bg))",
  "surface-raised": "hsl(var(--surface-raised))",
}
```

**Why HSL bare numbers?** Because Tailwind generates `hsl(var(--primary) / <alpha-value>)` — letting you do `bg-primary/10`, `border-primary/30`, `glow-primary`, etc.

---

## 3. Typography

Two Google fonts loaded at the top of `src/index.css`:

```css
@import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap');
```

Mapped in `tailwind.config.ts`:

```ts
fontFamily: {
  sans: ["'Space Grotesk'", "sans-serif"],   // headings, UI labels
  mono: ["'JetBrains Mono'", "monospace"],   // numbers, meta, code, hints
}
```

### Conventions

- **`font-sans`** — titles (`text-2xl font-bold tracking-tight`), button text, usernames.
- **`font-mono`** — anything numeric (XP, level, counts), uppercase labels, timestamps, hint text. Often paired with extra-small sizes like `text-[9px]` or `text-[10px]`.
- Use `tracking-tight` on display headings, default tracking on body.
- Common body sizes: `text-xs` (12px) and `text-sm` (14px) dominate. Pages title at `text-2xl`.

---

## 4. Global Base Layer

```css
@layer base {
  * { @apply border-border; }                /* default border color */
  body { @apply bg-background text-foreground font-sans antialiased; }
  ::selection { background: hsl(165 80% 48% / 0.3); }  /* teal selection */
}
```

This is what makes `<div className="border" />` automatically render in the brand border color.

---

## 5. Custom Utilities (Glows & Mesh)

Defined in `@layer utilities` of `src/index.css`. These are the "premium" touches:

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

Use `.gradient-mesh` on full-page wrappers for the soft purple/teal/gold ambient glow. Use `.glow-primary` on pinned cards or active state badges.

---

## 6. Animation Tokens

Defined in `tailwind.config.ts` keyframes/animation. Use sparingly — entry animations only.

```ts
keyframes: {
  "fade-in":   { "0%": { opacity:"0", transform:"translateY(10px)" }, "100%": { opacity:"1", transform:"translateY(0)" } },
  "scale-in":  { "0%": { transform:"scale(0.95)", opacity:"0" }, "100%": { transform:"scale(1)", opacity:"1" } },
  "pulse-glow":{ "0%,100%": { opacity:"1" }, "50%": { opacity:"0.7" } },
  "row-appear":{ from: { opacity:"0", transform:"translateY(4px)" }, to: { opacity:"1", transform:"translateY(0)" } },
},
animation: {
  "fade-in":   "fade-in 0.4s ease-out",
  "scale-in":  "scale-in 0.3s ease-out",
  "pulse-glow":"pulse-glow 2s ease-in-out infinite",
  "row-appear":"row-appear 0.2s ease-out forwards",
}
```

For richer page-level motion, the project also uses **`framer-motion`** with this pattern:

```tsx
<motion.div initial={{ opacity: 0, y: -8 }} animate={{ opacity: 1, y: 0 }}>
```

Stagger lists with `transition={{ delay: i * 0.05 }}`.

---

## 7. The Building-Block Patterns

These are the recurring HTML/JSX recipes used across pages. Copy them verbatim.

### 7.1 Page shell

```tsx
<div className="min-h-screen bg-background flex flex-col">
  <TopBar />
  <main className="flex-1 px-6 py-8 max-w-5xl mx-auto w-full">
    {/* content */}
  </main>
</div>
```

### 7.2 Section header with gradient icon tile

```tsx
<div className="flex items-center gap-3 mb-2">
  <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-accent to-primary
                  flex items-center justify-center">
    <Trophy className="w-5 h-5 text-accent-foreground" />
  </div>
  <div>
    <h1 className="font-sans text-2xl font-bold tracking-tight">Leaderboard</h1>
    <p className="font-mono text-xs text-muted-foreground">Subtitle meta line</p>
  </div>
</div>
```

The gradient tile (`bg-gradient-to-br from-X to-Y`) is the signature decoration. Common pairs:

- `from-primary to-secondary` — default brand
- `from-accent to-primary` — gold→teal (leaderboard, achievements)
- `from-secondary to-primary` — violet→teal (profile)
- `from-destructive to-accent` — red→gold (warnings, hero)

### 7.3 Avatar with level badge

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

### 7.4 XP / progress bar

```tsx
<div className="flex-1 h-1.5 rounded-full bg-muted overflow-hidden">
  <motion.div
    className="h-full rounded-full bg-gradient-to-r from-primary to-accent"
    initial={{ width: 0 }}
    animate={{ width: `${(value / max) * 100}%` }}
    transition={{ duration: 0.6 }}
  />
</div>
```

### 7.5 Stat tile (3-column grid)

```tsx
<div className="grid grid-cols-3 gap-2">
  <div className="text-center rounded-lg bg-muted/50 py-2">
    <div className="flex items-center justify-center gap-1 text-primary">
      <Trophy className="w-3 h-3" />
      <span className="font-mono text-xs font-semibold">{count}</span>
    </div>
    <p className="font-mono text-[8px] text-muted-foreground mt-0.5">Solved</p>
  </div>
  {/* repeat with text-accent / text-secondary for variety */}
</div>
```

### 7.6 Card / panel

```tsx
<div className="rounded-xl border border-border bg-card p-4">
  {/* content */}
</div>
```

For an interactive card add `hover:bg-muted/30 transition-colors` and optionally `card-shine`.

### 7.7 Top bar (sticky header)

```tsx
<header className="px-5 py-3 flex items-center justify-between
                   border-b border-border bg-background/80 backdrop-blur-sm shrink-0">
```

The `bg-background/80 backdrop-blur-sm` combo is the standard "glass" header.

### 7.8 Primary button (inline, not shadcn)

```tsx
<Link className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg
                 bg-primary text-primary-foreground hover:bg-primary/90
                 font-mono text-xs font-medium transition-colors">
  <LogIn className="w-3.5 h-3.5" /> Sign in
</Link>
```

### 7.9 Destructive ghost button

```tsx
<button className="... hover:bg-destructive/10 hover:text-destructive transition-colors">
  <LogOut className="w-3.5 h-3.5 text-destructive" /> Sign out
</button>
```

---

## 8. Spacing, Sizing & Radii

- **Radii:** `rounded-lg` (12px-ish via `--radius`) is the default. `rounded-xl` for icon tiles and cards. `rounded-full` for pills and progress bars.
- **Padding scale:** Pages use `px-6 py-8`. Cards use `p-4`. Compact elements use `px-3 py-1.5` or `px-2 py-1.5`.
- **Gaps:** `gap-2` / `gap-2.5` / `gap-3` dominate. Use `gap-1.5` inside buttons.
- **Icon sizing:** `w-3 h-3` (inline meta), `w-3.5 h-3.5` (dropdown items), `w-4 h-4` (buttons), `w-5 h-5` (section icons).

---

## 9. Iconography

Always use **`lucide-react`**. Pair icon color with the surrounding text token:

```tsx
<Trophy className="w-3 h-3 text-primary" />
<Star   className="w-3 h-3 text-accent" />
<Flame  className="w-3 h-3 text-secondary" />
```

Don't mix icon libraries. Keep stroke widths default.

---

## 10. shadcn/ui Components

Components live in `src/components/ui/*` and are styled exclusively with semantic tokens. When customizing:

1. **Don't hardcode colors** in the component — extend `cva` variants.
2. Add new variants like `premium` using gradients of design tokens:

```tsx
variant: {
  premium: "bg-gradient-to-r from-primary to-accent text-primary-foreground hover:opacity-90",
}
```

3. Always include focus styles: `focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2`.

---

## 11. Light-mode / Dark-mode

This project is **dark-only** (no `.dark` class toggle). All tokens are defined under `:root`. If you ever add a light theme, override the same tokens inside `.light { ... }` and wrap the app accordingly. Do **not** branch component code on theme — always go through tokens.

---

## 12. Checklist Before Committing UI

- [ ] No raw color classes (`text-white`, `bg-slate-X`, `text-red-500`, hex codes).
- [ ] Numbers/meta use `font-mono`; titles use `font-sans`.
- [ ] All borders use `border-border` (or omitted — base layer handles it).
- [ ] Surfaces use `bg-card` / `bg-muted` / `bg-background` only.
- [ ] Interactive elements have `transition-colors` and a `hover:` state.
- [ ] Icon tiles use a `bg-gradient-to-br from-X to-Y` from the approved pairs.
- [ ] Animations are entry-only (`fade-in`, `scale-in`) or `framer-motion` with subtle `y: -8` / `opacity: 0` start.
- [ ] Focus-visible ring is preserved on all interactive elements.

---

## 13. TL;DR Cheat Sheet

```text
Page wrapper:    min-h-screen bg-background flex flex-col
Container:       max-w-5xl mx-auto w-full px-6 py-8
Card:            rounded-xl border border-border bg-card p-4
Header glass:    bg-background/80 backdrop-blur-sm border-b border-border
Title:           font-sans text-2xl font-bold tracking-tight
Meta:            font-mono text-xs text-muted-foreground
Primary CTA:     bg-primary text-primary-foreground hover:bg-primary/90
Icon tile:       w-10 h-10 rounded-xl bg-gradient-to-br from-accent to-primary
Progress fill:   bg-gradient-to-r from-primary to-accent
Stat surface:    bg-muted/50 rounded-lg
```

Master these patterns and the rest of the codebase will feel like filling in a coloring book.
