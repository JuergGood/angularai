# Transmitting Snyk Results to Junie

To enable Junie to assess and fix vulnerabilities found by Snyk, you need to provide the results in a format that the AI can "read" and "understand" within the project environment.

Since Junie cannot directly access your private Snyk dashboard URL, follow one of the methods below.

## Method 1: Upload SARIF File (Recommended)

The **SARIF** (Static Analysis Results Interchange Format) is the best way to transmit results. It provides detailed context about the line numbers, files, and fix recommendations.

### Steps:
1.  **Generate results locally** (if you have Snyk CLI installed):

    > **Note for Windows users**: If you are using the standalone Windows binary, use `snyk-win.exe` instead of `snyk` in the commands below.

    **For Backend:**
    ```bash
    snyk-win test --sarif > snyk-backend.sarif.json
    ```

    **For Frontend:**

    ```bash
    snyk-win test --sarif > snyk-frontend.sarif.json
    ```

    **For Android:**

    ```bash
    cd android
    snyk-win test --sarif > ../snyk-android.sarif.json
    cd ..
    ```

    **For Scripts:**

    ```bash
    cd scripts
    snyk-win test --sarif > ../snyk-scripts.sarif.json
    cd ..
    ```

    **For Infrastructure as Code (Kubernetes):**

    ```bash
    snyk-win iac test deploy/k8s/ --sarif > snyk-deploy.sarif.json
    ```

    **For Snyk Code (SAST):**

    ```bash
    snyk-win code test --sarif > snyk-code.sarif.json
    ```

    **For Containers:**

    ```bash
    snyk-win container test angularai-app:latest --sarif > snyk-container.sarif.json
    ```

2.  **Upload/Commit the file** to the repository (e.g., in a `tmp/snyk/` folder).
3.  **Tell Junie**: "I have uploaded the Snyk results to `tmp/snyk/snyk-backend.sarif.json`. Please analyze and fix the issues."

## Method 2: Paste CLI Output (Quickest for few issues)

If you only have a few high-priority vulnerabilities, you can simply run the test and paste the text output directly into the chat.

### Steps:
1.  Run the test in your terminal:

    > **Note for Windows users**: Use `snyk-win.exe` if you are using the standalone binary.

    ```bash
    snyk-win test --severity-threshold=high
    ```
2.  **Copy the output** from the terminal.
3.  **Paste into Chat**: Provide the text to Junie.

## Method 3: Automated CI Artifacts

The project's GitHub Actions workflow is configured to automatically generate these files on every push and upload them as artifacts.

### Configuration
See the [Snyk Integration Guide](snyk-integration-guide.md) for details on how to set up the required `SNYK_TOKEN`.

### Workflow Implementation
The `snyk` job in `.github/workflows/code-review.yml` handles this:
```yaml
      - name: Snyk Open Source Scan (Backend)
        run: snyk test --severity-threshold=high --sarif > snyk-backend.sarif.json
        continue-on-error: true
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}

      - name: Upload Snyk Results
        uses: actions/upload-artifact@v4
        with:
          name: snyk-results
          path: |
            snyk-backend.sarif.json
            snyk-frontend.sarif.json
            snyk-container.sarif.json
```

## How Junie Processes Results

Once the file is available, Junie will:
1.  **Read the file**: Parse the JSON/SARIF to find specific CVEs and affected files.
2.  **Locate Code**: Use `search_project` to find the exact dependency or line of code.
3.  **Apply Fix**: 
    - For dependencies: Update `pom.xml` or `package.json` to the recommended secure version.
    - For code issues: Apply a `search_replace` to patch the vulnerability.
4.  **Verify**: Run `mvn clean install` or `npm test` to ensure the fix doesn't break the build.
5.  **Re-scan**: (If Snyk is available in the environment) Run the scan again to verify the fix.
