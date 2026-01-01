# AngularAI Application

This is a full-stack application with a Spring Boot backend and an Angular frontend.

## Prerequisites

- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/install/)

## Running with Docker

To run the entire application using Docker Compose, navigate to the root directory and run:

```bash
docker compose up --build
```

The application will be available at:
- Frontend: [http://localhost](http://localhost)
- Backend API: [http://localhost:8080/api](http://localhost:8080/api)
- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:testdb`)

## Project Structure

- `backend/`: Spring Boot application.
- `frontend/`: Angular application.
- `docker-compose.yml`: Orchestration for both services.

## Development

### Backend
Navigate to `backend/` and run `./mvnw spring-boot:run`.

### Frontend
Navigate to `frontend/` and run `npm install` and then `npm start`.
Note: For local development without Docker, you might need to revert the API URLs in frontend services if you don't use a proxy.
