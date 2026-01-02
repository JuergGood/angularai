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
The Angular development server is configured to proxy `/api` requests to `http://localhost:8080`. Ensure the backend is running.

## Deployment to AWS
For instructions on how to deploy this application to AWS (ECS Fargate, RDS PostgreSQL), please refer to the documentation in the `ai/` and `deploy/` directories:
- [AWS Setup and Infrastructure](ai/aws_setup.md)
- [PostgreSQL Setup](ai/postgres_setup.md)
- [ECS Fargate Configuration](ai/aws_fargate_config.md)
- [Pushing Images to Amazon ECR](ai/ecr_push_instructions.md)
