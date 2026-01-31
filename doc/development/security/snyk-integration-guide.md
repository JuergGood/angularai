# Snyk Integration Guide

This guide explains how to properly integrate Snyk for security scanning in the AngularAI project's CI/CD pipeline.

## 1. Obtain a Snyk API Token

To run Snyk in GitHub Actions, you need an API token from your Snyk account.

1.  Log in to your [Snyk account](https://app.snyk.io/).
2.  Click on your avatar in the bottom left corner and select **Account Settings**.
3.  Navigate to the **API Token** section.
4.  Click **Click to show** (or **Generate**) and copy the token.

## 2. Add the Token to GitHub Secrets

The GitHub Actions workflow (`code-review.yml`) expects a secret named `SNYK_TOKEN`.

1.  Open your project repository on GitHub.
2.  Go to **Settings** (top tab).
3.  In the left sidebar, click on **Secrets and variables** -> **Actions**.
4.  Click the **New repository secret** button.
5.  **Name**: `SNYK_TOKEN`
6.  **Secret**: Paste your Snyk API token.
7.  Click **Add secret**.

## 3. Verify Integration

Once the secret is added, the next run of the "Code Review" workflow will use this token to authenticate.

### Manual Trigger
You can manually trigger the workflow to test the integration:
1.  Go to the **Actions** tab in your GitHub repository.
2.  Select the **Code Review** workflow on the left.
3.  Click the **Run workflow** dropdown and then the **Run workflow** button.

## 4. Scan Targets

The Snyk integration covers multiple areas of the project to ensure comprehensive security:

-   **Open Source (SCA)**:
    -   **Backend**: Analyzes `backend/pom.xml`.
    -   **Frontend**: Analyzes `frontend/package.json`.
    -   **Android**: Analyzes `android/app/build.gradle`.
    -   **Scripts**: Analyzes `scripts/requirements.txt`.
-   **Infrastructure as Code (IaC)**: Analyzes Kubernetes manifests in `deploy/k8s/`.
-   **Static Analysis (SAST)**: Analyzes the source code across the entire project for common vulnerabilities.
-   **Containers**: Analyzes the final Docker image for OS-level vulnerabilities.

## 5. Troubleshooting

If you still see an `Authentication error (SNYK-0005)`:
- Ensure the secret name is exactly `SNYK_TOKEN` (all caps, underscore).
- Verify that the token hasn't expired in your Snyk settings.
- Check if your Snyk account has the necessary permissions to scan the project.

## 5. Analyzing Results

The workflow is configured to upload Snyk results as SARIF artifacts.
1.  After a workflow run completes, scroll down to the **Artifacts** section.
2.  Download `snyk-results-backend` or `snyk-results-frontend`.
3.  You can provide these files to Junie for automated fixing.

## 6. Local Scanning (Optional)

If you have the Snyk CLI installed, you can run scans locally.

### Installation
- **npm**: `npm install -g snyk`
- **Windows Standalone**: Download `snyk-win.exe` from [Snyk's GitHub releases](https://github.com/snyk/cli/releases).

### Commands
For local scanning, use `snyk` (or `snyk-win.exe` on Windows) followed by the desired command. 

Example for backend:
```bash
snyk test
```

For more detailed local usage, see [Transmitting Snyk Results to Junie](transmitting-snyk-results.md).
