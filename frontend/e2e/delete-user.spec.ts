import { test, expect } from '@playwright/test';

test.describe('Delete User Account Workflow', () => {

  test.beforeEach(async ({ page, context }) => {
    // Navigate to a page on our domain first so we have access to localStorage
    await page.goto('/login');
    // Clear storage state to ensure we are logged out
    await context.clearCookies();
    await page.evaluate(() => {
      localStorage.clear();
      sessionStorage.clear();
    });
  });

  test('user2 can delete their own account', async ({ page }) => {
    // 1. Login as user2
    await page.goto('/login');
    await page.screenshot({ path: 'e2e-screenshots/delete-user-1-login.png' });

    // Fill credentials
    await page.fill('input[name="login"]', 'user2');
    await page.fill('input[name="password"]', 'user123');
    await page.screenshot({ path: 'e2e-screenshots/delete-user-2-login-filled.png' });

    // Click login
    await page.click('#login-btn');

    // Wait for redirect to dashboard or tasks
    await expect(page).toHaveURL(/\/dashboard|\/tasks/, { timeout: 15000 });
    console.log('Logged in as user2');
    await page.screenshot({ path: 'e2e-screenshots/delete-user-3-home.png' });

    // 2. Navigate to Profile
    await page.goto('/profile');
    await expect(page).toHaveURL(/\/profile/);
    const title = page.locator('.page-title');
    const titleText = await title.innerText();
    expect(titleText).toMatch(/Profile|NAV.PROFILE/);
    await page.screenshot({ path: 'e2e-screenshots/delete-user-4-profile.png' });

    // 3. Check if delete button is visible
    const deleteBtn = page.locator('#delete-btn');
    await expect(deleteBtn).toBeVisible();

    // 4. Click delete button and check for confirmation dialog
    await deleteBtn.click();
    const dialog = page.locator('mat-dialog-container');
    await expect(dialog).toBeVisible();
    // Allow either the translated text or the translation key
    const dialogText = await dialog.innerText();
    expect(dialogText).toMatch(/Are you sure you want to delete your account\?|NAV.PROFILE_DELETE_CONFIRM/);
    await page.screenshot({ path: 'e2e-screenshots/delete-user-5-confirm-dialog.png' });

    // 5. Confirm deletion
    const confirmBtn = dialog.locator('button[color="warn"]');
    await confirmBtn.click();

    // 6. Verify logout and notification
    // After deletion, user should be redirected to login
    await expect(page).toHaveURL(/\/login/);

    // Check for snackbar message
    const snackbar = page.locator('mat-snack-bar-container');
    await expect(snackbar).toBeVisible();
    const snackbarText = await snackbar.innerText();
    expect(snackbarText).toMatch(/Your account has been deleted successfully\.|NAV.PROFILE_DELETED_SUCCESS/);
    await page.screenshot({ path: 'e2e-screenshots/delete-user-6-deleted-snackbar.png' });

    // 7. Try to login again - should fail
    await page.fill('input[name="login"]', 'user2');
    await page.fill('input[name="password"]', 'user123');
    await page.click('#login-btn');

    // Should show error message
    const errorMsg = page.locator('.error');
    await expect(errorMsg).toBeVisible();
    await page.screenshot({ path: 'e2e-screenshots/delete-user-7-login-failed.png' });
  });

  test('admin user cannot delete their account', async ({ page }) => {
    // 1. Login as admin
    await page.goto('/login');
    await page.fill('input[name="login"]', 'admin');
    // Use environment variable or default password from DataInitializer
    const adminPassword = process.env.ADMIN_PASSWORD || 'admin123';
    await page.fill('input[name="password"]', adminPassword);
    await page.click('#login-btn');

    // Wait for redirect to dashboard or tasks
    await expect(page).toHaveURL(/\/dashboard|\/tasks/);

    // 2. Navigate to Profile
    await page.goto('/profile');
    await expect(page).toHaveURL(/\/profile/);

    // 3. Check if delete button is NOT visible
    const deleteBtn = page.locator('#delete-btn');
    await expect(deleteBtn).not.toBeVisible();
  });
});
