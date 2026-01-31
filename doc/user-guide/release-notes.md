# Release Notes

## Version 1.0.6 (2026-01-31)
*   Add Snyk scans for Android, scripts, and IaC with SARIF reports; update security docs
*   Enhance security and use non-root users in Kubernetes and Docker setups. Adjust frontend to listen on port 8080, update backend and frontend Dockerfiles to run as non-root, and refine Kubernetes configurations for both services with improved security contexts.
*   **- **Checkstyle Configuration and Snyk Integration****: Updated Checkstyle config path and added checkstyle.xml to Docker context. Migrated from `values()` to `entries` for `Priority` and `TaskStatus` in `DataService`. Removed default `TaskStatus` initialization in `Task` constructor. Added Snyk integration guide for CI/CD pipeline security scanning.
*   **Update GitHub Actions workflow**: Save Snyk reports as artifacts
*   Update Gradle build script and dependencies
*   Add Snyk results transmission documentation and export script
*   - Integrate MCP & Autonomous Agents documentation and update relevant architecture details. - Refactor `MainActivity` to clean up navigation logic. - Enhance task status with additional `COMPLETED` enum. - Adjust build configuration for testing. - Remove unnecessary components and utilize updated mock versions. - Establish GitHub Snyk integration detailed in security roadmap.
*   Implement Trivy and Snyk scans in CI/CD pipeline
*   Enhance UI navigation and update components
*   Add static analysis checks and improve code formatting
*   Remove outdated presentation files for cleanup
*   Add presentation module with Maven setup and utilities
*   Add `JavaMailSender` mock to tests, align profile configurations, and update npm dependencies. Introduced shared test properties file and removed authorization header checks in mock requests.
*   **Finalize Security Roadmap Level 2**: Implement API rate limiting, strict CSP, and advanced audit logging with Hibernate Envers. Complete dependency pinning and enhance security monitoring with real-time alerts. Optimize observability with forensic context and session tracking.
*   Add advanced security roadmap, enhance reCAPTCHA handling, and refine tests
*   Remove `sonar-issues-v2.json` to clean up outdated static analysis artifacts.
*   Add comprehensive unit tests for components and enhance security handling
*   Automate UI screenshot updates for documentation with Playwright tests
*   Optimize test user setup in `TaskControllerTest` and enhance user registration handling
*   Refactor `TaskParserService` and optimize input sanitation, improve logging consistency with constants, and enhance task metrics endpoint in `TaskController`.
*   Refactor build scripts and documentation for consistency
*   **Update German FAQ**: improve password reset guidance and clarify user role
*   Remove outdated documentation related to Angular's action log and admin guide for improved repository clarity.
*   Add documentation for backend and development standards
*   Remove outdated workflow documentation and relocate essential diagrams and use cases to the architecture directory for improved clarity and organization.
*   Remove outdated documentation related to AngularAI, Android, and system architecture to improve repository clarity and organization.
*   Remove outdated documentation for better repository organization.
*   - Add support for `h2-file` storage on AWS Fargate and local Docker environments - Integrate EFS configuration and task definition updates for persistence in AWS - Update `docker-compose.yml`
*   Add landing message feature across backend, frontend, and e2e tests
*   Implement user account deletion feature and update e2e tests
*   Implement password recovery feature and enhance login error handling
*   Add Markdown to DOCX conversion script and enhance AWS deployment process
*   Add email verification component and update verification flow
*   Add .dockerignore for optimized build efficiency and test cleanup


## Version 1.0.5 (2026-01-25)
*   **Infrastructure**: Update environment variables configuration
*   **Security**: Add resend verification feature and enhance email verification UX
*   **UI**: Refine register component UI and functionality
*   **Auth**: Enhance registration form validation and error handling
*   **UX**: Enhance registration form UX with hints and improved error handling
*   **Auth**: Refactor registration form to enforce full name validation, enhance error handling, and update tests
*   **Testing**: Refactor registration and implement extensive validation tests
*   **Docs**: Add UI Architecture Documentation and Enhance Auth Flow Tests
*   **Testing**: Refactor e2e tests for improved session handling and login logic
*   **Testing**: Add Playwright e2e tests for Tasks and Auth Flow, update routing, and document UX strategy
*   **Quality**: Remove H2 configuration, enhance system info tests with i18n checks, add user registration and verification schema. Update i18n files for password strength and registration messages.
*   **Security**: Integrate Google reCAPTCHA for enhanced user verification, update registration logic to include token verification, and enhance user data initializer and error handling.

## Version 1.0.4 (2026-01-23)
*   **Security (reCAPTCHA)**: Implemented Google reCAPTCHA v2 on the registration page to ensure only real persons can register. Includes backend verification and configurable site/secret keys.
*   **Geolocation Service**: Integrated IP-based location lookup, including a system setting to toggle the feature and automatic local/loopback address skipping.
*   **Enhanced Environment Management**: Added `.env.example` template and improved environment loading logic for better local development setup.
*   **Advanced Task Parsing**: Implemented comprehensive task parsing logic with a dedicated test suite to improve natural language task entry.
*   **UI/UX Improvements**: Fixed dark mode issues and refined the task management interface.
*   **Documentation Refinement**: Streamlined AWS deployment documentation, removing obsolete ALB and ECR instructions.

## Version 1.0.2 (2026-01-17)
*   **Version Display Fixes**: Resolved issues where the version number was not correctly displayed in the UI.
*   **Quality Assurance**: Integrated Qodana for static code analysis and addressed multiple SonarLint issues to improve code quality.
*   **Test Coverage**: Significantly increased test coverage across the project, including backend JUnit tests and frontend Cypress integration tests.
*   **CI/CD Stability**: Fixed various GitHub Actions CI build issues to ensure reliable automated testing.

## Version 1.0.1 (2026-01-14)
*   **Dashboard Visuals**: Enhanced the dashboard with improved visuals and responsive layout across frontend, backend, and Android.
*   **Internationalization**: Added German translations (`de-ch`) and improved the translation infrastructure.
*   **Security Enhancements**: Introduced `ROLE_ADMIN_READ` for granular access control and read-only administrative access.
*   **Presentation Tools**: Added scripts and templates for generating high-quality architectural presentations directly from the codebase.

## Version 1.0.0 (2026-01-08)
*   **Initial Release**: Core functionality of the AngularAI prototype.
*   **Multi-Platform Support**: Unified experience across Web (Angular) and Android platforms.
*   **Task Management**: Comprehensive task lifecycle including status tracking, filtering, and drag-and-drop reordering.
*   **Real-time Monitoring**: Integrated Action Log and Log menu for system transparency.
*   **API Documentation**: Integrated Swagger UI for easy exploration of the backend REST API.
*   **Database Migrations**: Initialized Flyway integration for reliable schema management.
*   **Test Client**: Added a CLI tool for data management and direct API interaction.

## Pre-1.0.0 (2026-01-01)
*   **Foundation**: Established the core project structure with Spring Boot backend and Angular standalone components.
*   **Infrastructure**: Set up Docker-based deployment and Nginx reverse proxy configuration.
*   **Architecture**: Defined the "AngularAI" ecosystem diagrams and core design principles.




