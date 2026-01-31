# Transmitting Snyk Results to Junie

To enable Junie to assess and fix vulnerabilities found by Snyk, you need to provide the results in a format that the AI can "read" and "understand" within the project environment.

Since Junie cannot directly access your private Snyk dashboard URL, follow one of the methods below.

## Method 1: Upload SARIF File (Recommended)

The **SARIF** (Static Analysis Results Interchange Format) is the best way to transmit results. It provides detailed context about the line numbers, files, and fix recommendations.

### Steps:
1.  **Generate results locally** (if you have Snyk CLI installed):
    ```powershell
    # For Backend
    snyk test --sarif > snyk-backend.sarif.json
    
    # For Frontend
    cd frontend
    snyk test --sarif > ../snyk-frontend.sarif.json
    cd ..
    ```
2.  **Upload/Commit the file** to the repository (e.g., in a `tmp/snyk/` folder).
3.  **Tell Junie**: "I have uploaded the Snyk results to `tmp/snyk/snyk-backend.sarif.json`. Please analyze and fix the issues."

## Method 2: Paste CLI Output (Quickest for few issues)

If you only have a few high-priority vulnerabilities, you can simply run the test and paste the text output directly into the chat.

### Steps:
1.  Run the test in your terminal:
    ```bash
    snyk test --severity-threshold=high
    ```
2.  **Copy the output** from the terminal.
3.  **Paste into Chat**: Provide the text to Junie.

## Method 3: Automated CI Artifacts (Advanced)

We can update the GitHub Actions workflow to automatically generate these files on every push.

### Current Status:
The current `code-review.yml` runs Snyk but doesn't save the results as artifacts.

### Proposed Improvement:
Add a step to the `snyk` job in `.github/workflows/code-review.yml`:
```yaml
      - name: Generate SARIF report
        run: snyk test --sarif > snyk-results.sarif.json
        continue-on-error: true # Ensure the step runs even if vulnerabilities are found
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}

      - name: Upload Snyk Results
        uses: actions/upload-artifact@v4
        with:
          name: snyk-results
          path: snyk-results.sarif.json
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
