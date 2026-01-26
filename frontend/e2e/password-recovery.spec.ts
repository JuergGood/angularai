import { test, expect } from '@playwright/test';

test.describe('Password Recovery Workflow', () => {

  test.use({ storageState: { cookies: [], origins: [] } });

  test('should show the forgot password link and handle the recovery process', async ({ page }) => {
    // 1. Navigate to login page
    await page.goto('/login');

    // 2. Click "Forgot your password?" link
    const forgotLink = page.getByRole('link', { name: /Forgot your password|Passwort vergessen/i });
    await expect(forgotLink).toBeVisible();
    await forgotLink.click();

    await expect(page).toHaveURL(/\/forgot-password/);

    // 3. Request Password Reset
    const emailInput = page.locator('input[type="email"]');
    await emailInput.fill('user@example.com');

    // Mock the backend call
    await page.route('**/api/auth/forgot-password', async route => {
      await route.fulfill({ status: 200 });
    });

    const resetBtn = page.getByRole('button', { name: /Reset Password|Passwort zurücksetzen/i });

    // Log state before click
    const isDisabled = await resetBtn.isDisabled();
    console.log(`[DEBUG_LOG] Reset button disabled: ${isDisabled}`);

    await resetBtn.click({ force: true });

    // 4. Verify generic confirmation message
    await expect(page.locator('.confirmation-message')).toBeVisible();
    await expect(page.locator('.confirmation-message')).toContainText(/you will receive a password recovery link shortly|erhalten Sie in Kürze einen Link/i);

    // 5. Navigate to reset-password page with token
    await page.goto('/reset-password?token=valid-token-123');

    // 6. Set New Password
    const passwordInput = page.locator('input[formControlName="password"]');
    const confirmInput = page.locator('input[formControlName="confirmPassword"]');
    const saveBtn = page.getByRole('button', { name: /Save Password|Passwort speichern/i });

    await passwordInput.fill('NewSecurePassword123!');
    await confirmInput.fill('NewSecurePassword123!');

    // Mock the backend call for reset
    await page.route('**/api/auth/reset-password', async route => {
      await route.fulfill({ status: 200 });
    });

    await saveBtn.click();

    // 7. Verify success message and login link
    await expect(page.locator('.reset-success')).toBeVisible();
    await expect(page.locator('.reset-success')).toContainText(/successfully reset|erfolgreich zurückgesetzt/i);

    const loginBtn = page.locator('.reset-success').getByRole('link', { name: /Login|Anmelden/i });
    await expect(loginBtn).toBeVisible();
    await loginBtn.click();
    await expect(page).toHaveURL(/\/login/);
  });

});
