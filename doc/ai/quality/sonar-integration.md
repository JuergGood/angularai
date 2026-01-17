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

## Step 1: Create a SonarCloud Token

1. Open SonarCloud
2. Go to **My Account → Security**
3. Generate a new token
4. Copy the token (it will be used as the password in API calls)

> ⚠️ Store the token securely. Do not commit it to git.

---

## Step 2: Call the SonarCloud Issues API

SonarCloud does not provide a UI download button, but all issues are accessible via the API.

### API Endpoint

```powershell
curl.exe -k -u "YOUR_TOKEN:" "https://sonarcloud.io/api/issues/search?componentKeys=JuergGood_angularai&statuses=OPEN,CONFIRMED&ps=500" -o sonar-issues.json
```

---

## Step 3: Run Local Sonar Analysis

To run a full SonarCloud analysis locally and see the results on the dashboard before pushing:

### Option A: Using the Helper Script (Recommended)
We have provided a PowerShell script that handles the build, tests, and analysis:

```powershell
.\scripts\sonar-analysis.ps1 -Token "YOUR_SONAR_TOKEN"
```

### Option B: Manual Maven Command
Run the following command from the project root:

```powershell
mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar `
  -Dsonar.token="YOUR_SONAR_TOKEN" `
  -Dsonar.projectKey=JuergGood_angularai `
  -Dsonar.organization=juerggood `
  -Dsonar.javascript.lcov.reportPaths=frontend/coverage/lcov.info `
  -Dsonar.coverage.jacoco.xmlReportPaths=backend/target/site/jacoco/jacoco.xml
```

> **Note**: Ensure you run `npm test` in the `frontend` folder first to generate the `lcov.info` file if you want frontend coverage in the report.

