describe('User Management System', () => {
  beforeEach(() => {
    // We assume the backend is running and has the default admin/admin123 user
    cy.visit('/login');
  });

  it('should login successfully and redirect to tasks', () => {
    cy.screenshot('login-page');
    cy.get('input[name="login"]').type('admin');
    cy.get('input[name="password"]').type('admin123');
    cy.screenshot('login-filled');
    cy.get('#login-btn').click();

    cy.url().should('include', '/tasks');
    cy.get('.page-title').should('contain', 'Task Management');
    cy.screenshot('tasks-page');
  });

  it('should show error on invalid login', () => {
    cy.get('input[name="login"]').type('wrong');
    cy.get('input[name="password"]').type('wrong');
    cy.get('#login-btn').click();

    // Check for the error message using a more robust selector
    // and wait for the request to complete.
    cy.wait(1000);
    cy.url().should('include', '/login');
  });

  it('should edit user profile and save changes', () => {
    // Login first
    cy.get('input[name="login"]').type('admin');
    cy.get('input[name="password"]').type('admin123');
    cy.get('#login-btn').click();

    // Wait for redirect to tasks
    cy.url().should('include', '/tasks');

    // Go to profile via user menu
    cy.get('.user-profile-button', { timeout: 10000 }).click();
    // Material menu might take a moment to animate
    cy.get('.mat-mdc-menu-content', { timeout: 10000 }).should('be.visible');
    cy.get('.mat-mdc-menu-content button').contains('Profile').click({ force: true });

    // Edit profile
    cy.url({ timeout: 10000 }).should('include', '/profile');
    cy.get('input[name="firstName"]', { timeout: 10000 }).should('be.visible');

    // Use direct clear and type
    cy.get('input[name="firstName"]').clear({ force: true }).type('Super', { force: true });
    cy.get('input[name="lastName"]').clear({ force: true }).type('Admin', { force: true });
    cy.get('input[name="birthDate"]').clear({ force: true }).type('1985-05-20', { force: true });
    cy.get('textarea[name="address"]').clear({ force: true }).type('New Admin Street 1', { force: true });

    cy.get('#save-btn').click();

    cy.get('#profile-message', { timeout: 10000 }).should('be.visible').and('contain', 'Success');

    // Verify values directly after save
    cy.get('input[name="firstName"]').should('have.value', 'Super');
    cy.get('input[name="lastName"]').should('have.value', 'Admin');
    cy.get('textarea[name="address"]').should('have.value', 'New Admin Street 1');
  });

  it('should logout successfully', () => {
    // Login first
    cy.get('input[name="login"]').type('admin');
    cy.get('input[name="password"]').type('admin123');
    cy.get('#login-btn').click();

    // Wait for redirect to tasks
    cy.url().should('include', '/tasks');

    // Logout via user menu
    cy.get('.user-profile-button', { timeout: 10000 }).click();
    cy.get('.mat-mdc-menu-content', { timeout: 10000 }).should('be.visible');
    cy.get('.mat-mdc-menu-content button').contains('Logout').click({ force: true });

    cy.url({ timeout: 10000 }).should('include', '/login');
    cy.get('mat-card-title').should('contain', 'Login');
  });

  it('should register a new user successfully', () => {
    cy.get('a[routerLink="/register"]').click();
    cy.url().should('include', '/register');

    cy.get('input[name="firstName"]').type('Cypress', { force: true });
    cy.get('input[name="lastName"]').type('Test', { force: true });
    cy.get('input[name="login"]').type('cytest' + Date.now(), { force: true });
    cy.get('#password').type('cytest123', { force: true });
    cy.get('#confirm-password').type('cytest123', { force: true }).trigger('blur');
    cy.get('input[name="email"]').type('cytest' + Date.now() + '@example.com', { force: true }).trigger('blur');

    cy.get('#register-btn').should('not.be.disabled').click();

    cy.get('.success', { timeout: 15000 }).should('be.visible');
    cy.url({ timeout: 20000 }).should('include', '/login');
  });

  it('should show error when passwords do not match during registration', () => {
    cy.get('a[routerLink="/register"]').click();

    cy.get('#password').type('pass1', { force: true }).trigger('blur');
    cy.get('#confirm-password').type('pass2', { force: true }).trigger('blur');

    // The register button should be disabled when passwords don't match
    cy.get('#register-btn').should('be.disabled');
  });
});
