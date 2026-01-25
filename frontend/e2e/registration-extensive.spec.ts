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

    await confirmInput.fill('DifferentPass123!');
    await confirmInput.blur();

    // The error is a form-level error, but Angular Material displays it in a mat-error
    // we should wait for it to be visible
    await expect(page.locator('mat-error')).toContainText('match');
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
    await expect(page.locator('h1')).toContainText('Account created');

    // Check "Go to login" CTA
    const loginCta = page.locator('a[routerLink="/login"]');
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

});
