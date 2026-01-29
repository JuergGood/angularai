import { test, expect } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

test.describe('Registration Documentation Screenshots', () => {

  test.use({ storageState: { cookies: [], origins: [] } });

  const screenshotDir = 'doc/user-guide/workflows/assets';

  test.beforeAll(async () => {
    // Navigate from frontend/e2e/ to root
    const absolutePath = path.resolve(process.cwd(), '../', screenshotDir);
    if (!fs.existsSync(absolutePath)) {
      fs.mkdirSync(absolutePath, { recursive: true });
    }
  });

  test('capture registration workflow screenshots', async ({ page }) => {
    const absolutePath = path.resolve(process.cwd(), '../', screenshotDir);
    // Intercept recaptcha site key request and return "disabled" for initial load
    // We do this to ensure we don't get errors or delays on first load
    await page.route('**/api/system/recaptcha-site-key', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'text/plain',
        body: 'disabled'
      });
    });

    // 1. Landing / Login Page (to show where to start)
    await page.goto('/login');
    await page.screenshot({ path: `${absolutePath}/01-login-page.png`, fullPage: true });

    // 2. Click Register
    await page.locator('a[routerLink="/register"]').click();
    await expect(page).toHaveURL(/\/register/);

    // Now intercept and return a real-looking key so it renders
    await page.route('**/api/system/recaptcha-site-key', async route => {
        await route.fulfill({
          status: 200,
          contentType: 'text/plain',
          body: '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI' // Public test key
        });
      });

    // Reload to pick up the new key and render recaptcha
    await page.reload();
    await page.waitForTimeout(2000);
    await page.screenshot({ path: `${absolutePath}/02-registration-form-empty.png`, fullPage: true });

    // 3. Fill Form
    await page.locator('input[formControlName="fullName"]').fill('John Doe');
    await page.locator('input[formControlName="login"]').fill('johndoe');
    await page.locator('input[formControlName="email"]').fill('john.doe@example.com');
    await page.locator('input[formControlName="password"]').fill('Secret123!');
    await page.locator('input[formControlName="confirmPassword"]').fill('Secret123!');

    // Bypass reCAPTCHA validation for the click, but keep it visible for the screenshot
    await page.evaluate(() => {
      (window as any).BYPASS_RECAPTCHA = true;
    });

    await page.waitForTimeout(1000);
    await page.screenshot({ path: `${absolutePath}/03-registration-form-filled.png`, fullPage: true });

    // 4. Submit and see Success Page
    // Mock the registration request to ensure success
    await page.route('**/api/auth/register', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'User registered successfully' })
      });
    });

    // Directly call the component's executeRegistration if click is hard
    await page.evaluate(() => {
        const el = document.querySelector('app-register');
        if (el) {
            (window as any).ng.getComponent(el).executeRegistration('fake-token');
        }
    });

    await expect(page).toHaveURL(/\/register\/success/);
    await page.screenshot({ path: `${absolutePath}/04-registration-success.png`, fullPage: true });

    // 5. Verification Success (simulated)
    await page.goto('/verify/success');
    await page.screenshot({ path: `${absolutePath}/05-verification-success.png`, fullPage: true });

    // 6. Verification Error - Expired (simulated)
    await page.goto('/verify/error?reason=expired&email=john.doe@example.com');
    await page.screenshot({ path: `${absolutePath}/06-verification-expired.png`, fullPage: true });
  });
});
