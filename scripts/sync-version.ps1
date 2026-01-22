# Project-wide Version Synchronization Script
# This script reads the version from the root pom.xml and propagates it to all other relevant files.

$pomPath = "pom.xml"
if (-not (Test-Path $pomPath)) {
    Write-Error "Root pom.xml not found. Please run this script from the project root."
    exit 1
}

# 1. Read version from root pom.xml
[xml]$pom = Get-Content $pomPath
$version = $pom.project.version
if (-not $version) {
    Write-Error "Could not find version in root pom.xml"
    exit 1
}

Write-Host "Synchronizing version $version across the project..." -ForegroundColor Cyan

# 2. Update frontend/package.json
$packageJsonPath = "frontend/package.json"
if (Test-Path $packageJsonPath) {
    Write-Host "Updating $packageJsonPath"
    $packageJson = Get-Content $packageJsonPath -Raw | ConvertFrom-Json
    $packageJson.version = $version
    $packageJson | ConvertTo-Json -Depth 10 | Set-Content $packageJsonPath
}

# 3. Update android/app/build.gradle (versionName)
$androidBuildPath = "android/app/build.gradle"
if (Test-Path $androidBuildPath) {
    Write-Host "Updating $androidBuildPath"
    $content = Get-Content $androidBuildPath
    $content = $content -replace 'versionName ".*"', "versionName `"$version`""
    $content | Set-Content $androidBuildPath
}

# 4. Update scripts/deploy-aws.ps1 ($VERSION)
$deployAwsPath = "scripts/deploy-aws.ps1"
if (Test-Path $deployAwsPath) {
    Write-Host "Updating $deployAwsPath"
    $content = Get-Content $deployAwsPath
    $content = $content -replace '\$VERSION = ".*"', "`$VERSION = `"$version`""
    $content | Set-Content $deployAwsPath
}

# 5. Update AWS Task Definitions
Get-ChildItem -Path "deploy/aws" -Filter "*.json" | ForEach-Object {
    Write-Host "Updating $($_.FullName)"
    $json = Get-Content $_.FullName -Raw | ConvertFrom-Json
    
    # Update image tags in container definitions
    if ($json.containerDefinitions) {
        foreach ($container in $json.containerDefinitions) {
            if ($container.image -match ":.*") {
                $container.image = $container.image -replace ":[^:]*$", ":$version"
            } else {
                $container.image = "$($container.image):$version"
            }
        }
    }
    
    $json | ConvertTo-Json -Depth 10 | Set-Content $_.FullName
}

# 6. Update frontend/.gitignore
$frontendGitignorePath = "frontend/.gitignore"
if (Test-Path $frontendGitignorePath) {
    Write-Host "Updating $frontendGitignorePath"
    $content = Get-Content $frontendGitignorePath
    $content = $content -replace '/target/aifrontend-.*\.jar', "/target/aifrontend-$version.jar"
    $content | Set-Content $frontendGitignorePath
}

# 7. Update Markdown Documentation (Regex based replacement for hardcoded version strings)
# Note: This specifically targets common version patterns in docs
Write-Host "Updating documentation in doc/ai/..."
Get-ChildItem -Path "doc/ai" -Filter "*.md" -Recurse | ForEach-Object {
    $content = Get-Content $_.FullName
    # Replace '1.0.3' with current version, but avoid partial matches like '21.0.3'
    $newContent = $content -replace '(?<!\d)1\.0\.3(?!\d)', $version
    if ($content -join "`n" -ne ($newContent -join "`n")) {
        $newContent | Set-Content $_.FullName
    }
}

# 8. Update specific test files if they contain hardcoded version mocks
$sidenavSpecPath = "frontend/src/app/components/layout/sidenav.component.spec.ts"
if (Test-Path $sidenavSpecPath) {
    Write-Host "Updating $sidenavSpecPath"
    $content = Get-Content $sidenavSpecPath
    $content = $content -replace "backendVersion: '1\.0\.3'", "backendVersion: '$version'"
    $content = $content -replace "frontendVersion: '1\.0\.3'", "frontendVersion: '$version'"
    $content | Set-Content $sidenavSpecPath
}

# 9. Update test-client documentation
$testClientDocPath = "doc/ai/testclient/test-client.md"
if (Test-Path $testClientDocPath) {
    Write-Host "Updating $testClientDocPath"
    $content = Get-Content $testClientDocPath
    $content = $content -replace "test-client-.*\.jar", "test-client-$version.jar"
    $content | Set-Content $testClientDocPath
}

Write-Host "Synchronization complete!" -ForegroundColor Green
