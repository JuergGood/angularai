# Development Standards

This document outlines the core principles, naming conventions, and best practices for the AngularAI project.

## General Principles

- **Modern Standards**: Use the latest stable versions of frameworks (Angular 21+, Spring Boot 4+). Avoid deprecated methods and syntax.
- **Consistency**: Follow existing naming conventions and project structure.
- **Build Integrity**: Ensure the project builds successfully (`mvn clean install`) before submitting changes.
- **Testing**: Maintain high test coverage (>80%) for both frontend and backend.
- **Docker First**: Ensure all changes are compatible with the Docker-based deployment.

## Backend Standards (Spring Boot)

### Architecture
- **Controllers**: RESTful controllers in `ch.goodone.angularai.backend.controller`.
- **Models**: JPA entities in `ch.goodone.angularai.backend.model`.
- **DTOs**: Use DTOs for API requests and responses to avoid leaking internal entity structures.
- **Migrations**: Always use Flyway for database schema changes.

### Best Practices
- **Security**: No hardcoded keys. Use environment variables.
- **Validation**: Use Jakarta Validation annotations (e.g., `@NotBlank`, `@Email`).
- **Date/Time**: Use `LocalDate` and `LocalDateTime`.

## Frontend Standards (Angular)

### Architecture
- **Standalone Components**: All new components must be standalone.
- **Control Flow**: Use modern Angular control flow (`@if`, `@for`, `@empty`).
- **Styles**: ALWAYS extract templates and styles into separate `.html` and `.css` files unless the component is very small (< 60 lines).

### State Management
- **Signals**: Use Angular Signals for reactive state management.
- **Services**: Centralize all API calls in services.

## Security & Vulnerability Scanning

The project uses the **OWASP Dependency-Check** Maven plugin to identify project dependencies and check if there are any known, publicly disclosed vulnerabilities.

### Running a Security Scan
To run a scan locally (this will also update the local NVD database if `-DautoUpdate=false` is omitted):
```bash
$env:NVD_API_KEY="your-api-key"; mvn org.owasp:dependency-check-maven:check
```

### Report Locations
After a successful scan, the results are generated in multiple formats (`HTML`, `XML`, `JSON`, `CSV`, `SARIF`) in the `target` directory of the respective modules:

- **Root Project**: `target/dependency-check-report.html`
- **Backend**: `backend/target/dependency-check-report.html`
- **Frontend**: `frontend/target/dependency-check-report.html`
- **Test Client**: `test-client/target/dependency-check-report.html`

The **HTML report** is the most user-friendly format for manual review.

## Chapter 3: Maintenance & Security

To maintain a high security posture, the following practices are mandatory:

### 3.1 Regular Dependency Updates
- **Monthly Review**: Check for updates to major frameworks (Angular, Spring Boot).
- **Automated Fixes**: Use `npm audit fix` in the frontend and `mvn versions:display-dependency-updates` in the backend to identify out-of-date packages.
- **Vulnerability Patching**: If a dependency cannot be updated due to breaking changes, use `overrides` in `package.json` or `dependencyManagement` in `pom.xml` to force security patches.

### 3.2 Continuous Security Scanning
- **NVD Persistence**: The project is configured to persist the NVD database in `data/dependency-check/`. This ensures fast subsequent scans.
- **Full Scans**: At least once a week, run a full scan (omitting `-DautoUpdate=false`) to catch new vulnerabilities:
  ```bash
  $env:NVD_API_KEY="your-api-key"; mvn org.owasp:dependency-check-maven:check
  ```

### 3.3 False Positive Management
If a vulnerability is identified as a false positive or is non-exploitable in our context:
1. Review the vulnerability in the HTML report.
2. Click the **"suppress"** button to generate the XML snippet.
3. Add the snippet to `dependency-check-suppressions.xml` in the root directory.
4. Document the reason for suppression in a comment within the XML.

## Documentation Standards

- **Language**: English for all code, comments, and documentation.
- **Format**: Markdown for all guides.
- **Structure**: Maintain the Documentation Hub structure as the single source of truth for navigation.
