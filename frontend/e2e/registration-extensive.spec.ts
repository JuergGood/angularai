import { test, expect } from '@playwright/test';

test.describe('Registration Extensive E2E Tests', () => {

  test.use({ storageState: { cookies: [], origins: [] } });

  test.beforeEach(async ({ page, context }) => {
    // Intercept recaptcha site key request and return "disabled"
    await page.route('**/api/system/recaptcha-site-key', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'text/plain',
        body: 'disabled'
      });
    });

    // Navigate to /register
    await page.goto('/register');

    // Ensure clean state (bypass recaptcha if needed)
    await page.evaluate(() => {
      (window as any).BYPASS_RECAPTCHA = true;
    });
  });

  test('should display validation errors for required fields', async ({ page }) => {
    const fullNameInput = page.locator('input[formControlName="fullName"]');
    const loginInput = page.locator('input[formControlName="login"]');
    const emailInput = page.locator('input[formControlName="email"]');
    const passwordInput = page.locator('input[formControlName="password"]');
    const confirmPasswordInput = page.locator('input[formControlName="confirmPassword"]');

    // Touch fields to trigger validation
    await fullNameInput.focus();
    await loginInput.focus();
    await emailInput.focus();
    await passwordInput.focus();
    await confirmPasswordInput.focus();
    await fullNameInput.focus(); // focus back to trigger blur on last field

    // Check for error messages
    const errors = page.locator('mat-error');
    await expect(errors).toHaveCount(5);
    for (let i = 0; i < 5; i++) {
        await expect(errors.nth(i)).toBeVisible();
    }

    // Submit button should be disabled
    await expect(page.locator('#register-btn')).toBeDisabled();
  });

  test('should validate full name format', async ({ page }) => {
    const fullNameInput = page.locator('input[formControlName="fullName"]');

    await fullNameInput.fill('John');
    await fullNameInput.blur();

    await expect(page.locator('mat-error')).toContainText(/both first and last name|Vor- und Nachname/i);
    await expect(page.locator('#register-btn')).toBeDisabled();

    await fullNameInput.fill('John Doe');
    await fullNameInput.blur();
    await expect(page.locator('mat-error').filter({ hasText: /both first and last name|Vor- und Nachname/i })).not.toBeVisible();
  });

  test('should validate login has no spaces', async ({ page }) => {
    const loginInput = page.locator('input[formControlName="login"]');

    await loginInput.fill('user name');
    await loginInput.blur();

    await expect(page.locator('mat-error')).toContainText(/not contain spaces|keine Leerzeichen/i);
    await expect(page.locator('#register-btn')).toBeDisabled();

    await loginInput.fill('username');
    await loginInput.blur();
    await expect(page.locator('mat-error').filter({ hasText: /not contain spaces|keine Leerzeichen/i })).not.toBeVisible();
  });

  test('should validate email format', async ({ page }) => {
    const emailInput = page.locator('input[formControlName="email"]');

    await emailInput.fill('invalid-email');
    await emailInput.blur();

    await expect(page.locator('mat-error')).toContainText('valid email');
    await expect(page.locator('#register-btn')).toBeDisabled();
  });

  test('should validate password strength', async ({ page }) => {
    const passwordInput = page.locator('input[formControlName="password"]');

    // Too short
    await passwordInput.fill('Short1!');
    await passwordInput.blur();
    await expect(page.locator('mat-error')).toContainText('meet requirements');

    // No special char
    await passwordInput.fill('NoSpecialChar123');
    await passwordInput.blur();
    await expect(page.locator('mat-error')).toContainText('meet requirements');

    // Valid
    await passwordInput.fill('ValidPass123!');
    await passwordInput.blur();
    // Hint should still be there but not error
    await expect(page.locator('mat-error:has-text("meet requirements")')).not.toBeVisible();
  });

  test('should validate password matching', async ({ page }) => {
    await page.locator('input[formControlName="password"]').fill('ValidPass123!');
    const confirmInput = page.locator('input[formControlName="confirmPassword"]');

    // Fill something different
    await confirmInput.fill('DifferentPass123!');

    // Blur to trigger validation. Angular Material often needs a blur to show errors.
    await confirmInput.blur();

    // Sometimes we need to click or focus elsewhere to ensure the 'touched' state is acknowledged by the UI
    await page.locator('input[formControlName="fullName"]').focus();

    // Check for the error message
    // "Passwords do not match" in English, "Passwörter stimmen nicht überein" in German
    const error = page.locator('mat-error').filter({ hasText: /match|stimmen/i });
    await expect(error).toBeVisible();
    await expect(page.locator('#register-btn')).toBeDisabled();
  });

  test('should toggle password visibility', async ({ page }) => {
    const passwordInput = page.locator('input[formControlName="password"]');
    const toggleBtn = page.locator('button[aria-label="Toggle password visibility"]').first();

    await passwordInput.fill('Secret123!');
    await expect(passwordInput).toHaveAttribute('type', 'password');

    await toggleBtn.click();
    await expect(passwordInput).toHaveAttribute('type', 'text');

    await toggleBtn.click();
    await expect(passwordInput).toHaveAttribute('type', 'password');
  });

  test('should parse full name correctly during registration', async ({ page }) => {
    const uniqueId = Date.now();
    const login = `user_${uniqueId}`;
    const email = `user_${uniqueId}@example.com`;

    // Mock successful registration response to avoid hitting real backend if it's not up
    // However, if we want to test parsing, we can just observe what's sent in the request if possible
    // or just assume it works if it reaches the success screen.

    await page.locator('input[formControlName="fullName"]').fill('   Hans Peter   Müller   ');
    await page.locator('input[formControlName="login"]').fill(login);
    await page.locator('input[formControlName="email"]').fill(email);
    await page.locator('input[formControlName="password"]').fill('ValidPass123!');
    await page.locator('input[formControlName="confirmPassword"]').fill('ValidPass123!');

    // The button should be enabled now
    const registerBtn = page.locator('#register-btn');
    await expect(registerBtn).toBeEnabled();

    // Intercept the registration request to verify the parsed name
    await page.route('**/api/auth/register', async route => {
      const request = route.request();
      const postData = JSON.parse(request.postData() || '{}');

      // Verify parsing logic (should be done by the component before sending)
      expect(postData.firstName).toBe('Hans Peter');
      expect(postData.lastName).toBe('Müller');

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ message: 'User registered successfully' })
      });
    });

    await registerBtn.click();

    // Should navigate to success screen
    await expect(page).toHaveURL(/\/register\/success/);
    await expect(page.locator('h1')).toContainText(/Account created|Konto erstellt/i);

    // Take screenshot of the success screen
    await page.screenshot({ path: 'e2e-screenshots/register-success.png', fullPage: true });

    // Check "Go to login" CTA
    const loginCta = page.locator('.success-card').getByRole('button', { name: /Login|Anmelden/i });
    await expect(loginCta).toBeVisible();
    await loginCta.click();
    await expect(page).toHaveURL(/\/login/);
  });

  test('should handle registration errors from backend', async ({ page }) => {
    await page.locator('input[formControlName="fullName"]').fill('John Doe');
    await page.locator('input[formControlName="login"]').fill('existinguser');
    await page.locator('input[formControlName="email"]').fill('existing@example.com');
    await page.locator('input[formControlName="password"]').fill('ValidPass123!');
    await page.locator('input[formControlName="confirmPassword"]').fill('ValidPass123!');

    // Mock error response
    await page.route('**/api/auth/register', async route => {
      await route.fulfill({
        status: 400,
        contentType: 'text/plain',
        body: 'User already exists'
      });
    });

    await page.locator('#register-btn').click();

    // Should show error message
    const errorMsg = page.locator('.error');
    await expect(errorMsg).toBeVisible();
    await expect(errorMsg).toContainText('User already exists'); // Or the translated version if i18n is mocked or loaded
  });

  test('should allow re-registration with same email if user is pending', async ({ page }) => {
    const uniqueId = Date.now();
    const login = `retryuser_${uniqueId}`;
    const email = `retry_${uniqueId}@example.com`;

    // 1. Initial registration (Mock success)
    await page.locator('input[formControlName="fullName"]').fill('First Attempt');
    await page.locator('input[formControlName="login"]').fill(login);
    await page.locator('input[formControlName="email"]').fill(email);
    await page.locator('input[formControlName="password"]').fill('ValidPass123!');
    await page.locator('input[formControlName="confirmPassword"]').fill('ValidPass123!');

    await page.route('**/api/auth/register', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ login, email, status: 'PENDING' })
      });
    }, { times: 1 });

    await page.locator('#register-btn').click();
    await expect(page).toHaveURL(/\/register\/success/);

    // 2. Go back to register and try again with SAME email/login
    await page.goto('/register');
    await page.evaluate(() => {
      (window as any).BYPASS_RECAPTCHA = true;
    });

    await page.locator('input[formControlName="fullName"]').fill('Second Attempt');
    await page.locator('input[formControlName="login"]').fill(login);
    await page.locator('input[formControlName="email"]').fill(email);
    await page.locator('input[formControlName="password"]').fill('ValidPass123!');
    await page.locator('input[formControlName="confirmPassword"]').fill('ValidPass123!');

    // Mock backend allowing it (because it was PENDING)
    await page.route('**/api/auth/register', async route => {
        const request = route.request();
        const postData = JSON.parse(request.postData() || '{}');
        expect(postData.firstName).toBe('Second');
        expect(postData.lastName).toBe('Attempt');

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ login, email, status: 'PENDING' })
        });
      });

    await page.locator('#register-btn').click();
    await expect(page).toHaveURL(/\/register\/success/);
    await expect(page.locator('h1')).toContainText(/Account created|Konto erstellt/i);
  });

});
