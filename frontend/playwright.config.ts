import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:4200',
    trace: 'on-first-retry',
    screenshot: 'on',
    actionTimeout: 15000,
    navigationTimeout: 30000,
  },
  timeout: 60000,
  webServer: {
    command: 'npm start',
    url: 'http://localhost:4200',
    reuseExistingServer: !process.env.CI,
    stdout: 'pipe',
    stderr: 'pipe',
    timeout: 120000,
  },
  projects: [
    { name: 'setup', testMatch: /auth\.setup\.ts/ },
    {
      name: 'no-auth',
      testMatch: /verification-flow\.spec\.ts|registration-extensive\.spec\.ts|password-recovery\.spec\.ts|landing-message\.spec\.ts|registration-docs\.spec\.ts/,
      use: {
        ...devices['Desktop Chrome'],
        storageState: { cookies: [], origins: [] },
      },
    },
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'playwright/.auth/user.json',
      },
      dependencies: ['setup'],
    },
  ],
});
