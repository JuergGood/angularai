describe('Comprehensive Frontend Tests', () => {
  const timestamp = Date.now();
  const adminUser = 'admin';
  const adminPass = 'admin123';
  const testUser = `comp_user_${timestamp}`;
  const testEmail = `comp_${timestamp}@example.com`;

  beforeEach(() => {
    // Intercept common API calls
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: { id: 1, firstName: 'Admin', lastName: 'User', login: 'admin', email: 'admin@example.com', role: 'ROLE_ADMIN' }
    }).as('loginRequest');

    cy.intercept('GET', '/api/users/me', {
      statusCode: 200,
      body: { id: 1, firstName: 'Admin', lastName: 'User', login: 'admin', email: 'admin@example.com', role: 'ROLE_ADMIN' }
    }).as('meRequest');

    cy.intercept('GET', '/api/dashboard', {
      statusCode: 200,
      body: {
        summary: { openTasks: 5, openTasksDelta: 1, activeUsers: 3, activeUsersDelta: 0, completedTasks: 10, completedTasksDelta: 2, todayLogs: 15, todayLogsDelta: 5 },
        taskDistribution: { total: 15, open: 5, inProgress: 0, completed: 10 },
        recentActivity: [{ timestamp: new Date().toISOString(), login: 'admin', action: 'USER_LOGIN' }],
        recentUsers: [{ login: 'admin', email: 'admin@example.com', role: 'ROLE_ADMIN' }],
        priorityTasks: []
      }
    }).as('dashboardRequest');

    cy.intercept('GET', '/api/tasks', { statusCode: 200, body: [] }).as('tasksRequest');
    cy.intercept('POST', '/api/auth/logout', { statusCode: 200 }).as('logoutRequest');

    cy.window().then((win) => {
      win.localStorage.clear();
      win.sessionStorage.clear();
    });
  });

  const loginAsAdmin = () => {
    cy.visit('/login');
    cy.get('input[name="login"]').type(adminUser, { force: true });
    cy.get('input[name="password"]').type(adminPass, { force: true });
    cy.get('#login-btn').click({ force: true });
    cy.get('.user-profile-button', { timeout: 10000 }).should('be.visible');
  };

  it('should register a user', () => {
    cy.intercept('POST', '/api/auth/register', { statusCode: 200, body: { id: 2, login: testUser } }).as('registerRequest');
    cy.visit('/register');
    cy.get('input[name="firstName"]').type('Temp', { force: true });
    cy.get('input[name="lastName"]').type('User', { force: true });
    cy.get('input[name="login"]').type(testUser, { force: true });
    cy.get('#password').type('Pass123!', { force: true });
    cy.get('#confirm-password').type('Pass123!', { force: true });
    cy.get('input[name="email"]').type(testEmail, { force: true });
    cy.get('mat-datepicker-toggle').click({ force: true });
    cy.get('button.mat-calendar-body-cell').contains('15').click({ force: true });
    cy.get('#register-btn').click({ force: true });
    cy.wait('@registerRequest');
    cy.url().should('include', '/login');
  });

  it('should display dashboard and navigate', () => {
    loginAsAdmin();
    cy.get('.dashboard-container', { timeout: 15000 }).should('be.visible');
    cy.get('[data-cy="dashboard-open-tasks"]').click({ force: true });
    cy.url().should('include', '/tasks');
  });

  it('should handle admin user management', () => {
    loginAsAdmin();
    cy.intercept('GET', '/api/admin/users', {
      statusCode: 200,
      body: [
        { id: 1, login: 'admin', firstName: 'Admin', lastName: 'User', email: 'admin@example.com', role: 'ROLE_ADMIN' },
        { id: 2, login: 'testuser', firstName: 'Temp', lastName: 'User', email: 'test@example.com', role: 'ROLE_USER' }
      ]
    }).as('getUsers');

    // Use the link from the sidenav instead of visit to keep session
    cy.get('a[routerLink="/user-admin"]').click({ force: true });
    cy.wait('@getUsers');
    cy.contains('td', 'testuser').should('exist');

    // Edit user (mock fail)
    cy.intercept('PUT', '/api/admin/users/*', { statusCode: 400, body: 'Bad Request' }).as('updateFail');
    cy.contains('td', 'testuser').parent('tr').find('button').first().click({ force: true });
    cy.get('button').contains('Save').click({ force: true });
    cy.wait('@updateFail');
    cy.get('.error').should('exist');
    cy.get('button').contains('Cancel').click({ force: true });
  });

  it('should filter and clear logs', () => {
    loginAsAdmin();
    cy.get('a[routerLink="/logs"]').click({ force: true });
    cy.url().should('include', '/logs');
    cy.get('mat-select').first().click({ force: true });
    cy.get('mat-option').contains('Login').click({ force: true });
    cy.get('button.reset-filter-btn').click({ force: true });
  });

  it('should update profile', () => {
    loginAsAdmin();
    cy.get('.user-profile-button').click({ force: true });
    cy.get('.mat-mdc-menu-item').contains('Profile').click({ force: true });
    cy.url().should('include', '/profile');
    cy.get('input[name="firstName"]').clear({ force: true }).type('AdminName', { force: true });
    cy.get('#save-btn').click({ force: true });
    cy.get('.success').should('exist');
  });
});
