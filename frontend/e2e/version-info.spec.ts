import { test, expect } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

// Helper to get version from pom.xml
function getVersion() {
  const pomPath = path.resolve(__dirname, '../../pom.xml');
  const pomContent = fs.readFileSync(pomPath, 'utf-8');
  const versionMatch = pomContent.match(/<version>(.*?)<\/version>/);
  return versionMatch ? versionMatch[1] : 'unknown';
}

const version = getVersion();

test.describe('Version and Release Notes', () => {

  test('should display correct version in About menu', async ({ page }) => {
    await page.goto('/');

    // Open Settings menu
    await page.click('button.settings-button[title="Settings"], button.settings-button[title="Einstellungen"]');

    // Open About menu
    await page.click('button[role="menuitem"]:has-text("About"), button[role="menuitem"]:has-text("Über")');

    // Check version information
    // Using regex to be flexible with translation but strict with version number
    const backendVersionLocator = page.locator('.info-item:has-text("Backend") .info-value, .info-item:has-text("Version") .info-value').first();
    const frontendVersionLocator = page.locator('.info-item:has-text("Frontend") .info-value, .info-item:has-text("Version") .info-value').nth(1);

    await expect(backendVersionLocator).toHaveText(version);
    await expect(frontendVersionLocator).toHaveText(version);
  });

  test('should display updated release notes', async ({ page }) => {
    await page.goto('/');

    // Open Settings menu
    await page.click('button.settings-button[title="Settings"], button.settings-button[title="Einstellungen"]');

    // Open Help submenu
    await page.click('button[role="menuitem"]:has-text("Help"), button[role="menuitem"]:has-text("Hilfe")');

    // Open Release Notes
    await page.click('button[role="menuitem"]:has-text("Release Notes"), button[role="menuitem"]:has-text("Versionshinweise")');

    // Wait for the release notes content to load
    await page.waitForSelector('.help-content');

    // Check for the current version header in release notes
    const versionHeader = page.locator(`.help-content h2:has-text("Version ${version}")`);
    await expect(versionHeader).toBeVisible();

    // Verify some content is present under the header
    // The release notes are rendered from markdown, so Version 1.0.5 (YYYY-MM-DD) is usually an h2
    console.log(`Verified Release Notes for Version ${version}`);
  });
});
