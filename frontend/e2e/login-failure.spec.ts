import { test, expect } from '@playwright/test';

test.describe('Login Failure Handling', () => {

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

  test('should show error message on invalid password and capture screenshot', async ({ page }) => {
    // 1. Navigate to login page
    await page.goto('/login');
    await page.waitForSelector('.login-card');

    // 2. Fill in login and a wrong password
    await page.fill('input[name="login"]', 'admin');
    await page.fill('input[name="password"]', 'wrong-password');

    // 3. Click login button
    await page.click('#login-btn');

    // 4. Wait for the error message to appear
    const errorMsg = page.locator('.error');
    await expect(errorMsg).toBeVisible({ timeout: 10000 });

    // 5. Verify the error message text (localized)
    // Since we don't know the exact language of the runner, we check if it contains "failed" or "fehlgeschlagen"
    const errorText = await errorMsg.innerText();
    expect(errorText.toLowerCase()).toMatch(/failed|fehlgeschlagen/);

    // 6. Capture screenshot
    console.log('Capturing login failure screenshot...');
    await page.screenshot({ path: 'e2e-screenshots/login-failure.png' });

    // Also capture the specific error element for better visibility in logs if needed
    await errorMsg.screenshot({ path: 'e2e-screenshots/login-error-message.png' });
  });

  test('should show error message on non-existent user and capture screenshot', async ({ page }) => {
    await page.goto('/login');
    await page.waitForSelector('.login-card');

    await page.fill('input[name="login"]', 'non-existent-user-' + Date.now());
    await page.fill('input[name="password"]', 'any-password');

    await page.click('#login-btn');

    const errorMsg = page.locator('.error');
    await expect(errorMsg).toBeVisible({ timeout: 10000 });

    const errorText = await errorMsg.innerText();
    expect(errorText.toLowerCase()).toMatch(/failed|fehlgeschlagen/);

    await page.screenshot({ path: 'e2e-screenshots/login-failure-non-existent.png' });
  });
});
