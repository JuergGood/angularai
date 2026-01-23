# Development Guidelines

This document outlines the best practices and standards for the AngularAI project.

## General Principles
- **Modern Standards**: Use the latest stable versions of frameworks (Angular 21+, Spring Boot 4+). NEVER use deprecated methods, features, or syntax in any language (e.g., avoid `*ngIf` and `*ngFor` in Angular, use modern control flow instead).
- **Consistency**: Follow existing naming conventions and project structure.
- **Centralized Versioning**: 
    - The **Root `pom.xml`** is the single source of truth for the project version.
    - All modules (Backend, Frontend, Android, Test Client) must share the same version.
    - Use `.\scripts\sync-version.ps1` to propagate version changes from the root `pom.xml` to other files (package.json, build.gradle, deployment scripts, and documentation).
- **Build Integrity**: Ensure the project builds successfully (`mvn clean install`) before submitting changes.
- **Testing**: Maintain high test coverage (>70%) for both frontend and backend at all times.
- **Docker First**: Ensure all changes are compatible with the Docker-based deployment.
- **Language**: Always communicate in English for all interactions, thoughts, and documentation, unless explicitly requested otherwise by the user.
- **Translations**: Always provide translations for both supported languages (English `en.json` and German `de-ch.json`) when adding or modifying UI text.

## Backend Development (Spring Boot)

### 1. Architecture
- **Controllers**: Use RESTful controllers in `ch.goodone.angularai.backend.controller`.
- **Models**: Use JPA entities in `ch.goodone.angularai.backend.model`. Always create a Flyway migration script (in `backend/src/main/resources/db/migration/`) whenever a JPA entity is created or modified to ensure the database schema stays in sync.
- **Repositories**: Use Spring Data JPA repositories in `ch.goodone.angularai.backend.repository`.
- **DTOs**: Use DTOs for API requests and responses to avoid leaking internal entity structures. Implement `fromEntity()` static methods in DTOs for centralized mapping.

### 2. Best Practices
- **Security**: 
    - Use `@MockitoBean` instead of `@MockBean` in tests (Spring Boot 4 requirement).
    - **No Hardcoded Keys**: Never include sensitive API keys, tokens, or credentials in the source code, configuration files (e.g., `application.properties`), or IDE settings committed to Git (e.g., `.idea/workspace.xml`). Use environment variables and placeholders (e.g., `${MY_API_KEY}`) instead.
- **Validation**: Use `@Column` annotations for explicit database mapping. Use unique constraints where appropriate (e.g., login, email).
- **JSON Handling**: Use `tools.jackson.databind.ObjectMapper` for JSON processing in tests.
- **Date/Time**: Use `LocalDate` for dates. Use `@JsonFormat(pattern = "yyyy-MM-dd")` for DTO date fields.
- **Role-Based Access Control**: Enforce security in `SecurityConfig` and use the `Role` enum.

### 3. Testing
- Use JUnit 5 and MockMvc for controller testing.
- Always include Spring Security in the test context if the endpoint is protected.
- Keep tests isolated from the database using a `test` profile if needed.

## Frontend Development (Angular)

### 1. Architecture
- **Templates & Styles**: ALWAYS extract templates and styles into separate `.html` and `.css` files. For very small components (typically < 60 lines total for the `.ts` file), inlining templates and styles is acceptable to reduce file clutter. Do NOT use deprecated `*ngIf` or `*ngFor` regardless of inlining.
- **Standalone Components**: All new components must be `standalone: true`.
- **Control Flow**: Use modern Angular control flow (`@if`, `@for`, `@empty`) instead of `*ngIf` and `*ngFor`.
- **Styles**: Use Scoped CSS within the component or external `.css` files. Prefer Material Design for UI elements.

### 2. State & Data
- **Signals**: Use Angular Signals for reactive state management (e.g., `currentUser` in `AuthService`).
- **Services**: Centralize all API calls in services.
- **Relative URLs**: Use relative paths (e.g., `/api/...`) for API calls to support the Nginx/dev-server proxying.

### 3. UI/UX (Angular Material)
- **Icons**: Use Material Icons (already configured in `index.html`).
- **Theming**: Follow the `indigo-pink` theme.
- **Accessibility**: Use appropriate Material components for forms and buttons.
- **Consistency**: Follow the [Frontend Style Guideline](frontend-style-guideline.md) for all UI changes to ensure consistent look and feel.

### 4. Testing (Vitest/Angular Testing Library)
- **Providers**: Use `provideHttpClient()`, `provideHttpClientTesting()`, and `provideAnimations()` for test setup.
- **Clean Environment**: Initialize the test environment in `src/test.ts` using `BrowserDynamicTestingModule`.

## Deployment & Environments
- **Local Dev**: Use `npm start` (Angular) and the Spring Boot application (IntelliJ). The Angular proxy (`proxy.conf.json`) handles routing to the backend on `localhost:8080`.
- **Docker**: Use `docker compose up --build`. Nginx handles the reverse proxying of `/api` requests to the `backend` container.
