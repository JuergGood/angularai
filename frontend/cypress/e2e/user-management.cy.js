describe('User Management System', () => {
  beforeEach(() => {
    // We assume the backend is running and has the default admin/admin123 user
    cy.visit('/login');
  });

  it('should login successfully and redirect to profile', () => {
    cy.screenshot('login-page');
    cy.get('input[name="login"]').type('admin');
    cy.get('input[name="password"]').type('admin123');
    cy.screenshot('login-filled');
    cy.get('#login-btn').click();

    cy.url().should('include', '/profile');
    cy.get('mat-card-title').should('contain', 'User Profile Details');
    cy.screenshot('profile-page');
  });

  it('should show error on invalid login', () => {
    cy.get('input[name="login"]').type('wrong');
    cy.get('input[name="password"]').type('wrong');
    cy.get('#login-btn').click();

    cy.get('#login-error').should('be.visible').and('contain', 'Invalid login or password');
    cy.url().should('include', '/login');
  });

  it('should edit user profile and save changes', () => {
    // Login first
    cy.get('input[name="login"]').type('admin');
    cy.get('input[name="password"]').type('admin123');
    cy.get('#login-btn').click();

    // Edit profile
    cy.get('input[name="firstName"]').clear().type('Super');
    cy.get('input[name="lastName"]').clear().type('Admin');
    cy.get('input[name="birthDate"]').clear().type('1985-05-20');
    cy.get('textarea[name="address"]').clear().type('New Admin Street 1');

    cy.get('#save-btn').click();

    cy.get('#profile-message').should('be.visible').and('contain', 'Profile updated successfully!');

    // Reload and verify
    cy.reload();
    cy.get('input[name="firstName"]').should('have.value', 'Super');
    cy.get('input[name="lastName"]').should('have.value', 'Admin');
    cy.get('textarea[name="address"]').should('have.value', 'New Admin Street 1');
  });

  it('should logout successfully', () => {
    // Login first
    cy.get('input[name="login"]').type('admin');
    cy.get('input[name="password"]').type('admin123');
    cy.get('#login-btn').click();

    // Logout
    cy.get('#logout-btn').click();

    cy.url().should('include', '/login');
    cy.get('mat-card-title').should('contain', 'Login');
  });

  it('should register a new user successfully', () => {
    cy.get('a[routerLink="/register"]').click();
    cy.url().should('include', '/register');

    cy.get('input[name="firstName"]').type('Cypress');
    cy.get('input[name="lastName"]').type('Test');
    cy.get('input[name="login"]').type('cytest');
    cy.get('#password').type('cytest123');
    cy.get('#confirm-password').type('cytest123');
    cy.get('input[name="email"]').type('cytest@example.com');

    cy.get('#register-btn').should('not.be.disabled').click();

    cy.get('.success').should('be.visible').and('contain', 'Registration successful');
    cy.url({ timeout: 5000 }).should('include', '/login');
  });

  it('should show error when passwords do not match during registration', () => {
    cy.get('a[routerLink="/register"]').click();

    cy.get('#password').type('pass1');
    cy.get('#confirm-password').type('pass2');

    cy.get('.error').should('be.visible').and('contain', 'Passwords do not match');
    cy.get('#register-btn').should('be.disabled');
  });
});
