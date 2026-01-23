param (
    [Parameter(Mandatory=$true)]
    [string]$NewVersion,

    [Parameter(Mandatory=$false)]
    [string]$ReleaseDate = (Get-Date -Format "yyyy-MM-dd")
)

$ErrorActionPreference = "Stop"

Write-Host "Starting release process for version $NewVersion..." -ForegroundColor Cyan

# 1. Update version in root pom.xml
Write-Host "Updating root pom.xml version to $NewVersion..."
mvn versions:set -DnewVersion=$NewVersion -DgenerateBackupPoms=false

# 2. Sync version to all other files
Write-Host "Syncing version across project..."
.\scripts\sync-version.ps1

# 3. Update Release Notes (Add a placeholder for the new version if it doesn't exist)
$releaseNotesPath = "doc/userguide/release-notes.md"
if (Test-Path $releaseNotesPath) {
    $content = Get-Content $releaseNotesPath -Raw
    $versionHeader = "## Version $NewVersion ($ReleaseDate)"
    
    if ($content -notmatch [regex]::Escape($versionHeader)) {
        Write-Host "Adding version header to $releaseNotesPath..."
        $newHeader = "# Release Notes`n`n$versionHeader`n*   New release version $NewVersion.`n"
        $content = $content -replace "^# Release Notes", $newHeader
        $content | Set-Content $releaseNotesPath
    }
}

# 4. Regenerate Help Pages
Write-Host "Regenerating help data..."
python .\scripts\generate_help_pages.py

# 5. Git Commit and Tag
Write-Host "Committing changes and creating git tag v$NewVersion..."
git add .
git commit -m "Release version $NewVersion"
git tag -a "v$NewVersion" -m "Release version $NewVersion"

Write-Host "Release $NewVersion completed successfully!" -ForegroundColor Green
Write-Host "Don't forget to push: git push origin main --tags" -ForegroundColor Yellow
