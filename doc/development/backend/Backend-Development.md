# Backend Development

The backend is a robust Spring Boot application built with Java 21 and Spring Boot 4.

## Core Technologies

- **Spring Boot 4**: Framework for rapid application development.
- **Java 21**: Utilizing the latest LTS features.
- **Spring Data JPA**: Persistent data storage with Hibernate.
- **Spring Security**: Role-based access control and secure endpoints.
- **Flyway**: Database migrations for version-controlled schema changes.
- **Maven**: Dependency management and build automation.

## Development Setup

To run the backend locally:

1.  **Prerequisites**: Ensure you have Java 21 and Maven installed.
2.  **Configuration**: Create a `.env` file in the root directory (use `.env.example` as a template).
3.  **Run with IntelliJ**:
    - Open the project in IntelliJ IDEA.
    - Use the **EnvFile** plugin to load the `.env` file.
    - Run `AngularAiBackendApplication`.
4.  **Run with Maven**:
    ```bash
    mvn clean install
    cd backend
    mvn spring-boot:run
    ```

## Database

- **H2 (Default)**: Used for local development and testing. Access the console at `http://localhost:8080/h2-console`.
- **PostgreSQL**: Supported for production-like environments. See [PostgreSQL Setup](postgres_setup.md) for details.

## API Documentation

- **OpenAPI/Swagger**: Explore the API endpoints at `http://localhost:8080/swagger-ui.html` (when enabled).
- **REST Principles**: The backend follows standard RESTful principles using DTOs for data transfer.

## Testing

- **JUnit 5 & MockMvc**: For controller and service layer testing.
- **High Coverage**: We aim for >80% test coverage.

Run tests:
```bash
mvn test
```
