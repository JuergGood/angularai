<#
.SYNOPSIS
    Runs SonarCloud analysis locally.

.DESCRIPTION
    This script runs a full Maven verify followed by a SonarCloud scan.
    It includes coverage reports for backend (JaCoCo) and frontend (LCOV).
    Requirement: You must have a SONAR_TOKEN from SonarCloud.

.PARAMETER Token
    The SonarCloud token. If not provided, it will look for an environment variable SONAR_TOKEN.

.EXAMPLE
    .\scripts\sonar-analysis.ps1 -Token "your_secret_token"
#>

param (
    [Parameter(Mandatory=$false)]
    [string]$Token = $env:SONAR_TOKEN
)

if (-not $Token) {
    Write-Warning "SONAR_TOKEN is not set. Looking for environment variables..."
    $Token = $env:SONAR_TOKEN
}

if (-not $Token) {
    Write-Error "SONAR_TOKEN is not set. Please provide it via -Token parameter or set the SONAR_TOKEN environment variable in .env"
    exit 1
}

if ($Token -eq "dummy_sonar_token") {
    Write-Warning "SONAR_TOKEN is set to 'dummy_sonar_token'. SonarCloud analysis will fail because the token is invalid."
    Write-Warning "If you want to perform real analysis, please set a valid SONAR_TOKEN in your .env file."
}

Write-Host "Starting Local SonarCloud Analysis..." -ForegroundColor Cyan

Write-Host "Note: Ensure 'Automatic Analysis' is turned OFF in SonarCloud (Administration -> Analysis Method)" -ForegroundColor Gray

# 1. Frontend: Ensure coverage report is generated
Write-Host "Step 1: Running Frontend Tests with Coverage..." -ForegroundColor Yellow
Push-Location frontend
npm install --legacy-peer-deps
npm test
Pop-Location

# 2. Root: Run Maven verify and Sonar scan
Write-Host "Step 2: Running Maven Verify and Sonar Scan..." -ForegroundColor Yellow
$mvnArgs = @(
    "-B",
    "verify",
    "sonar:sonar",
    "-Dsonar.token=$Token",
    "-Dsonar.javascript.lcov.reportPaths=frontend/coverage/lcov.info",
    "-Dsonar.coverage.jacoco.xmlReportPaths=backend/target/site/jacoco/jacoco.xml,test-client/target/site/jacoco/jacoco.xml,android/app/build/reports/jacoco/testDebugUnitTest/testDebugUnitTest.xml"
)

# Execute Maven
& mvn $mvnArgs

if ($LASTEXITCODE -eq 0) {
    Write-Host "SonarCloud Analysis completed successfully!" -ForegroundColor Green
    Write-Host "Check your results at: https://sonarcloud.io/project/dashboard?id=JuergGood_angularai" -ForegroundColor Cyan
} else {
    Write-Error "SonarCloud Analysis failed with exit code $LASTEXITCODE"
}
