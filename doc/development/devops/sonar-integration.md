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

### Linux / macOS (Bash/Zsh)
```bash
# Load from .env
export $(grep SONAR_TOKEN .env | xargs)
```

### Linux / macOS (Manual)
```bash
# Set manually
export SONAR_TOKEN="your_actual_token_here"
```

### Windows (PowerShell - Load from .env)
```bash
# Load from .env
Get-Content .env | Foreach-Object { if ($_ -match "SONAR_TOKEN=(.*)") { $env:SONAR_TOKEN = $matches[1].Trim() } }
```

### Windows (PowerShell - Manual)
```bash
# Set manually
$env:SONAR_TOKEN = "your_actual_token_here"
```


---



## Step 2: Call the SonarCloud Issues API

SonarCloud does not provide a UI download button, but all issues are accessible via the API.

### API Endpoint

#### Linux / macOS
```bash
curl -k -u "$SONAR_TOKEN:" "https://sonarcloud.io/api/issues/search?componentKeys=JuergGood_angularai&statuses=OPEN,CONFIRMED&ps=500" -o sonar-issues.json
```

#### Windows (PowerShell)
```bash
# Ensure you are in the project root
curl.exe -k -u "${env:SONAR_TOKEN}:" https://sonarcloud.io/api/issues/search?componentKeys=JuergGood_angularai&statuses=OPEN,CONFIRMED&ps=500 -o sonar-issues.json
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

#### Linux / macOS
```bash
# Ensure the script is executable
chmod +x ./scripts/sonar-analysis.sh

# If you haven't loaded .env yet, you can do it for the current session:
export $(grep -v '^#' .env | xargs)

# Run the script
./scripts/sonar-analysis.sh
```

#### Windows (PowerShell)
```bash
# Load all variables from .env
Get-Content .env | Where-Object { $_ -and -not $_.StartsWith("#") } | ForEach-Object {
    if ($_ -match "(.+?)=(.*)") {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}
```

```bash
# Run the analysis (requires bash environment like Git Bash or WSL if running .sh)
./scripts/sonar-analysis.sh
```

> **Note**: You can also pass the token directly: `./scripts/sonar-analysis.sh your_token`.

### Option B: Manual Maven Command

```bash
# Ensure SONAR_TOKEN is in your environment
mvn verify sonar:sonar -Dsonar.token="$SONAR_TOKEN"
```

**Windows (PowerShell) equivalent:**

```bash
# 1. Ensure JDK 21 is used (required for JaCoCo compatibility)
$env:JAVA_HOME = "C:\programs\java\jdk-21.0.10"
$env:Path = "$($env:JAVA_HOME)\bin;" + $env:Path
```

```bash
# 2. Run the analysis (Ensure you are in the project root)
# Using -D"key=value" is the most robust way for PowerShell + IDE runners
cd C:\doc\sw\ai\angularai\angularai
mvn verify sonar:sonar -DskipTests -D"sonar.token=$env:SONAR_TOKEN"
```

> **Note**: Ensure you run `npm test` in the `frontend` folder first to generate the `lcov.info` file if you want frontend coverage in the report.

