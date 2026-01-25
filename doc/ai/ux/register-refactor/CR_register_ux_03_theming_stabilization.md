# CR_register_ux_03_theming_stabilization.md

## Context
You have extensive CSS-variable theming. It works, but dark mode stability suffers when many component-specific overrides stack up.
This CR stabilizes **only the registration + success screens** without a global theming rewrite.

---

## TODO anchor convention
Use:
- `// CR-REG-05:` for theming changes (register + success only)

---

## CR-REG-05 — Registration theming stabilization (light + dark) using existing tokens

### Problem
Dark mode requires too many special-case fixes, making styling fragile.

### Change
- Reduce per-component hacks.
- Make register + success screens rely on the same small set of global tokens.
- Keep Material components consistent with your surface/text/border tokens.

### Files
- Modify:
  - `src/styles.css` (or global theme file where tokens live)
  - `src/app/components/register/register.component.css`
  - `src/app/components/register/register-success.component.css`

### Implementation notes
- Prefer using your existing tokens consistently:
  - background: `var(--bg)`
  - surface/container: `var(--surface)` / `var(--surface-2)`
  - text: `var(--text)`
  - borders: `var(--border)`
  - radius: `var(--r)`
  - shadows: `--shadow-*`
- Avoid:
  - hardcoded colors in component CSS
  - dark-mode-only patches (unless strictly necessary)
- Ensure:
  - form-field outlines/labels remain readable in dark mode
  - buttons have enough contrast
  - success icon/text readable in both modes

### Acceptance criteria
- Register screen looks correct in light + dark (no layout break, readable text).
- Success screen looks correct in light + dark.
- Component CSS contains minimal overrides and uses tokens (no random hex values unless unavoidable).

---

## Smoke tests (for this CR)
- Toggle dark mode: all labels readable, no “washed out” placeholders
- Buttons: text/icon contrast acceptable
- Success icon not too bright/dim in either mode
