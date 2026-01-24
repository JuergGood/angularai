import { test, expect } from '@playwright/test';

test.describe('Auth Flow (Login & Register)', () => {

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
    console.log('Navigating to /register...');
    await page.goto('/register');

    // Wait for the register card
    await page.waitForSelector('.register-card');

    // Take screenshot of the empty registration form
    console.log('Capturing empty registration screen...');
    await page.screenshot({ path: 'e2e-screenshots/register-screen-empty.png' });

    // Fill out the registration form
    console.log('Filling registration form...');
    await page.fill('input[name="firstName"]', 'John');
    await page.fill('input[name="lastName"]', 'Doe');
    await page.fill('input[name="login"]', 'johndoe' + Date.now()); // Unique login
    await page.fill('input[name="email"]', 'john.doe@example.com');

    // Password must match pattern: ^(?=.*[A-Za-z])(?=.*[^A-Za-z0-9]).{8,}$
    const password = 'Password123!';
    await page.fill('input[name="password"]', password);
    await page.fill('input[name="confirmPassword"]', password);

    // Take screenshot of the filled form
    console.log('Capturing filled registration screen...');
    await page.screenshot({ path: 'e2e-screenshots/register-screen-filled.png' });

    // The register button might be disabled due to CAPTCHA
    const registerBtn = page.locator('#register-btn');
    const isDisabled = await registerBtn.isDisabled();

    if (isDisabled) {
      console.log('Register button is disabled (likely CAPTCHA required).');
      // Capture the state with the disabled button
      await page.screenshot({ path: 'e2e-screenshots/register-screen-captcha-required.png' });
    } else {
      console.log('Register button is enabled, attempting registration...');
      await registerBtn.click();

      // Wait a bit for the response/message
      await page.waitForTimeout(2000);

      // Capture the result
      await page.screenshot({ path: 'e2e-screenshots/register-result.png' });

      // Check for success or error message
      const successMsg = page.locator('.success');
      const errorMsg = page.locator('.error');

      if (await successMsg.isVisible()) {
        console.log('Registration successful!');
      } else if (await errorMsg.isVisible()) {
        const errorText = await errorMsg.innerText();
        console.log(`Registration failed: ${errorText}`);
      }
    }
  });
});
