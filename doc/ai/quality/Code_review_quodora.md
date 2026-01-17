For a GitHub-based project with a Spring Boot backend and Angular frontend, I recommend the following low-cost (or free) tools that provide excellent visual reports and integrate directly into your workflow.

### 1. JetBrains Qodana (Highly Recommended)
Since I noticed a `qodana.yaml` file already exists in your project root, this is the most natural choice.
- **Visual Output**: Provides a rich, interactive web UI (similar to IntelliJ's "Inspect Code" but for the whole project) with sunburst charts, trend graphs, and categorized issues.
- **Why use it**: It uses the same engine as IntelliJ IDEA and WebStorm, ensuring high-quality analysis for both your Java and Angular code.
- **Coverage**: **Now Enabled**. It tracks test coverage across Java (JaCoCo), Kotlin (JaCoCo), and Angular (LCOV) modules.
- **License Audit**: **Now Enabled**. Automatically audits dependencies for license compliance.
- **Cost**: Offers a **Community version** (free for open source) and a "Starter" tier. It can be easily integrated into GitHub Actions.
- **How to view**: You can run it locally via Docker or view results in the Qodana Cloud dashboard after connecting your GitHub repo.

### 2. SonarCloud
The industry standard for static analysis and "Clean Code."
- **Visual Output**: Detailed dashboards showing "Quality Gates," "Code Smells," "Bugs," and "Vulnerabilities." It provides a historical trend of your technical debt.
- **Why use it**: Excellent at identifying complex logic issues and security vulnerabilities. It decorates your GitHub Pull Requests with visual summaries of the impact of your changes.
- **Cost**: **Free** for public GitHub repositories. Very affordable for small private teams.
- **Integration**: Direct integration with GitHub Actions.

### 3. GitHub Code Scanning (CodeQL)
Built directly into GitHub.
- **Visual Output**: Integrated directly into the "Security" tab of your repository. It shows "Security Alerts" with detailed data-flow visualizations (showing how an untrusted input reaches a dangerous sink).
- **Why use it**: Zero configuration for basic security analysis. It uses CodeQL to find deep-seated security patterns.
- **Cost**: **Free** for public repositories; included in GitHub Advanced Security for private ones.

### 4. Snyk
Focuses on security vulnerabilities in your code and dependencies.
- **Visual Output**: High-quality dependency graphs showing exactly which library is bringing in a vulnerability and how to fix it (remediation paths).
- **Why use it**: Crucial for Angular projects where the `node_modules` tree can be large and complex.
- **Cost**: Generous **Free tier** for individual developers and small teams (even for private repos).

### 5. Dependency-Track / OWASP Dependency-Check
If you want to focus specifically on the "Software Bill of Materials" (SBOM).
- **Visual Output**: Generates HTML reports with graphs showing the severity of vulnerabilities in your `pom.xml` and `package.json` dependencies.
- **Cost**: **Free** (Open Source).
- **Setup**: Can be run as a Maven plugin or a GitHub Action.

### Summary Recommendation
I suggest starting by activating **Qodana** (since you already have the config) and **SonarCloud**. This combination gives you the best "Visual Dashboard" experience for both general code quality and security.

Would you like me to help you set up a GitHub Action to automate any of these?