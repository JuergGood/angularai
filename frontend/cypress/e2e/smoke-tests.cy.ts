describe('Smoke Tests - Main Actions', () => {
  const timestamp = Date.now();
  const testUser = `user_${timestamp}`;
  const testEmail = `user_${timestamp}@example.com`;

  beforeEach(() => {
    cy.visit('/login');
    // Ensure we are in English for consistent text matching if possible,
    // but the app defaults to English.
  });

  it('should register a user', () => {
    cy.get('a[routerLink="/register"]').click();
    cy.url().should('include', '/register');

    cy.get('input[name="firstName"]').type('John', { force: true });
    cy.get('input[name="lastName"]').type('Doe', { force: true });
    cy.get('input[name="login"]').type(testUser, { force: true });
    cy.get('#password').type('Password123!', { force: true });
    cy.get('#confirm-password').type('Password123!', { force: true });
    cy.get('input[name="email"]').type(testEmail, { force: true });

    // Using datepicker for birthDate to avoid format issues
    cy.get('mat-datepicker-toggle').click({ force: true });
    cy.get('button.mat-calendar-previous-button').click({ force: true }); // Go back a month
    cy.get('button.mat-calendar-body-cell').contains('1').click({ force: true });

    cy.get('#register-btn').click({ force: true });

    // The app shows success message and redirects
    cy.get('.success', { timeout: 15000 }).should('exist');
    cy.url({ timeout: 15000 }).should('include', '/login');
  });

  it('should login with the new user', () => {
    cy.get('input[name="login"]').type(testUser, { force: true });
    cy.get('input[name="password"]').type('Password123!', { force: true });
    cy.get('#login-btn').click({ force: true });

    // App redirects to /tasks after login for regular users
    cy.url().should('include', '/tasks');
    cy.get('.page-title').should('contain', 'Task Management');
  });

  it('should perform task CRUD operations', () => {
    // Login
    cy.get('input[name="login"]').type(testUser, { force: true });
    cy.get('input[name="password"]').type('Password123!', { force: true });
    cy.get('#login-btn').click({ force: true });

    // Create Task
    cy.get('.add-task-btn').click({ force: true });
    cy.get('input[name="title"]').type('Cypress Task', { force: true });
    cy.get('textarea[name="description"]').type('Task created by Cypress', { force: true });
    cy.get('mat-select[name="priority"]').click({ force: true });
    cy.get('mat-option').contains('High').click({ force: true });
    cy.get('mat-select[name="status"]').click({ force: true });
    cy.get('mat-option').contains('Open').click({ force: true });
    cy.get('button[type="submit"]').click({ force: true });

    // Verify Task
    cy.get('.task-card').should('contain', 'Cypress Task');

    // Edit Task
    cy.get('.task-card').find('button').first().click({ force: true }); // Edit button
    cy.get('input[name="title"]').clear({ force: true }).type('Updated Cypress Task', { force: true });
    cy.get('button[type="submit"]').click({ force: true });
    cy.get('.task-card').should('contain', 'Updated Cypress Task');

    // Delete Task
    cy.get('.task-card').find('button[color="warn"]').click({ force: true });
    cy.get('button').contains('Confirm').click({ force: true });
    cy.get('.task-card').should('not.exist');
  });

  it('should switch language', () => {
    // Guest language switch
    cy.get('button[title="Language"]').click({ force: true });
    cy.get('button').contains('German').click({ force: true });

    // Verify translation (e.g., Login button text)
    cy.get('#login-btn').should('contain', 'Anmelden');

    cy.get('button[title="Sprache"]').click({ force: true });
    cy.get('button').contains('Englisch').click({ force: true });
    cy.get('#login-btn').should('contain', 'Login');
  });

  it('should navigate through all help pages and settings as admin', () => {
    // Login as admin
    cy.get('input[name="login"]').type('admin', { force: true });
    cy.get('input[name="password"]').type('admin123', { force: true });
    cy.get('#login-btn').click({ force: true });

    // Check Dashboard directly
    cy.get('a[routerLink="/dashboard"]').should('be.visible').click({ force: true });
    cy.url().should('include', '/dashboard');
    cy.get('.dashboard-container', { timeout: 30000 }).should('be.visible');

    // Check Settings -> Help -> User Guide
    cy.get('button[title="Settings"]').click({ force: true });
    cy.get('button').contains('Help').click({ force: true });
    cy.get('button').contains('User Guide').click({ force: true });
    cy.url().should('include', '/help/user-guide');
    cy.get('h1').should('contain', 'User Guide');

    // Check Settings -> Help -> Admin Guide (only for admins)
    cy.get('button[title="Settings"]').click({ force: true });
    cy.get('button').contains('Help').click({ force: true });
    cy.get('button').contains('Admin Guide').click({ force: true });
    cy.url().should('include', '/help/admin-guide');
    cy.get('h1').should('contain', 'Admin Guide');

    // Check Settings -> Help -> FAQ
    cy.get('button[title="Settings"]').click({ force: true });
    cy.get('button').contains('Help').click({ force: true });
    cy.get('button').contains('FAQ').click({ force: true });
    cy.url().should('include', '/help/faq');
    cy.get('h1').should('contain', 'FAQ');

    // Check Settings -> About -> Version
    cy.get('button[title="Settings"]').click({ force: true });
    cy.get('button').contains('About').click({ force: true });
    cy.get('.info-label').contains('Version').should('exist');

    // Navigate to Logs
    cy.get('a[routerLink="/logs"]').click({ force: true });
    cy.url().should('include', '/logs');
    cy.get('.page-title').should('contain', 'System Logs');
    cy.get('table').should('exist');
  });
});
