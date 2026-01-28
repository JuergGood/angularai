# Backend Development

The backend is a Spring Boot application built with Java 21.

## Architecture

- **Controllers**: RESTful endpoints in `ch.goodone.angularai.backend.controller`.
- **Models**: JPA entities in `ch.goodone.angularai.backend.model`.
- **Repositories**: Spring Data JPA repositories for data access.
- **DTOs**: Data Transfer Objects for API requests and responses.

## Key Features

- **Security**: Role-based access control (RBAC) using Spring Security.
- **Audit Logging**: Automated logging of user actions.
- **Validation**: Strict data validation using Hibernate Validator and database constraints.

## Development Setup

To run the backend locally:
1. Navigate to the `backend/` directory.
2. Run `./mvnw spring-boot:run`.
3. The API will be available at `http://localhost:8080/api`.

## Testing

We use JUnit 5 and Mockito for unit and integration testing. Run tests with:
```bash
./mvnw test
```
