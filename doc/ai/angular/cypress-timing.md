# Cypress Testing: Timing and Stability Learnings

This document summarizes the key learnings and best practices derived from fixing flakiness and timing issues in the Cypress end-to-end test suite, specifically regarding the `DashboardComponent` and `DemoComponent`.

## 1. Timing and Stability

### Avoid `cy.reload()`
Reloading the page in a headless environment often leads to race conditions, especially when combined with API intercepts. 
- **Problem**: `cy.reload()` might complete before the interceptors are fully re-attached or the application might miss the initial "me" request if the reload happens too fast.
- **Solution**: Instead of reloading to verify state changes, perform direct UI assertions or navigate away and back. If verification is needed, ensure the previous action (like a `PUT` or `POST`) has finished using `cy.wait('@alias')`.

### Wait for API Responses
Always use `cy.intercept` with aliases and `cy.wait` for non-deterministic operations.
- **Best Practice**:
  ```javascript
  cy.intercept('PUT', '/api/users/me').as('updateUser');
  cy.get('[data-cy=save-btn]').click();
  cy.wait('@updateUser');
  // Now verify UI changes
  ```

## 2. Authentication Mocking

### Clean State for Each Test
State pollution between tests (via `localStorage`) is a common cause of "redirect loops" or unexpected "Unauthorized" errors.
- **Learning**: Each `it` block should be independent. Start with `localStorage.clear()` or rely on Cypress's automatic clear between tests (if configured).
- **Refactoring**: Move from a monolithic "lifecycle" test to small, independent tests that each perform their own login.

### Robust Login Helpers
A `loginAsAdmin` helper shouldn't just check the URL. It should wait for a "UI anchor" that signifies the app is fully initialized.
- **Anchor example**: `cy.get('[data-cy=profile-button]', { timeout: 10000 }).should('be.visible');`
- This ensures that the `AuthService` has finished its `init()` cycle and the session is truly active before the test tries to interact with other components.

## 3. UI Robustness

### Stable Selectors (`data-cy`)
Relying on CSS classes (`.mat-card-title`) or structural tags (`h2`) makes tests brittle to UI redesigns.
- **Action**: Add `data-cy` attributes to critical interactive elements (buttons, summary cards, form fields).
- **Example**: `<mat-card data-cy="dashboard-open-tasks">`

### Handle Loading States
Components that fetch data on `ngOnInit` can cause `TypeError` in tests if the template tries to access nested properties before the signal/observable emits.
- **Solution**: Use safe navigation (`?.`), nullish coalescing (`??`), and explicit `*ngIf` checks for data containers.
- **Cypress Learning**: If a test fails with "item is not visible" because it was hidden by an `*ngIf` during loading, use a longer timeout or explicitly wait for the loading indicator to disappear.

## 4. Test Isolation

### Global vs. Local Intercepts
- **Global**: Move common intercepts (like `GET /api/users/me` or `GET /api/dashboard`) to a `beforeEach` block to ensure consistency.
- **Local**: Keep specific mocks (like a failing `POST` or a specific `PUT` payload) within the `it` block to avoid side effects on other tests.

## 5. Environment Differences
Tests that pass in `cypress open` (headed) might fail in `cypress run` (headless/Electron) due to:
- Rendering speed (headless is faster).
- Viewport size differences (causing sidebar elements to be hidden).
- **Solution**: Use `{ force: true }` sparingly for clicks if elements are covered by overlays, but prefer ensuring the viewport is large enough (`viewportWidth: 1280` in `cypress.config.js`).
