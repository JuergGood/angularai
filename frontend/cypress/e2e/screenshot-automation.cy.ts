describe('Screenshot Automation', () => {
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-');

  const takeScreenshot = (name: string) => {
    // Wait for any animations or loading to settle
    cy.wait(1000);
    cy.screenshot(`${timestamp}/${name}`);
  };

  beforeEach(() => {
    // Set a standard viewport for documentation screenshots
    cy.viewport(1280, 800);
  });

  it('should capture screenshots of all major screens', () => {
    // 1. Login Page
    cy.visit('/login');
    takeScreenshot('01-login-page');

    // 2. Register Page
    cy.get('a[routerLink="/register"]').click();
    cy.url().should('include', '/register');
    takeScreenshot('02-register-page');

    // 3. Login as Admin to access all pages
    cy.visit('/login');
    cy.get('input[name="login"]').type('admin', { force: true });
    cy.get('input[name="password"]').type('admin123', { force: true });
    cy.get('#login-btn').click({ force: true });
    cy.url().should('include', '/tasks'); // Default redirect

    // 4. Tasks Page
    takeScreenshot('03-tasks-page');

    // 5. Dashboard
    cy.get('a[routerLink="/dashboard"]').click({ force: true });
    cy.url().should('include', '/dashboard');
    takeScreenshot('04-dashboard');

    // 6. User Administration (Admin only)
    cy.get('a[routerLink="/user-admin"]').click({ force: true });
    cy.url().should('include', '/user-admin');
    takeScreenshot('05-user-admin');

    // 7. System Logs (Admin only)
    cy.get('a[routerLink="/logs"]').click({ force: true });
    cy.url().should('include', '/logs');
    takeScreenshot('06-system-logs');

    // 8. Profile Page
    cy.get('a[routerLink="/profile"]').click({ force: true });
    cy.url().should('include', '/profile');
    takeScreenshot('07-profile-page');

    // 9. Help - User Guide
    cy.get('button[title="Settings"], button[title="Einstellungen"]').click({ force: true });
    cy.get('button').contains(/Help|Hilfe/).click({ force: true });
    cy.get('button').contains(/User Guide|Benutzerhandbuch/).click({ force: true });
    cy.url().should('include', '/help/user-guide');
    takeScreenshot('08-help-user-guide');

    // 10. Help - Admin Guide
    cy.get('button[title="Settings"], button[title="Einstellungen"]').click({ force: true });
    cy.get('button').contains(/Help|Hilfe/).click({ force: true });
    cy.get('button').contains(/Admin Guide|Administrator-Handbuch/).click({ force: true });
    cy.url().should('include', '/help/admin-guide');
    takeScreenshot('09-help-admin-guide');

    // 11. Help - FAQ
    cy.get('button[title="Settings"], button[title="Einstellungen"]').click({ force: true });
    cy.get('button').contains(/Help|Hilfe/).click({ force: true });
    cy.get('button').contains(/FAQ/).click({ force: true });
    cy.url().should('include', '/help/faq');
    takeScreenshot('10-help-faq');

    // 12. About Dialog
    cy.get('button[title="Settings"], button[title="Einstellungen"]').click({ force: true });
    cy.get('button').contains(/About|Über/).click({ force: true });
    cy.get('mat-dialog-container').should('be.visible');
    takeScreenshot('11-about-dialog');
  });
});
