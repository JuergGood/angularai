describe('User Management System', () => {
  beforeEach(() => {
    // We assume the backend is running and has the default admin/admin123 user
    cy.visit('/login');
  });

  it('should login successfully and redirect to profile', () => {
    cy.get('input[name="login"]').type('admin');
    cy.get('input[name="password"]').type('admin123');
    cy.get('button[type="submit"]').click();

    cy.url().should('include', '/profile');
    cy.get('h2').should('contain', 'User Profile');
    cy.get('input[name="login"]').should('have.value', 'admin');
  });

  it('should show error on invalid login', () => {
    cy.get('input[name="login"]').type('wrong');
    cy.get('input[name="password"]').type('wrong');
    cy.get('button[type="submit"]').click();

    cy.get('.error').should('contain', 'Invalid login or password');
    cy.url().should('include', '/login');
  });

  it('should edit user profile and save changes', () => {
    // Login first
    cy.get('input[name="login"]').type('admin');
    cy.get('input[name="password"]').type('admin123');
    cy.get('button[type="submit"]').click();

    // Edit profile
    cy.get('input[name="firstName"]').clear().type('Super');
    cy.get('input[name="lastName"]').clear().type('Admin');
    cy.get('input[name="birthDate"]').type('1985-05-20');
    cy.get('textarea[name="address"]').clear().type('New Admin Street 1');

    cy.get('button').contains('Save Changes').click();

    cy.get('.success').should('contain', 'Profile updated successfully!');

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
    cy.get('button[type="submit"]').click();

    // Logout
    cy.get('.logout-btn').click();

    cy.url().should('include', '/login');
    cy.get('h2').should('contain', 'Login');
  });
});
