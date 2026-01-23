# Export SonarCloud Issues for Junie (JSON via Web API)

This guide explains how to export SonarCloud issues into a **machine-readable JSON file**
that can be consumed by **Junie** for automated fixes.

The approach uses the official **SonarCloud Web API** and works reliably for Angular,
TypeScript, Java, and other supported languages.

---

## Prerequisites

- A SonarCloud account
- Access to the project: `JuergGood_angularai`
- `curl` installed
- A SonarCloud **user token**

---

## Step 1: Set up the SonarCloud Token in `.env`

1. Open SonarCloud
2. Go to **My Account → Security**
3. Generate a new token
4. Copy the token
5. Open the `.env` file in the project root and add or update the `SONAR_TOKEN` variable:

```text
SONAR_TOKEN=your_actual_token_here
```

> ⚠️ The `.env` file contains sensitive information. Do not commit it to git (it should be in `.gitignore`).

If you want to use it in your current terminal session:

**PowerShell:**
```powershell
# Load from .env (simple way)
Get-Content .env | Foreach-Object {
    $name, $value = $_.Split('=', 2)
    if ($name -eq "SONAR_TOKEN") { [System.Environment]::SetEnvironmentVariable($name, $value) }
}
# Or set manually
$env:SONAR_TOKEN="your_actual_token_here"
```

**Bash:**
```bash
# Load from .env
export $(grep SONAR_TOKEN .env | xargs)
# Or set manually
export SONAR_TOKEN="your_actual_token_here"
```


---



## Step 2: Call the SonarCloud Issues API

SonarCloud does not provide a UI download button, but all issues are accessible via the API.

### API Endpoint

**PowerShell:**
```powershell
curl.exe -k -u "${env:SONAR_TOKEN}:" "https://sonarcloud.io/api/issues/search?componentKeys=JuergGood_angularai&statuses=OPEN,CONFIRMED&ps=500" -o sonar-issues.json
```

**Bash:**
```bash
curl -k -u "$SONAR_TOKEN:" "https://sonarcloud.io/api/issues/search?componentKeys=JuergGood_angularai&statuses=OPEN,CONFIRMED&ps=500" -o sonar-issues.json
```

---

## Step 3: Run Local Sonar Analysis

> ⚠️ **Important**: To run a manual analysis (local or via CI), you must **disable "Automatic Analysis"** in SonarCloud.
> 1. Go to your project in SonarCloud.
> 2. Go to **Administration -> Analysis Method**.
> 3. Turn **OFF** "SonarCloud Automatic Analysis".
>
> If this is ON, any manual scan attempt will fail with the error: *"You are running manual analysis while Automatic Analysis is enabled"*.

To run a full SonarCloud analysis locally and see the results on the dashboard before pushing:

### Option A: Using the Helper Script (Recommended)
We have provided scripts that handle the build, tests, and analysis. They automatically use the `SONAR_TOKEN` from the environment.

**PowerShell:**
```powershell
# If you haven't loaded .env yet, you can do it for the current session:
Get-Content .env | Foreach-Object { $name, $value = $_.Split('=', 2); [System.Environment]::SetEnvironmentVariable($name, $value) }

.\scripts\sonar-analysis.ps1
```

**Bash:**
```bash
# Ensure the script is executable
chmod +x ./scripts/sonar-analysis.sh

# If you haven't loaded .env yet, you can do it for the current session:
# Note: This is a simple way that doesn't handle spaces or special characters in values perfectly
export $(grep -v '^#' .env | xargs)

# Run the script
./scripts/sonar-analysis.sh
```

> **Note**: You can also pass the token directly: `.\scripts\sonar-analysis.ps1 -Token "your_token"` (PowerShell) or `./scripts/sonar-analysis.sh your_token` (Bash).

### Option B: Manual Maven Command

**PowerShell:**
```powershell
mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar `
  -Dsonar.token="$env:SONAR_TOKEN" `
  -Dsonar.projectKey=JuergGood_angularai `
  -Dsonar.organization=juerggood `
  -Dsonar.javascript.lcov.reportPaths=frontend/coverage/lcov.info `
  -Dsonar.coverage.jacoco.xmlReportPaths=backend/target/site/jacoco/jacoco.xml
```

**Bash:**
```bash
mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.token="$SONAR_TOKEN" \
  -Dsonar.projectKey=JuergGood_angularai \
  -Dsonar.organization=juerggood \
  -Dsonar.javascript.lcov.reportPaths=frontend/coverage/lcov.info \
  -Dsonar.coverage.jacoco.xmlReportPaths=backend/target/site/jacoco/jacoco.xml
```

> **Note**: Ensure you run `npm test` in the `frontend` folder first to generate the `lcov.info` file if you want frontend coverage in the report.

