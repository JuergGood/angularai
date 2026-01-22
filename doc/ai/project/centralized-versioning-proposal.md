# Proposal: Centralized Version Management

## 1. Problem Statement
Currently, the project version (e.g., `1.0.3`) is hardcoded in over 70 locations across multiple file types:
- Maven `pom.xml` files (4 locations)
- Frontend `package.json` (1 location)
- Deployment scripts `deploy-aws.ps1` (1 location)
- AWS ECS Task Definitions (2 locations)
- Documentation `.md` files (Multiple locations in `doc/ai/aws/`, etc.)
- Android App `build.gradle` (1 location)

This makes version updates error-prone and tedious.

## 2. Proposed Solution
We propose making the **Root `pom.xml`** the single source of truth for the project version. All other files will be synchronized based on this value.

### 2.1 Implementation Strategy

#### A. Maven Modules
Update child modules to inherit the version from the parent without restating it.
- **Root `pom.xml`**: Keep `<version>1.x.y</version>`.
- **Sub-modules (`backend`, `frontend`, `test-client`)**: Remove the redundant `<version>` tag from their `pom.xml`.

#### B. Synchronization Script (`scripts/sync-version.ps1`)
Create a PowerShell script that performs the following:
1.  **Read** the current version from the root `pom.xml`.
2.  **Update** `frontend/package.json`.
3.  **Update** `scripts/deploy-aws.ps1` (`$VERSION = "..."`).
4.  **Update** AWS Task Definitions (`deploy/aws/*.json`).
5.  **Update** Documentation files in `doc/ai/` using regex to replace version patterns.
6.  **Update** `frontend/.gitignore` (specifically the `.jar` exclusion).
7.  **Update** `android/app/build.gradle` (`versionName`).

#### C. Optional: Build-time Synchronization
For files needed during the build (like `package.json`), we can use the `exec-maven-plugin` in the root POM to trigger the sync script automatically during the `initialize` phase.

## 3. Workflow for Developers
1.  Open the root `pom.xml`.
2.  Update the `<version>` value.
3.  Run `.\scripts\sync-version.ps1` (or let Maven do it during build).
4.  Commit the changes.

## 4. Benefits
- **Consistency**: No more version mismatches between backend, frontend, and deployment tags.
- **Efficiency**: Version updates take seconds instead of minutes.
- **Reliability**: Automation reduces the risk of forgetting a file.

## 5. Script Preview (Concept)
```powershell
# scripts/sync-version.ps1
[xml]$pom = Get-Content "pom.xml"
$version = $pom.project.version

Write-Host "Syncing version $version across project..." -ForegroundColor Cyan

# 1. Update package.json
$packageJson = Get-Content "frontend/package.json" -Raw | ConvertFrom-Json
$packageJson.version = $version
$packageJson | ConvertTo-Json -Depth 10 | Set-Content "frontend/package.json"

# 2. Update deploy-aws.ps1
(Get-Content "scripts/deploy-aws.ps1") -replace '\$VERSION = ".*"', "`$VERSION = `"$version`"" | Set-Content "scripts/deploy-aws.ps1"

# 3. Update Markdown Docs (Regex based)
Get-ChildItem -Path "doc/ai/" -Filter "*.md" -Recurse | ForEach-Object {
    (Get-Content $_.FullName) -replace '1\.0\.3', $version | Set-Content $_.FullName
}

# 4. Update Android build.gradle
(Get-Content "android/app/build.gradle") -replace 'versionName ".*"', "versionName `"$version`"" | Set-Content "android/app/build.gradle"
```
