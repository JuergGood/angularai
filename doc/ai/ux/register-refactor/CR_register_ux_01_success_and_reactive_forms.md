# CR_register_ux_01_success_and_reactive_forms.md

## Context
Refactor the registration flow to feel guided (Material-style), with predictable inline validation and a dedicated success screen.

**Refactoring is allowed** (HTML/TS/CSS). Keep scope to registration + immediate success step.

---

## TODO anchor convention (for traceability)
Add **inline TODO markers** where you implement each requirement so later you can grep the code.

Use this exact pattern:
- `// CR-REG-01:`
- `// CR-REG-02:`

Examples:
- In TS: `// CR-REG-02: reactive form + validators`
- In HTML: `<!-- CR-REG-01: success screen route + CTA -->`
- In CSS: `/* CR-REG-01: success screen spacing */`

---

## CR-REG-01 — Extract Registration Success Screen (standalone component + route)

### Problem
Success is displayed inline under the form, leaving the user without clear closure and a next action.

### Change
1. Create a standalone success component.
2. Add a route `/register/success`.
3. Navigate there upon successful registration.

### Files
- Create:
  - `src/app/components/register/register-success.component.ts`
  - `src/app/components/register/register-success.component.html`
  - `src/app/components/register/register-success.component.css`
- Modify:
  - `src/app/components/register/register.component.ts`
  - `src/app/app.routes.ts`

### Implementation notes
- In `register.component.ts` success callback, replace current inline message + delayed redirect with:
  - `this.router.navigate(['/register/success'], { state: { email: this.form.value.email } });`
- Success screen content:
  - Large success icon
  - Headline: “Account created”
  - One short line: “You can now sign in.” OR “Check your email to verify your account.” (depending on current behavior)
  - **Primary CTA**: “Go to login” → navigates immediately to `/login`
  - Optional **secondary** action as text button ONLY if actually supported (e.g., “Resend email”)

### Acceptance criteria
- After successful registration, user lands on `/register/success` (no form visible).
- Success page has 1 primary CTA and no competing buttons.
- Back navigation doesn’t expose pre-filled form in a confusing state.

---

## CR-REG-02 — Convert Register Form to Reactive Forms + centralize validators

### Problem
Validation is duplicated (template pattern + TS regex + custom conditions), causing UX inconsistencies and future bugs.

### Change
- Convert registration form to reactive forms.
- Keep validators in a single source of truth (FormGroup + custom validators).
- Implement confirm-password as a **real form error** (not a template-only if).

### Files
- Modify:
  - `src/app/components/register/register.component.ts`
  - `src/app/components/register/register.component.html`
  - `src/app/components/register/register.component.css` (only if needed)

### Requirements
- Use `ReactiveFormsModule`
- Define `FormGroup` with:
  - `firstName`, `lastName`, `login`, `email`, `password`, `confirmPassword`
- Validators:
  - required fields → `Validators.required`
  - email → `Validators.email`
  - password strength → **one** regex validator (reuse your current rule, but keep it in TS only)
  - confirm match → group-level validator returns `{ passwordMismatch: true }`

### UX rules for showing errors
- Show field errors only when `touched || dirty`
- Show mismatch error below confirm-password

### Acceptance criteria
- No `pattern="..."` validation in template for password (validator lives in TS).
- Submit button disabled when `form.invalid` (plus reCAPTCHA requirements handled in CR 02/03).
- Confirm-password mismatch is reflected as a form validation error.

---

## Smoke tests (for this CR)
- Empty required fields show errors only after interaction
- Invalid email shows a clear inline error
- Password mismatch shows inline error under confirm field
- Successful registration routes to success screen
