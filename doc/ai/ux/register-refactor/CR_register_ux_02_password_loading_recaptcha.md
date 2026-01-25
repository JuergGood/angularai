# CR_register_ux_02_password_loading_recaptcha.md

## Context
Improve the “feel” of registration: password usability, predictable submit behavior, and transparent reCAPTCHA behavior.

---

## TODO anchor convention
Use:
- `// CR-REG-03:` for password UX changes
- `// CR-REG-04:` for reCAPTCHA changes
- `// CR-REG-06:` for submit/loading state changes (introduced here)

---

## CR-REG-03 — Password UX: show/hide + clear rules + calm strength feedback

### Problem
Users retry passwords when requirements are unclear. Missing show/hide toggle increases friction.

### Change
- Add show/hide toggles for password and confirm-password.
- Display password rules as helper text **before** errors.
- Keep strength indicator helpful (not alarming).

### Files
- Modify:
  - `src/app/components/register/register.component.ts`
  - `src/app/components/register/register.component.html`
  - `src/app/components/register/register.component.css`

### Implementation notes
- Add booleans: `passwordVisible`, `confirmPasswordVisible`
- Add `matSuffix` icon button with `visibility / visibility_off`
- Strength indicator:
  - Show only after user typed at least 1 char in password
  - If invalid, show “Password requirements not met” (not “Weak” in angry red)

### Acceptance criteria
- Both password inputs have show/hide toggles.
- Helper text with requirements is visible even before errors.
- Strength indicator doesn’t appear for empty password.

---

## CR-REG-06 — Submit/loading behavior (fast win)

### Problem
When the user clicks submit, the UI may feel unresponsive or allow double submissions.

### Change
- Add explicit loading state during register.
- Disable submit button while submitting.
- Show spinner inside submit button.

### Files
- Modify:
  - `src/app/components/register/register.component.ts`
  - `src/app/components/register/register.component.html`

### Implementation notes
- Add `isSubmitting = false`
- On submit start: set `isSubmitting = true`
- On success/error finalize: set `isSubmitting = false`
- Button content: switch label → spinner while `isSubmitting`

### Acceptance criteria
- Submit button disabled while submitting.
- Spinner visible inside button during submission.
- Double-submit not possible.

---

## CR-REG-04 — reCAPTCHA: explicit state + clear UX (score-based vs visible)

### Problem
reCAPTCHA feels random when the user can’t see what’s happening (score-based) or why the button is disabled (visible).

### Change
Track a simple state machine and reflect it in the UI.

### Files
- Modify:
  - `src/app/components/register/register.component.ts`
  - `src/app/components/register/register.component.html`

### Implementation notes
Introduce:
- `recaptchaMode: 'disabled' | 'score' | 'visible'`
- `recaptchaStatus: 'idle' | 'verifying' | 'verified' | 'expired' | 'error'`

Behavior:
- Visible mode: require token before enabling submit.
- Score mode: allow click; on click trigger execute, set status to `verifying`, show button spinner.
- If token fails: show calm inline error near the action area (“Verification failed, please try again.”)

### Acceptance criteria
- User can always infer why they can’t proceed.
- Score mode shows “something is happening” feedback.
- No silent failure; errors are human-readable.

---

## Smoke tests (for this CR)
- Toggle password visibility on both fields
- Submit shows spinner and disables button
- Visible reCAPTCHA blocks submit until completed
- Score reCAPTCHA gives feedback while verifying
