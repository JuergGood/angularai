import { test, expect, Page } from '@playwright/test';

async function loginAs(page: Page, role: 'admin' | 'adminread' | 'user') {
  await page.goto('/login');

  // Credentials for roles (assuming they are seeded or we use existing ones)
  // In a real environment we might need to create these users first in a setup step
  // But for this test we assume 'admin', 'adminread', and 'user' exist with 'password123'
  // Or we use the seeding from the backend tests if they share a DB.
  // Actually Playwright tests usually run against a real started app.

  const credentials = {
    admin: { login: 'admin', password: 'admin123' },
    adminread: { login: 'adminread', password: 'password123' },
    user: { login: 'user', password: 'password123' }
  };

  const creds = credentials[role];

  await page.fill('input[name="login"]', creds.login);
  await page.fill('input[name="password"]', creds.password);
  await page.click('#login-btn');

  await expect(page).toHaveURL(/.*dashboard|.*tasks/);
}

test.describe('Role-Based Security E2E', () => {

  test('ADMIN should have full access to User Management', async ({ page }) => {
    await loginAs(page, 'admin');

    await page.goto('/user-admin');
    await expect(page.locator('h2.page-title')).toContainText('User Management');

    // Check if "Create User" button is visible
    await expect(page.locator('.add-user-btn')).toBeVisible();

    // Check if "Edit" icon is visible in the table
    await expect(page.locator('mat-icon:has-text("edit")').first()).toBeVisible();

    // Check if "Delete" icon is visible in the table
    await expect(page.locator('mat-icon:has-text("delete")').first()).toBeVisible();

    // Open edit dialog and check if "Save" button is visible
    await page.locator('button[title="Edit User"]').first().click();
    await expect(page.locator('.submit-btn')).toBeVisible();
    await expect(page.locator('input[name="firstName"]')).toBeEnabled();
  });

  test('ADMIN_READ should have read-only access to User Management', async ({ page }) => {
    // Note: We need to ensure 'adminread' user exists.
    // If not, this test might fail unless we have a setup script that creates users.
    await loginAs(page, 'adminread');

    await page.goto('/user-admin');
    await expect(page.locator('h2.page-title')).toContainText('User Management');

    // "Create User" button should NOT be visible
    await expect(page.locator('.add-user-btn')).not.toBeVisible();

    // "Edit" icon should be "visibility" icon instead, and "Delete" should be hidden
    await expect(page.locator('mat-icon:has-text("visibility")').first()).toBeVisible();
    await expect(page.locator('mat-icon:has-text("delete")')).not.toBeVisible();

    // Open view dialog and check if "Save" button is hidden and fields are disabled
    await page.locator('button[title="View User"]').first().click();
    await expect(page.locator('.submit-btn')).not.toBeVisible();
    await expect(page.locator('input[name="firstName"]')).toBeDisabled();
  });

  test('USER should be denied access to User Management', async ({ page }) => {
    await loginAs(page, 'user');

    // Try to navigate directly
    await page.goto('/user-admin');

    // Should show "Access Denied" message (defined in template)
    await expect(page.locator('.main-card')).toContainText('Access Denied');

    // Sidenav links for admin should not be visible
    await expect(page.locator('a[routerLink="/user-admin"]')).not.toBeVisible();
    await expect(page.locator('a[routerLink="/logs"]')).not.toBeVisible();
  });

  test('ADMIN_READ should see settings but cannot modify them', async ({ page }) => {
    await loginAs(page, 'adminread');

    // Open settings menu
    await page.click('.settings-button');

    // Admin settings should be visible (Geolocation, reCAPTCHA, Landing Message)
    // We check for the text since mat-menu-item doesn't have unique IDs easily
    await expect(page.locator('button:has-text("Geolocation")')).toBeVisible();
    await expect(page.locator('button:has-text("reCAPTCHA")')).toBeVisible();

    // Try to click Geolocation toggle - it should show a snackbar error if we try to call the API
    // Actually in the UI, we didn't disable the click handler in sidenav.component.html
    // but the backend will return 403.
    // Let's see if we can trigger the snackbar.
    await page.click('button:has-text("Geolocation")');

    // Wait for snackbar with error message
    // sidenav.component.ts: this.snackBar.open('Failed to update geolocation setting', ...
    await expect(page.locator('mat-snack-bar-container')).toContainText('Failed to update geolocation setting');
  });

  test('USER should not see admin settings', async ({ page }) => {
    await loginAs(page, 'user');

    // Open settings menu
    await page.click('.settings-button');

    // Admin settings should NOT be visible
    await expect(page.locator('button:has-text("Geolocation")')).not.toBeVisible();
    await expect(page.locator('button:has-text("reCAPTCHA")')).not.toBeVisible();
    await expect(page.locator('button:has-text("Landing Message")')).not.toBeVisible();
  });
});
