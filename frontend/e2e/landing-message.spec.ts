import { test, expect } from '@playwright/test';

test.describe('Landing Message Feature', () => {

  test.beforeEach(async ({ page, context }) => {
    // Navigate to a page on our domain first so we have access to localStorage
    await page.goto('/login');
    // Clear storage state to ensure we are starting fresh
    await context.clearCookies();
    await page.evaluate(() => {
      localStorage.clear();
      sessionStorage.clear();
    });
    // Ensure landing message is enabled via API for consistent tests
    await page.evaluate(async () => {
      const auth = btoa('admin:admin123');
      await fetch('/api/admin/settings/landing-message', {
        method: 'POST',
        headers: {
          'Authorization': `Basic ${auth}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ enabled: true })
      });
    });
    await page.reload();
  });

  test('should display landing message in English and German', async ({ page }) => {
    // 1. Check English (Default)
    await page.goto('/login');
    const banner = page.locator('.landing-info-banner');

    // In our test environment, we'll try to wait for it, but if backend is down we can't do much.
    // However, the task requires us to add this test.
    await expect(banner).toBeVisible({ timeout: 15000 });
    const englishMessage = await banner.locator('span').first().textContent();
    console.log(`English Message: ${englishMessage}`);
    expect(englishMessage?.length).toBeGreaterThan(0);

    await page.screenshot({ path: 'e2e-screenshots/landing-message-en.png' });

    // 2. Switch to German
    // Click settings menu
    await page.waitForSelector('.settings-button');
    await page.click('.settings-button');
    // Click Language menu item
    await page.waitForSelector('button:has-text("Language"), button:has-text("Sprache")');
    await page.click('button:has-text("Language"), button:has-text("Sprache")');
    // Click German
    await page.waitForSelector('button:has-text("German"), button:has-text("Deutsch")');
    await page.click('button:has-text("German"), button:has-text("Deutsch")');

    // Wait for the banner to update (the message should change)
    await expect(banner).toBeVisible();
    const germanMessage = await banner.locator('span').first().textContent();
    console.log(`German Message: ${germanMessage}`);
    expect(germanMessage?.length).toBeGreaterThan(0);
    expect(germanMessage).not.toBe(englishMessage);

    await page.screenshot({ path: 'e2e-screenshots/landing-message-de-ch.png' });
  });

  test('admin should be able to enable/disable landing message', async ({ page }) => {
    // 1. Login as Admin
    await page.goto('/login');
    // Ensure the page is loaded
    await page.waitForSelector('.login-card');

    await page.fill('input[name="login"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('#login-btn');

    // Wait for redirect to dashboard or tasks
    await page.waitForURL(/.*dashboard|.*tasks/, { timeout: 30000 });

    const banner = page.locator('.landing-info-banner');
    // We might need to refresh if the banner didn't show up yet
    await page.reload();
    await expect(banner).toBeVisible({ timeout: 15000 });

    // 2. Disable landing message
    await page.waitForSelector('.settings-button');
    await page.click('.settings-button', { force: true });
    // Use a regex to be more flexible with the text and language
    const toggleButton = page.locator('.mat-mdc-menu-item:has-text("Landing Message"), .mat-mdc-menu-item:has-text("Landing-Nachricht")');
    await expect(toggleButton).toBeVisible({ timeout: 15000 });

    // Check if it's currently ON and toggle it
    const toggleText = await toggleButton.textContent();
    console.log(`Current toggle text: ${toggleText}`);

    await toggleButton.click();

    // Wait for the banner to disappear
    await expect(banner).not.toBeVisible();
    await page.screenshot({ path: 'e2e-screenshots/landing-message-disabled.png' });

    // 3. Enable it back
    await page.click('.settings-button');
    await toggleButton.click();

    // Wait for the banner to reappear
    await expect(banner).toBeVisible();
    await page.screenshot({ path: 'e2e-screenshots/landing-message-enabled.png' });
  });
});
