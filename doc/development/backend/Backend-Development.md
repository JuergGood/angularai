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

### H2 Database (Local Development)
The application uses H2 as the default database for local development and testing.

- **Console URL**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **Settings**:
    - **Driver Class**: `org.h2.Driver`
    - **JDBC URL (Memory)**: `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1` (Default for local IDE runs)
    - **JDBC URL (File/Docker)**: `jdbc:h2:file:./data/angularai;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE` (When using Docker or `h2-file` profile)
    - **User Name**: `sa`
    - **Password**: (leave empty)

**Note**: To access the console in a Docker environment, ensure you use the `8080` port directly.

```powershell
docker compose logs app | Select-String "jdbc:h2"
```

Database JDBC URL [jdbc:h2:file:./data/testdb]

jdbc:h2:file:./data/testdb;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE

```sql
SELECT * FROM ACTION_LOG;
```

### PostgreSQL
Supported for production-like environments. See [PostgreSQL Setup](postgres_setup.md) for details.

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
