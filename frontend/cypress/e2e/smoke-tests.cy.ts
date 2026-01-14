describe('Smoke Tests - Main Actions', () => {
  const timestamp = Date.now();
  const testUser = `user_${timestamp}`;
  const testEmail = `user_${timestamp}@example.com`;

  beforeEach(() => {
    cy.visit('/login');
    // Ensure we are in English for consistent text matching if possible,
    // but the app defaults to English.
  });

  it('should register a user without birthdate', () => {
    cy.get('a[routerLink="/register"]').click();
    cy.url().should('include', '/register');

    cy.get('input[name="firstName"]').type('John');
    cy.get('input[name="lastName"]').type('Doe');
    cy.get('input[name="login"]').type(testUser);
    cy.get('#password').type('Password123!');
    cy.get('#confirm-password').type('Password123!');
    cy.get('input[name="email"]').type(testEmail);

    // Birthdate is not in the form, so we just register
    cy.get('#register-btn').click();

    // The app shows success message and redirects
    cy.get('.success', { timeout: 10000 }).should('be.visible');
    cy.url({ timeout: 10000 }).should('include', '/login');
  });

  it('should login with the new user', () => {
    cy.get('input[name="login"]').type(testUser);
    cy.get('input[name="password"]').type('Password123!');
    cy.get('#login-btn').click();

    // App redirects to /tasks after login
    cy.url().should('include', '/tasks');
    cy.get('.page-title').should('contain', 'Task Management');
  });

  it('should perform task CRUD operations', () => {
    // Login
    cy.get('input[name="login"]').type(testUser);
    cy.get('input[name="password"]').type('Password123!');
    cy.get('#login-btn').click();

    // Create Task
    cy.get('.add-task-btn').click();
    cy.get('input[name="title"]').type('Cypress Task');
    cy.get('textarea[name="description"]').type('Task created by Cypress');
    cy.get('mat-select[name="priority"]').click();
    cy.get('mat-option').contains('High').click();
    cy.get('mat-select[name="status"]').click();
    cy.get('mat-option').contains('Open').click();
    cy.get('button[type="submit"]').click();

    // Verify Task
    cy.get('.task-card').should('contain', 'Cypress Task');

    // Edit Task
    cy.get('.task-card').find('button').first().click(); // Edit button
    cy.get('input[name="title"]').clear().type('Updated Cypress Task');
    cy.get('button[type="submit"]').click();
    cy.get('.task-card').should('contain', 'Updated Cypress Task');

    // Delete Task
    cy.get('.task-card').find('button[color="warn"]').click();
    cy.get('button').contains('Confirm').click();
    cy.get('.task-card').should('not.exist');
  });

  it('should switch language', () => {
    // Guest language switch
    cy.get('button[title="Language"]').click();
    cy.get('button').contains('German').click();

    // Verify translation (e.g., Login button text)
    cy.get('#login-btn').should('contain', 'Anmelden');

    cy.get('button[title="Sprache"]').click();
    cy.get('button').contains('Englisch').click();
    cy.get('#login-btn').should('contain', 'Login');
  });

  it('should navigate through all help pages and settings as admin', () => {
    // Login as admin
    cy.get('input[name="login"]').type('admin');
    cy.get('input[name="password"]').type('admin123');
    cy.get('#login-btn').click();

    // Check Dashboard directly
    cy.get('a[routerLink="/dashboard"]').should('be.visible').click();
    cy.url().should('include', '/dashboard');
    cy.get('.dashboard-container', { timeout: 30000 }).should('be.visible');

    // Check Settings -> Help -> User Guide
    cy.get('button[title="Settings"]').click();
    cy.get('button').contains('Help').click();
    cy.get('button').contains('User Guide').click();
    cy.url().should('include', '/help/user-guide');
    cy.get('h1').should('contain', 'User Guide');

    // Check Settings -> Help -> Admin Guide (only for admins)
    cy.get('button[title="Settings"]').click();
    cy.get('button').contains('Help').click();
    cy.get('button').contains('Admin Guide').click();
    cy.url().should('include', '/help/admin-guide');
    cy.get('h1').should('contain', 'Admin Guide');

    // Check Settings -> Help -> FAQ
    cy.get('button[title="Settings"]').click();
    cy.get('button').contains('Help').click();
    cy.get('button').contains('FAQ').click();
    cy.url().should('include', '/help/faq');
    cy.get('h1').should('contain', 'FAQ');

    // Check Settings -> About -> Version
    cy.get('button[title="Settings"]').click();
    cy.get('button').contains('About').click();
    cy.get('button').contains('Version').should('exist');

    // Navigate to Logs
    cy.get('a[routerLink="/logs"]').click({ force: true });
    cy.url().should('include', '/logs');
    cy.get('.page-title').should('contain', 'System Logs');
    cy.get('table').should('exist');
  });
});
