# CR_email_verification_ux.md

## Context
Improve the email verification experience to feel professional, calm, and product-quality.
This applies to a demo application (no legal constraints), but UX quality should match a real product.

Scope includes:
- HTML verification email (EN / DE)
- Verification result pages
- Resend verification email UX

---

## VER-01 — HTML verification email (EN / DE)

### Goal
Replace the current plain-text email with a professional HTML email.

### Tasks
- Implement HTML email templates in:
  - English
  - German
- Keep plain-text fallback if supported by the mail framework
- Use a single primary CTA button

### Content requirements
- Friendly welcome headline
- Short explanation
- Clear “Confirm email address” button
- Raw link fallback
- Reassurance (“ignore if not you”)

### Acceptance criteria
- Email renders correctly in common mail clients
- No external CSS or images required
- Looks professional even in demo environment

---

## VER-02 — Verification result routing

### Goal
Avoid raw backend messages in the browser.

### Tasks
- After email verification:
  - Redirect to `/verify/success` if token is valid
  - Redirect to `/verify/error` if token is invalid or expired
- Do not return plain text or JSON to end users

### Acceptance criteria
- Browser never shows raw backend text
- Verification always ends on a UI page

---

## VER-03 — Verification success page (`/verify/success`)

### Goal
Provide a positive, reassuring end to onboarding.

### UI requirements
- Material Design layout
- Centered card
- Success icon (e.g. `check_circle`)
- Clear visual hierarchy

### Copy (English)
- Headline: **Email confirmed 🎉**
- Text: Your account has been successfully verified. You can now sign in and start using GoodOne.
- Primary CTA: **Go to login**

### Copy (Deutsch)
- Headline: **E-Mail-Adresse bestätigt 🎉**
- Text: Ihr Konto wurde erfolgreich verifiziert. Sie können sich jetzt anmelden und GoodOne nutzen.
- Primary CTA: **Zum Login**

### UX rules
- No auto-redirect
- One primary action only
- No technical wording

---

## VER-04 — Verification error page (`/verify/error`)

### Goal
Handle invalid or expired verification links gracefully.

### UI requirements
- Same layout as success page (visual consistency)
- Error icon (e.g. `error_outline`)

### Copy (English)
- Headline: **Verification failed**
- Text: This verification link is invalid or has expired.
- Primary CTA: **Go to login**
- Secondary CTA: **Resend verification email**

### Copy (Deutsch)
- Headline: **Verifizierung fehlgeschlagen**
- Text: Dieser Bestätigungslink ist ungültig oder abgelaufen.
- Primary CTA: **Zum Login**
- Secondary CTA: **Bestätigungs-E-Mail erneut senden**

---

## VER-05 — Resend verification email UX

### Goal
Allow users to recover from verification errors without frustration.

### Scope
Frontend + backend integration using existing email infrastructure.

### Tasks
1. Show secondary action only on `/verify/error`
2. On click:
   - Trigger backend endpoint to resend verification email
   - Disable button while request is running
3. Show inline feedback

### Feedback copy
**Success**
- EN: Verification email sent. Please check your inbox.
- DE: Bestätigungs-E-Mail wurde erneut gesendet. Bitte prüfen Sie Ihr Postfach.

**Error**
- EN: Failed to send verification email. Please try again later.
- DE: Bestätigungs-E-Mail konnte nicht gesendet werden. Bitte versuchen Sie es später erneut.

### UX rules
- Secondary action (not visually dominant)
- No automatic resend
- No email editing on this page

---

## VER-06 — Visual consistency & theming

### Goal
Ensure verification pages feel part of the application.

### Tasks
- Reuse existing layout/container components
- Respect light and dark mode
- Follow existing spacing and typography rules

### Acceptance criteria
- Success and error pages match app look & feel
- No special-case styling hacks

---

## Out of scope
- Legal copy (privacy, terms)
- Email branding assets (logos, images)
- Multi-factor authentication flows

---

## Final acceptance checklist
- [ ] HTML email renders correctly (EN / DE)
- [ ] Verification never shows raw backend text
- [ ] Success page has one clear primary CTA
- [ ] Error page allows recovery via resend
- [ ] All copy is translated
- [ ] UX feels calm, professional, and intentional
