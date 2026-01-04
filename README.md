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

## Deployment Scripts

PowerShell scripts are available in the `scripts/` folder for common deployment tasks:

- **Local Docker Deployment**: `.\scripts\deploy-local.ps1`
  - Runs `docker compose up --build -d` to start the application locally in the background.
- **AWS Deployment**: `.\scripts\deploy-aws.ps1`
  - Authenticates with AWS ECR, builds, tags, and pushes frontend and backend images, and forces a new deployment on ECS services.

## Project Structure

- `backend/`: Spring Boot application.
- `frontend/`: Angular application.
- `android/`: Android Jetpack Compose application.
- `docker-compose.yml`: Orchestration for both services.

## Development

### Backend
Navigate to `backend/` and run `./mvnw spring-boot:run`.

### Frontend (Web)
Navigate to `frontend/` and run `npm install` and then `npm start`.
The Angular development server is configured to proxy `/api` requests to `http://localhost:8080`. Ensure the backend is running.

### Frontend (Android)
See the [Android Build Instructions](doc/ai/android/android-build-instructions.md) for details on how to build and run the mobile application.

### AWS Deployment
Detailed instructions for AWS deployment are located in the `doc/ai/` and `deploy/` directories.
**Important**: Before registering task definitions, ensure you replace all placeholders (e.g., `<AWS_ACCOUNT_ID>`, `<REGION>`, `<RDS_ENDPOINT>`) with your actual AWS resource values.

- [AWS Setup and Infrastructure](doc/ai/aws/aws_setup.md)
- [PostgreSQL Setup](doc/ai/postgres_setup.md)
- [ECS Fargate Configuration](doc/ai/aws/aws_fargate_config.md)
- [Creating a Backend Target Group](doc/ai/aws/aws_create_target_group.md)
- [ALB and Connectivity Troubleshooting](doc/ai/aws/aws_alb_troubleshooting.md)
- [Pushing Images to Amazon ECR](doc/ai/aws/aws_ecs_push_instructions.md)
