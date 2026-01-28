# Architecture & Tech Stack

AngularAI is designed as a modular, scalable application with a clear separation of concerns between its various layers.

## Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 4, JPA, Security, Maven |
| **Frontend** | Angular 21, TypeScript, Material, Signals |
| **Mobile** | Android (Jetpack Compose, Kotlin) |
| **DevOps** | Docker, AWS RDS/Fargate, SonarCloud |

## Project Structure

- `backend/`: Spring Boot application containing the REST API and business logic.
- `frontend/`: Angular application providing the web-based user interface.
- `android/`: Native Android application built with Jetpack Compose.
- `docker-compose.yml`: Configuration for running the entire stack locally.
- `doc/`: Detailed project documentation and design artifacts. Organized as follows:
    - `doc/architecture/`: High-level design, core workflows, and hub.
    - `doc/user-guide/`: End-user documentation.
    - `doc/admin-guide/`: Administrative documentation.
    - `doc/development/`: Developer guides and technical standards.
    - `doc/infrastructure/`: Deployment and infrastructure setup.
    - `doc/history/`: Structured project archive, including feature plans, architectural proposals, and milestones.
- `scripts/`: Utility scripts for deployment and maintenance.

## Quality Assurance

We maintain high quality standards through:
- **Comprehensive Testing**: ~80% coverage on both frontend and backend.
- **Static Analysis**: Integration with SonarCloud and Qodana for code health.
- **Automated CI/CD**: GitHub Actions for building, testing, and deployment.
