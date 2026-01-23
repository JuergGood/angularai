# Release Notes

## Version 1.0.4 (2026-01-23)
*   New release version 1.0.4.


## Version 1.0.3 (2026-01-23)
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

