import { test, expect } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

test.describe('Forgot Password Documentation Screenshots', () => {

  test.use({ storageState: { cookies: [], origins: [] } });

  const screenshotDir = 'doc/user-guide/workflows/assets';

  test.beforeAll(async () => {
    // Navigate from frontend/e2e/ to root
    const absolutePath = path.resolve(process.cwd(), '../', screenshotDir);
    if (!fs.existsSync(absolutePath)) {
      fs.mkdirSync(absolutePath, { recursive: true });
    }
  });

  test('capture forgot password workflow screenshots', async ({ page }) => {
    const absolutePath = path.resolve(process.cwd(), '../', screenshotDir);

    // 1. Login Page - Highlight "Forgot password?" link
    await page.goto('/login');
    await page.screenshot({ path: `${absolutePath}/11-login-page-forgot-link.png`, fullPage: true });

    // 2. Click Forgot Password
    await page.locator('a.forgot-link').click();
    await expect(page).toHaveURL(/\/forgot-password/);
    await page.screenshot({ path: `${absolutePath}/12-forgot-password-form.png`, fullPage: true });

    // 3. Fill Email
    await page.locator('input[type="email"]').fill('john.doe@example.com');
    await page.screenshot({ path: `${absolutePath}/13-forgot-password-filled.png`, fullPage: true });

    // 4. Submit and see Success Message
    await page.route('**/api/auth/forgot-password', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Success' })
      });
    });
    await page.getByRole('button', { name: /Reset Password|Passwort zurücksetzen/i }).click();
    await expect(page.locator('.confirmation-message')).toBeVisible();
    await page.screenshot({ path: `${absolutePath}/14-forgot-password-success.png`, fullPage: true });

    // 5. Reset Password Form (simulated via token in URL)
    await page.goto('/reset-password?token=mock-token-123');
    await expect(page.locator('input[formControlName="password"]')).toBeVisible();
    await page.screenshot({ path: `${absolutePath}/15-reset-password-form.png`, fullPage: true });

    // 6. Fill New Password
    await page.locator('input[formControlName="password"]').fill('NewSecure123!');
    await page.locator('input[formControlName="confirmPassword"]').fill('NewSecure123!');
    await page.screenshot({ path: `${absolutePath}/16-reset-password-filled.png`, fullPage: true });

    // 7. Submit Reset and see Success
    await page.route('**/api/auth/reset-password', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'Success' })
      });
    });

    // Check if the button is enabled before clicking
    const saveBtn = page.getByRole('button', { name: /Save Password|Passwort speichern/i });
    await expect(saveBtn).toBeEnabled();
    await saveBtn.click();

    // Try to find any success indication
    await page.waitForTimeout(1000);
    await page.screenshot({ path: `${absolutePath}/17-reset-password-success.png`, fullPage: true });
  });
});
