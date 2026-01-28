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

## Documentation Standards

- **Language**: English for all code, comments, and documentation.
- **Format**: Markdown for all guides.
- **Structure**: Maintain the Documentation Hub structure as the single source of truth for navigation.
