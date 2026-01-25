# Form UX Review Checklist (Angular Material)

Use this checklist when reviewing PRs or Junie-generated code.

---

## Structure
- [ ] Every field uses `<mat-form-field>`
- [ ] Labels are always visible (no placeholder-only labels)
- [ ] Field structure order is respected

---

## Errors
- [ ] Errors appear **below** the field
- [ ] `mat-error` used consistently
- [ ] Errors shown only after interaction
- [ ] Only one error visible per field

---

## Helper Text
- [ ] Helper text is neutral (not red)
- [ ] Helper text explains expectations
- [ ] Helper text is placed below the field

---

## Password UX
- [ ] Eye toggle exists for password fields
- [ ] Eye toggle is inside the field (right aligned)
- [ ] Password strength is shown **below** the field
- [ ] Strength is not shown for empty input

---

## Icons
- [ ] No icon clustering on the right side of inputs
- [ ] Control icons and feedback icons are clearly separated

---

## Buttons & Submit
- [ ] Single primary action per screen
- [ ] Submit disabled when form is invalid
- [ ] Loading spinner visible during submit
- [ ] Double-submit impossible

---

## Validation Logic
- [ ] ReactiveFormsModule used
- [ ] Validators live in TS (single source of truth)
- [ ] Cross-field validation uses group validators

---

## Accessibility
- [ ] Keyboard navigation works end-to-end
- [ ] Focus state visible
- [ ] Error state understandable without color

---

## Final Sanity Check
- [ ] No mixed UX patterns
- [ ] No visual “error flooding”
- [ ] UI feels calm, predictable, and guided
