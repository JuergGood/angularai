# Export Snyk results to SARIF format for Junie analysis
# Requires Snyk CLI to be installed and authenticated

$ErrorActionPreference = "Stop"

# Create target directory if it doesn't exist
$OutputDir = "tmp/snyk"
if (!(Test-Path $OutputDir)) {
    New-Item -Path $OutputDir -ItemType Directory -Force | Out-Null
}

Write-Host "--- Scanning Backend (Maven) ---" -ForegroundColor Cyan
try {
    # We use continue-on-error behavior because snyk test returns non-zero if vulnerabilities are found
    snyk test --sarif > "$OutputDir/snyk-backend.sarif.json"
} catch {
    Write-Host "Snyk scan finished with findings or errors. Results saved to $OutputDir/snyk-backend.sarif.json" -ForegroundColor Yellow
}

Write-Host "--- Scanning Frontend (npm) ---" -ForegroundColor Cyan
try {
    Push-Location frontend
    snyk test --sarif > "../$OutputDir/snyk-frontend.sarif.json"
    Pop-Location
} catch {
    Write-Host "Snyk scan finished with findings or errors. Results saved to $OutputDir/snyk-frontend.sarif.json" -ForegroundColor Yellow
}

Write-Host "--- Done ---" -ForegroundColor Green
Write-Host "You can now tell Junie: 'I have uploaded the Snyk results to $OutputDir. Please assess and fix them.'"
