import { test, expect } from '@playwright/test';

test.describe('Auth Flow (Login & Register)', () => {

  test.beforeEach(async ({ page, context }) => {
    console.log('Ensuring clean state (logged out)...');
    // Navigate to a page on our domain first so we have access to localStorage
    await page.goto('/login');
    // Clear storage state to ensure we are logged out
    await context.clearCookies();
    await page.evaluate(() => {
      localStorage.clear();
      sessionStorage.clear();
    });
  });

  test('capture login screen', async ({ page }) => {
    console.log('Navigating to /login...');
    await page.goto('/login');

    // Wait for the login card
    await page.waitForSelector('.login-card');

    // Take screenshot of the login page
    console.log('Capturing login screen...');
    await page.screenshot({ path: 'e2e-screenshots/login-screen.png' });

    // Verify login elements
    await expect(page.locator('input[name="login"]')).toBeVisible();
    await expect(page.locator('input[name="password"]')).toBeVisible();
    await expect(page.locator('#login-btn')).toBeVisible();
  });

  test('capture registration screen and attempt registration', async ({ page }) => {
    // Enable console logging from the browser
    page.on('console', msg => console.log(`BROWSER: ${msg.text()}`));
    page.on('pageerror', err => console.error(`BROWSER ERROR: ${err.message}`));

    console.log('Navigating to /register...');
    await page.goto('/register');

    // Wait for the register card
    await page.waitForSelector('.register-card');

    // Take screenshot of the empty registration form
    console.log('Capturing empty registration screen...');
    await page.screenshot({ path: 'e2e-screenshots/register-screen-empty.png' });

    // Fill out the registration form
    console.log('Filling registration form...');
    await page.fill('input[formControlName="fullName"]', 'John Doe');
    const uniqueId = Date.now();
    await page.fill('input[formControlName="login"]', 'johndoe' + uniqueId); // Unique login
    await page.fill('input[formControlName="email"]', 'john.doe' + uniqueId + '@example.com'); // Unique email

    // Password must match pattern: ^(?=.*[A-Za-z])(?=.*[^A-Za-z0-9]).{8,}$
    const password = 'Password123!';
    await page.fill('input[formControlName="password"]', password);
    await page.fill('input[formControlName="confirmPassword"]', password);

    // Take screenshot of the filled form
    console.log('Capturing filled registration screen...');
    await page.screenshot({ path: 'e2e-screenshots/register-screen-filled.png' });

    // The register button might be disabled due to CAPTCHA
    const registerBtn = page.locator('#register-btn');

    // Diagnostic: Check site key from backend
    const siteKey = await page.evaluate(async () => {
      try {
        const response = await fetch('/api/system/recaptcha-site-key');
        return await response.text();
      } catch (e) {
        return 'error';
      }
    });
    console.log(`Diagnostic: Backend reCAPTCHA site key is "${siteKey}"`);

    const isDisabled = await registerBtn.isDisabled();

    if (isDisabled) {
      console.log('Register button is disabled (likely CAPTCHA required). Attempting to force enable for screenshot...');

      // Force enable button and inject dummy token
      await page.evaluate(() => {
        (window as any).BYPASS_RECAPTCHA = true;
        const btn = document.querySelector('#register-btn') as HTMLButtonElement;
        if (btn) btn.disabled = false;

        console.log('reCAPTCHA bypass enabled in window state');
      });

      console.log('Register button force enabled, attempting registration (Success Case)...');
      await registerBtn.click({ force: true });

      // DIAGNOSTIC: Check if button is really clickable or if we should call onSubmit
      await page.evaluate(() => {
        const btn = document.querySelector('#register-btn') as HTMLButtonElement;
        console.log('Button disabled state in DOM:', btn?.disabled);
        const form = document.querySelector('form');
        console.log('Manually submitting form...');
        if (form) form.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true }));
      });

      // Wait for success screen or error message
      console.log('Waiting for result message...');
      try {
        await Promise.race([
          page.waitForURL(/\/register\/success/, { timeout: 10000 }),
          page.waitForSelector('.error', { timeout: 10000 })
        ]);
      } catch (e) {
        console.log('Timeout waiting for result message. Taking screenshot of current state.');
      }

      const errorMsg = page.locator('.error');

      if (page.url().includes('/register/success')) {
        console.log('Capturing registration success screen...');
        await page.screenshot({ path: 'e2e-screenshots/register-success.png', fullPage: true });

        // Now attempt registration with the SAME login to get an error
        console.log('Attempting registration with duplicate login (Error Case)...');
        // Trigger submit again. Since it's the same component, 'BYPASS_RECAPTCHA' is still true in window.
        await page.evaluate(() => {
           const form = document.querySelector('form');
           if (form) form.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true }));
        });

        console.log('Waiting for error message...');
        await page.waitForSelector('.error', { timeout: 10000 });
        console.log('Capturing registration error message...');
        await errorMsg.scrollIntoViewIfNeeded();
        await page.screenshot({ path: 'e2e-screenshots/register-error.png', fullPage: true });
      } else if (await errorMsg.isVisible()) {
        console.log(`Registration result message found: ${await errorMsg.innerText()}`);
        await errorMsg.scrollIntoViewIfNeeded();
        await page.screenshot({ path: 'e2e-screenshots/register-error.png', fullPage: true });
      } else {
        await page.screenshot({ path: 'e2e-screenshots/register-result-fallback.png', fullPage: true });
      }
    } else {
      console.log('Register button is enabled, attempting registration (Success Case)...');
      await registerBtn.click();

      // Wait for success screen
      await page.waitForURL(/\/register\/success/, { timeout: 10000 });
      console.log('Capturing registration success screen...');
      await page.screenshot({ path: 'e2e-screenshots/register-success.png', fullPage: true });

      // Now attempt registration with the SAME login to get an error
      console.log('Attempting registration with duplicate login (Error Case)...');
    await page.fill('input[formControlName="fullName"]', 'Jane Doe');
    // login is already filled with the same unique login from before
    await page.fill('input[formControlName="email"]', 'jane.doe@example.com');
    await page.fill('input[formControlName="password"]', password);
    await page.fill('input[formControlName="confirmPassword"]', password);

      await registerBtn.click();

      // Wait for error message
      await page.waitForSelector('.error', { timeout: 10000 });
      console.log('Capturing registration error message...');
      await page.locator('.error').scrollIntoViewIfNeeded();
      await page.screenshot({ path: 'e2e-screenshots/register-error.png', fullPage: true });
    }
  });
});
