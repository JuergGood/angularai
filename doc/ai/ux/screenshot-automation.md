# Screenshot Automation with Cypress

To facilitate UX design discussions and documentation, we use Cypress to automatically capture screenshots of all major screens in the GoodOne application.

## 1. Automated Screenshot Script

A dedicated Cypress test file is available at:
`frontend/cypress/e2e/screenshot-automation.cy.ts`

This script:
1. Sets a consistent viewport (1280x800).
2. Navigates through all primary routes (Login, Register, Dashboard, Tasks, Admin, Profile, Help).
3. Authenticates as an admin to access protected pages.
4. Saves screenshots into a timestamped subfolder within `frontend/cypress/screenshots/`.

## 2. How to Run

### Prerequisites
- The backend must be running (`localhost:8080`).
- The frontend must be running (`localhost:4200`).

### Execution Commands

Run the screenshot automation in headless mode:
```bash
cd frontend
npx cypress run --spec cypress/e2e/screenshot-automation.cy.ts
```

Or run via the Cypress UI:
```bash
cd frontend
npx cypress open
```
Then select `screenshot-automation.cy.ts` from the E2E tests list.

## 3. Screenshot Locations

After execution, the screenshots can be found in:
`frontend/cypress/screenshots/screenshot-automation.cy.ts/<timestamp>/`

Each file is named sequentially to reflect the logical flow of the application (e.g., `01-login-page.png`, `04-dashboard.png`).

## 4. Using Screenshots for UX Design

These screenshots are intended to be shared with **ChatGPT** for visual analysis as described in [UX Improvement Strategy](ux-improvement.md).

### Recommended Workflow:
1. Run the screenshot automation.
2. Select the relevant screenshots for the page you want to improve.
3. Upload the screenshots to ChatGPT.
4. Provide the corresponding `.html` and `.css` files.
5. Ask for design proposals and implementation snippets.
6. Pass the proposals to **Junie** for integration.
