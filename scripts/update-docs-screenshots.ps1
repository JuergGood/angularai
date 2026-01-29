# Update Documentation Screenshots
# This script regenerates the screenshots used in the documentation by running Playwright tests.

$frontendDir = Join-Path $PSScriptRoot "..\frontend"

if (-not (Test-Path $frontendDir)) {
    Write-Error "Frontend directory not found at $frontendDir"
    exit 1
}

Push-Location $frontendDir

Write-Host "Regenerating documentation screenshots..." -ForegroundColor Cyan

# Run Playwright tests for documentation
npx playwright test registration-docs forgot-password-docs --project=no-auth

if ($LASTEXITCODE -eq 0) {
    Write-Host "Screenshots updated successfully in doc/user-guide/workflows/assets/" -ForegroundColor Green
} else {
    Write-Error "Failed to update screenshots. Please check the Playwright output above."
}

Pop-Location
