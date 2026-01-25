# PR_review_checklist_register_ux.md

Use this checklist to review Junie’s PR quickly and consistently.

---

## Functional flow
- [x] Can complete registration successfully
- [x] On success, navigates to `/register/success`
- [x] Success screen has ONE obvious primary CTA to login
- [x] Back button does not show confusing pre-filled form state

---

## Reactive forms + validation
- [x] Register uses ReactiveFormsModule (no template-driven remnants)
- [x] Validators are centralized in TS (no duplicated `pattern="..."` in template)
- [x] Errors show inline only after interaction (touched/dirty)
- [x] Password mismatch is a real form error (`passwordMismatch`)

---

## Password UX
- [x] Show/hide toggle works for password + confirm-password
- [x] Password requirements are visible as helper text before errors
- [x] Strength indicator only appears after user starts typing
- [x] Errors are specific and actionable (no generic “invalid input”)

---

## Submit/loading UX
- [x] Submit button disabled while submitting
- [x] Spinner appears inside the submit button during submit
- [x] Double-click cannot trigger double submit

---

## reCAPTCHA UX
- [x] Visible mode clearly explains why submit is disabled until completed
- [x] Score mode provides feedback while verifying (no silent wait)
- [x] reCAPTCHA failure shows calm, human-readable message

---

## Theming (light/dark)
- [x] Register screen readable and stable in light + dark mode
- [x] Success screen readable and stable in light + dark mode
- [x] Minimal hardcoded colors; uses global tokens where possible
- [x] No scattered dark-mode-only hacks unless justified

---

## Regression sanity
- [x] No console errors
- [x] No layout shifts or clipped content at common widths
- [x] Keyboard-only: tab order makes sense; focus visible

---

## Code hygiene
- [x] `// CR-REG-xx:` anchors present (easy grep)
- [x] Dead code removed (old message/timeout behavior gone)
- [x] No duplicated regex rules for password
