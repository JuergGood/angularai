describe('Task Management New Features (Mocked)', () => {
  const mockExistingTask = {
    id: 1,
    title: 'Existing Task',
    description: 'Desc',
    priority: 'MEDIUM',
    status: 'OPEN',
    dueDate: '2026-01-20',
    createdAt: '2026-01-20 10:00:00'
  };

  beforeEach(() => {
    // Mock Login
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: { id: 1, firstName: 'Regular', lastName: 'User', login: 'user', email: 'user@example.com', role: 'ROLE_USER' }
    }).as('loginRequest');

    // Mock Users/Me
    cy.intercept('GET', '/api/users/me', {
      statusCode: 200,
      body: { id: 1, firstName: 'Regular', lastName: 'User', login: 'user', email: 'user@example.com', role: 'ROLE_USER' }
    }).as('meRequest');

    // Mock Initial Tasks
    cy.intercept('GET', '**/api/tasks*', {
      statusCode: 200,
      body: [mockExistingTask]
    }).as('getTasks');

    // Clear state
    cy.window().then((win) => {
      win.localStorage.clear();
      win.sessionStorage.clear();
    });

    // Login via UI (Mocked)
    cy.visit('/login');
    cy.get('input[name="login"]').type('user', { force: true });
    cy.get('input[name="password"]').type('user123', { force: true });
    cy.get('#login-btn').click({ force: true });

    cy.url({ timeout: 10000 }).should('include', '/tasks');
    cy.wait('@getTasks');
    cy.get('app-quick-add-task', { timeout: 10000 }).should('be.visible');
  });

  it('should add a task using Quick Add', () => {
    const taskTitle = 'New Mocked Task';
    cy.intercept('POST', '/api/tasks', {
      statusCode: 200,
      body: { ...mockExistingTask, id: 2, title: taskTitle, status: 'OPEN' }
    }).as('createTask');

    cy.get('app-quick-add-task input').type(`${taskTitle}{enter}`, { force: true });
    cy.wait('@createTask');

    cy.contains('.task-card', taskTitle).should('be.visible');
  });

  it('should edit task title inline', () => {
    const updatedTitle = 'Updated Mocked Title';
    cy.intercept('PATCH', '/api/tasks/1', {
      statusCode: 200,
      body: { ...mockExistingTask, title: updatedTitle }
    }).as('patchTask');

    cy.contains('.task-title-text', 'Existing Task').dblclick({ force: true });
    cy.get('.task-title-input').clear({ force: true }).type(`${updatedTitle}{enter}`, { force: true });

    cy.wait('@patchTask');
    cy.contains('.task-card', updatedTitle).should('be.visible');
  });

  it('should cycle task status inline', () => {
    // 1st click: Open -> In Progress
    cy.intercept('PATCH', '/api/tasks/1', {
      statusCode: 200,
      body: { ...mockExistingTask, status: 'IN_PROGRESS' }
    }).as('patchTaskInProgress');

    cy.get('.status-pill').click({ force: true });
    cy.wait('@patchTaskInProgress');
    cy.get('.status-pill').should('contain', 'In Progress');

    // 2nd click: In Progress -> Done
    cy.intercept('PATCH', '/api/tasks/1', {
      statusCode: 200,
      body: { ...mockExistingTask, status: 'DONE' }
    }).as('patchTaskDone');

    cy.get('.status-pill').click({ force: true });
    cy.wait('@patchTaskDone');
    cy.get('.status-pill').should('contain', 'Done');
  });

  it('should update task priority using inline menu', () => {
    cy.intercept('PATCH', '/api/tasks/1', {
      statusCode: 200,
      body: { ...mockExistingTask, priority: 'HIGH' }
    }).as('patchTaskHigh');

    cy.get('.priority-badge').click({ force: true });
    cy.get('.cdk-overlay-container').contains('High').click({ force: true });

    cy.wait('@patchTaskHigh');
    cy.get('.priority-badge').should('have.class', 'priority-high');
  });

  it('should filter tasks using smart chips', () => {
    cy.intercept('GET', '**/api/tasks?smartFilter=HIGH*', {
      statusCode: 200,
      body: [{ ...mockExistingTask, title: 'High Priority Task', priority: 'HIGH' }]
    }).as('getHighTasks');

    cy.get('mat-chip-option').contains('High Priority').click({ force: true });
    cy.wait('@getHighTasks');
    cy.get('.task-card', { timeout: 10000 }).should('contain', 'High Priority Task');
  });

  it('should toggle view density', () => {
    cy.get('.task-row').should('not.have.class', 'compact');
    cy.get('button[title="Toggle View Mode"]').click({ force: true });
    cy.get('.task-row').first().should('have.class', 'compact');
  });

  it('should show bulk actions bar when tasks selected', () => {
    cy.get('.task-card mat-checkbox').first().find('input').click({ force: true });
    // Verify the state change in the component if possible, but here we check UI
    cy.get('.bulk-actions-bar', { timeout: 15000 }).should('exist').and('be.visible');
    cy.get('.bulk-actions-bar').should('contain', '1 selected');
  });

  it('should show completed tasks in the completed section', () => {
    cy.intercept('PATCH', '/api/tasks/1', {
      statusCode: 200,
      body: { ...mockExistingTask, status: 'DONE', completedAt: new Date().toISOString() }
    }).as('completeTask');

    cy.get('.task-status-btn').click({ force: true });
    cy.wait('@completeTask');

    cy.get('mat-expansion-panel-header').click({ force: true });
    cy.get('app-completed-tasks-section').should('contain', 'Existing Task');
  });
});
