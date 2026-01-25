# PR_review_checklist_register_ux.md

Use this checklist to review Junie’s PR quickly and consistently.

---

## Functional flow
- [x] Can complete registration successfully
- [x] On success, navigates to `/register/success`
- [x] Success screen has ONE obvious primary CTA to login
- [ ] Back button does not show confusing pre-filled form state

---

## Reactive forms + validation
- [x] Register uses ReactiveFormsModule (no template-driven remnants)
- [x] Validators are centralized in TS (no duplicated `pattern="..."` in template)
- [x] Errors show inline only after interaction (touched/dirty)
- [x] Password mismatch is a real form error (`passwordMismatch`)

---

## Password UX
- [x] Show/hide toggle works for password + confirm-password
- [ ] Password requirements are visible as helper text before errors
- [ ] Strength indicator only appears after user starts typing
- [ ] Errors are specific and actionable (no generic “invalid input”)

---

## Submit/loading UX
- [ ] Submit button disabled while submitting
- [ ] Spinner appears inside the submit button during submit
- [ ] Double-click cannot trigger double submit

---

## reCAPTCHA UX
- [ ] Visible mode clearly explains why submit is disabled until completed
- [ ] Score mode provides feedback while verifying (no silent wait)
- [ ] reCAPTCHA failure shows calm, human-readable message

---

## Theming (light/dark)
- [ ] Register screen readable and stable in light + dark mode
- [ ] Success screen readable and stable in light + dark mode
- [ ] Minimal hardcoded colors; uses global tokens where possible
- [ ] No scattered dark-mode-only hacks unless justified

---

## Regression sanity
- [ ] No console errors
- [ ] No layout shifts or clipped content at common widths
- [ ] Keyboard-only: tab order makes sense; focus visible

---

## Code hygiene
- [ ] `// CR-REG-xx:` anchors present (easy grep)
- [ ] Dead code removed (old message/timeout behavior gone)
- [ ] No duplicated regex rules for password
