# Angular Material Form UX – Reference & Patterns

This document defines **consistent, predictable UX rules** for Angular Material forms.
Use it as a reference when implementing or reviewing forms (registration, login, profile, etc.).

---

## 1. Core Principles

- The **input field shows state**, not explanations
- **Explanatory text always lives below the field**
- Users should never wonder:
  - *Why is this red?*
  - *What do I need to fix?*
  - *What happens next?*

---

## 2. Field Structure (Mandatory Order)

Every form field must follow this structure:

1. `<mat-form-field>`
2. `<mat-label>`
3. `<input matInput | textarea matInput>`
4. Optional: `matSuffix` (controls only, e.g. eye toggle)
5. Optional: `<mat-hint>` (helper text, strength, explanation)
6. `<mat-error>` (validation messages)

---

## 3. Errors

- Errors **always appear below the field**
- Use `mat-error` only
- Never place error text inside the input field
- Show errors only when:
  - `control.touched || control.dirty`
- Show **one error at a time** (most relevant first)

Priority example:
1. `required`
2. format / strength
3. cross-field mismatch

---

## 4. Helper Text

- Helper text explains expectations
- Helper text is neutral (not red)
- Helper text may be always visible
- Helper text must not look like an error

Typical examples:
- Password rules
- Email format hint
- “We’ll never share your email”

---

## 5. Password Fields

### Required behavior
- Password visibility toggle (eye icon) inside the field, right aligned
- Password strength + rules below the field

### Forbidden
- Strength indicators next to the eye icon
- Error text inside the input field
- Multiple icons competing for attention

---

## 6. Icons

- Icons **inside the field** = controls (clickable)
- Icons **below the field** = feedback (read-only)
- Never mix control and feedback icons together

---

## 7. Buttons & Submission

- One primary action per screen
- Submit disabled when:
  - form is invalid
  - submission is in progress
- Show loading spinner inside submit button
- Prevent double submission

---

## 8. Validation Logic

- Use **Reactive Forms**
- Validators defined in TS only
- No duplicated validation logic in templates
- Cross-field rules implemented as group validators

---

## 9. Accessibility Rules

- Inputs >= 48px height
- Focus visible and logical
- Error state not communicated by color alone
- Keyboard-only usage must work

---

## 10. Global Rule

> If the user hesitates after reading a field, the UX already failed.
