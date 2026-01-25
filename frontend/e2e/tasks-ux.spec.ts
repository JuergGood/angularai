import { test, expect } from '@playwright/test';

test.describe('Tasks UX Screenshots', () => {
  test.beforeEach(async ({ page, context }) => {
    // If we want to ensure we start from a clean state but then use the session,
    // we should be careful. However, the user asked to logout an accidentally
    // logged in user. For tasks-ux, we EXPECT to be logged in.
    // If the storageState is working, we should already be logged in as admin.
  });

  test('capture tasks component states', async ({ page }) => {
    // Enable console logging from the browser
    page.on('console', msg => console.log(`BROWSER: ${msg.text()}`));
    page.on('pageerror', err => console.error(`BROWSER ERROR: ${err.message}`));

    console.log('Navigating directly to /tasks (Auth should be handled by storageState)...');
    await page.goto('/tasks', { waitUntil: 'load', timeout: 30000 });

    // Check local storage to verify if auth is set
    const authStored = await page.evaluate(() => localStorage.getItem('auth'));
    console.log(`Auth token in localStorage: ${authStored ? 'PRESENT' : 'MISSING'}`);

    // If missing, try to navigate to /login to trigger any potential redirect logic
    if (!authStored) {
      console.log('Auth missing in localStorage, checking if we are redirected...');
      await page.goto('/', { waitUntil: 'load' });

      const currentUrl = page.url();
      if (currentUrl.includes('/login')) {
         console.log('Redirected to login, attempting to login manually...');
         await page.fill('input[name="login"]', 'admin');
         await page.fill('input[name="password"]', 'admin123');
         await page.click('#login-btn');
         await page.waitForURL(/.*tasks/);
      }
    }

    // Wait for the container with a longer timeout and better logging
    console.log('Waiting for .tasks-container or task cards...');
    try {
      await page.waitForSelector('.tasks-container, .task-card, .empty-message, app-tasks', { timeout: 30000 });
      console.log('Tasks UI detected.');
    } catch (e) {
      console.error(`Tasks UI NOT detected: ${e.message}`);
      await page.screenshot({ path: 'e2e-screenshots/tasks-load-failed.png', fullPage: true });
      const bodyPreview = await page.evaluate(() => document.body.innerHTML.substring(0, 1000));
      console.log('HTML Body Preview:', bodyPreview);
      throw e;
    }

    // 2. Default View Screenshot
    console.log('Capturing default view...');
    await page.waitForTimeout(2000); // Give it a bit more time for everything to render
    await page.screenshot({ path: 'e2e-screenshots/tasks-default.png', fullPage: true });

    // 3. Add Task Form Screenshot
    console.log('Opening Add Task form...');
    // Try to find by text if class selector fails
    const addTaskBtn = page.locator('.add-task-btn, button:has-text("Add Task"), button:has-text("Task hinzufügen")').first();

    if (await addTaskBtn.count() > 0 && await addTaskBtn.isVisible()) {
      await addTaskBtn.click();
      await page.waitForSelector('.add-task-card, mat-card-title:has-text("Add Task"), mat-card-title:has-text("Task hinzufügen")', { timeout: 5000 });
      await page.screenshot({ path: 'e2e-screenshots/tasks-add-form.png' });
    } else {
      console.log('Add Task button not found or not visible. Taking diagnostic screenshot.');
      await page.screenshot({ path: 'e2e-screenshots/tasks-add-btn-missing.png' });
    }

    // 4. Bulk Actions Screenshot
    console.log('Capturing Bulk Actions...');
    // Don't re-navigate, just use current state
    const taskCards = page.locator('.task-card');
    const taskCount = await taskCards.count();
    console.log(`Found ${taskCount} task cards.`);

    if (taskCount >= 2) {
      const checkboxes = page.locator('.task-card mat-checkbox').locator('input');
      // Click the first two checkboxes
      await checkboxes.nth(0).click({ force: true });
      await checkboxes.nth(1).click({ force: true });
      await page.waitForSelector('.bulk-actions-bar', { timeout: 5000 });
      await page.screenshot({ path: 'e2e-screenshots/tasks-bulk-actions.png' });
    } else if (taskCount > 0) {
      // If only one task, try to select that one
      const checkbox = page.locator('.task-card mat-checkbox').locator('input').first();
      await checkbox.click({ force: true });
      await page.waitForSelector('.bulk-actions-bar', { timeout: 5000 });
      await page.screenshot({ path: 'e2e-screenshots/tasks-bulk-actions.png' });
    }

    // 5. Priority Menu Screenshot
    console.log('Capturing Priority Menu...');
    const priorityBadge = page.locator('.priority-badge').first();
    if (await priorityBadge.isVisible()) {
      await priorityBadge.click();
      await page.waitForSelector('.mat-mdc-menu-panel', { timeout: 10000 });
      await page.screenshot({ path: 'e2e-screenshots/tasks-priority-menu.png' });
      await page.mouse.click(0, 0); // Close menu
    }

    // 6. Compact View
    console.log('Capturing Compact View...');
    const viewModeBtn = page.locator('button[title*="View Mode"], button[title*="Ansichtsmodus"], button mat-icon:has-text("view_headline"), button mat-icon:has-text("view_stream")').first();
    if (await viewModeBtn.isVisible()) {
      await viewModeBtn.click();
      await page.waitForTimeout(1000);
      await page.screenshot({ path: 'e2e-screenshots/tasks-compact-view.png', fullPage: true });
    }
  });
});
