import { test as setup, expect } from '@playwright/test';

setup('authenticate', async ({ page }) => {
  await page.goto('/login');
  await page.fill('input[name="login"]', 'admin');
  await page.fill('input[name="password"]', 'admin123');

  const loginResponse = page.waitForResponse(response =>
    response.url().includes('/api/auth/login') && response.request().method() === 'POST'
  );

  await page.click('#login-btn');
  await loginResponse;

  await expect(page).toHaveURL(/.*dashboard|.*tasks/);

  // End session by saving state
  await page.context().storageState({ path: 'playwright/.auth/user.json' });
});
