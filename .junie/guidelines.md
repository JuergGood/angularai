# Development Guidelines

This document outlines the best practices and standards for the AngularAI project.

## General Principles
- **Modern Standards**: Use the latest stable versions of frameworks (Angular 21+, Spring Boot 4+).
- **Consistency**: Follow existing naming conventions and project structure.
- **Testing**: Maintain high test coverage for both frontend and backend.
- **Docker First**: Ensure all changes are compatible with the Docker-based deployment.

## Backend Development (Spring Boot)

### 1. Architecture
- **Controllers**: Use RESTful controllers in `ch.goodone.angularai.backend.controller`.
- **Models**: Use JPA entities in `ch.goodone.angularai.backend.model`.
- **Repositories**: Use Spring Data JPA repositories in `ch.goodone.angularai.backend.repository`.
- **DTOs**: Use DTOs for API requests and responses to avoid leaking internal entity structures. Implement `fromEntity()` static methods in DTOs for centralized mapping.

### 2. Best Practices
- **Security**: Use `@MockitoBean` instead of `@MockBean` in tests (Spring Boot 4 requirement).
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
- **Standalone Components**: All new components must be `standalone: true`.
- **Control Flow**: Use modern Angular control flow (`@if`, `@for`, `@empty`) instead of `*ngIf` and `*ngFor`.
- **Templates**: Keep templates in separate `.html` files for better maintainability.
- **Styles**: Use Scoped CSS within the component or external `.css` files. Prefer Material Design for UI elements.

### 2. State & Data
- **Signals**: Use Angular Signals for reactive state management (e.g., `currentUser` in `AuthService`).
- **Services**: Centralize all API calls in services.
- **Relative URLs**: Use relative paths (e.g., `/api/...`) for API calls to support the Nginx/dev-server proxying.

### 3. UI/UX (Angular Material)
- **Icons**: Use Material Icons (already configured in `index.html`).
- **Theming**: Follow the `indigo-pink` theme.
- **Accessibility**: Use appropriate Material components for forms and buttons.

### 4. Testing (Vitest/Angular Testing Library)
- **Providers**: Use `provideHttpClient()`, `provideHttpClientTesting()`, and `provideAnimations()` for test setup.
- **Clean Environment**: Initialize the test environment in `src/test.ts` using `BrowserDynamicTestingModule`.

## Deployment & Environments
- **Local Dev**: Use `npm start` (Angular) and the Spring Boot application (IntelliJ). The Angular proxy (`proxy.conf.json`) handles routing to the backend on `localhost:8080`.
- **Docker**: Use `docker compose up --build`. Nginx handles the reverse proxying of `/api` requests to the `backend` container.
