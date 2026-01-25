import { test, expect } from '@playwright/test';

test.describe('Verification Flow E2E Tests', () => {

  test.use({ storageState: { cookies: [], origins: [] } });

  test('should display verification success page and navigate to login', async ({ page }) => {
    // Navigate directly to /verify/success
    await page.goto('/verify/success');

    // Check for success title
    await expect(page.locator('h1')).toContainText(/Email confirmed|E-Mail-Adresse bestätigt/i);
    await expect(page.locator('.large-icon')).toHaveText('check_circle');

    // Check "Go to login" CTA
    const loginCta = page.locator('.verify-card').getByRole('button', { name: /Login|Anmelden/i });
    await expect(loginCta).toBeVisible();

    // Take screenshot
    await page.screenshot({ path: 'e2e-screenshots/verify-success.png', fullPage: true });

    // Click CTA and verify navigation
    await loginCta.click();
    await expect(page).toHaveURL(/\/login/);
  });

  test('should display verification error page (invalid token)', async ({ page }) => {
    // Navigate to /verify/error without reason
    await page.goto('/verify/error');

    // Check for error title
    await expect(page.locator('h1')).toContainText(/Verification failed|Verifizierung fehlgeschlagen/i);
    await expect(page.locator('.large-icon')).toHaveText('error_outline');

    // Default text should be "invalid"
    await expect(page.locator('.subtitle')).toContainText(/link is invalid|Bestätigungslink ist ungültig/i);

    // Resend button should NOT be visible if no email is provided
    await expect(page.locator('.resend-btn')).not.toBeVisible();

    // Take screenshot
    await page.screenshot({ path: 'e2e-screenshots/verify-error-invalid.png', fullPage: true });
  });

  test('should display verification error page (expired token) and handle resend', async ({ page }) => {
    const testEmail = 'test@example.com';
    // Navigate to /verify/error with reason=expired and email
    await page.goto(`/verify/error?reason=expired&email=${testEmail}`);

    // Check for expired text
    await expect(page.locator('.subtitle')).toContainText(/link has expired|Bestätigungslink ist abgelaufen/i);

    // Resend button should be visible
    const resendBtn = page.locator('.resend-btn');
    await expect(resendBtn).toBeVisible();

    // Mock successful resend request
    await page.route('**/api/auth/resend-verification*', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'text/plain',
        body: 'Verification email sent'
      });
    });

    // Click resend and verify feedback
    await resendBtn.click();

    const feedback = page.locator('.feedback.success');
    await expect(feedback).toBeVisible();
    await expect(feedback).toContainText(/sent|gesendet/i);

    // Take screenshot with feedback
    await page.screenshot({ path: 'e2e-screenshots/verify-error-resend-success.png', fullPage: true });
  });

  test('should handle resend error', async ({ page }) => {
    const testEmail = 'error@example.com';
    await page.goto(`/verify/error?reason=expired&email=${testEmail}`);

    const resendBtn = page.locator('.resend-btn');

    // Mock failed resend request
    await page.route('**/api/auth/resend-verification*', async route => {
      await route.fulfill({
        status: 500,
        contentType: 'text/plain',
        body: 'Failed to send email'
      });
    });

    await resendBtn.click();

    const feedback = page.locator('.feedback.error');
    await expect(feedback).toBeVisible();
    await expect(feedback).toContainText(/Failed|Fehler/i);
  });

  test('should simulate full verification redirect from backend', async ({ page }) => {
    // 1. Mock valid token redirect
    await page.route('**/api/auth/verify?token=valid-token', async route => {
      await route.fulfill({
        status: 302,
        headers: {
          'Location': '/verify/success'
        }
      });
    });

    // Navigate to backend verify endpoint
    await page.goto('/api/auth/verify?token=valid-token');

    // Should follow redirect and end up on success page
    await expect(page).toHaveURL(/\/verify\/success/);
    await expect(page.locator('h1')).toContainText(/Email confirmed|E-Mail-Adresse bestätigt/i);

    // 2. Mock invalid token redirect
    await page.route('**/api/auth/verify?token=invalid-token', async route => {
        await route.fulfill({
          status: 302,
          headers: {
            'Location': '/verify/error?reason=invalid'
          }
        });
      });

      await page.goto('/api/auth/verify?token=invalid-token');

      // Should follow redirect and end up on error page
      await expect(page).toHaveURL(/\/verify\/error\?reason=invalid/);
      await expect(page.locator('h1')).toContainText(/Verification failed|Verifizierung fehlgeschlagen/i);
  });

  test('should handle redirect to error with expired token and email', async ({ page }) => {
    const testEmail = 'expired@example.com';
    await page.route('**/api/auth/verify?token=expired-token', async route => {
      await route.fulfill({
        status: 302,
        headers: {
          'Location': `/verify/error?reason=expired&email=${testEmail}`
        }
      });
    });

    await page.goto('/api/auth/verify?token=expired-token');

    await expect(page).toHaveURL(new RegExp(`/verify/error.*reason=expired.*email=${testEmail}`));
    await expect(page.locator('.subtitle')).toContainText(/link has expired|Bestätigungslink ist abgelaufen/i);
    await expect(page.locator('.resend-btn')).toBeVisible();
  });

});
