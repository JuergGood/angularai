# Deployment & Infrastructure

AngularAI is designed to be easily deployable using containerization and cloud services.

## Docker

The application is fully containerized using Docker. The `docker-compose.yml` file in the root directory orchestrates the following services:
- **frontend**: The Angular web application served via Nginx.
- **backend**: The Spring Boot API.
- **db**: (Optional/Prod) PostgreSQL database.

### Local Deployment
To start the stack locally:
```bash
docker compose up --build
```

## AWS Deployment

The project includes scripts and documentation for deploying to AWS using ECS Fargate.

### Prerequisites
- AWS CLI configured with appropriate permissions.
- Docker installed and running.
- An ECR repository created for both frontend and backend.

### Deployment Process
1. **Build and Push**: Use the provided scripts in `scripts/` to build and push images to ECR.
   - `.\scripts\deploy-aws.ps1`
2. **ECS Service Update**: The script also triggers a new deployment on the ECS services.

For detailed AWS setup, refer to `doc/ai/aws/aws_setup.md`.

## CI/CD

Continuous Integration and Deployment are handled via GitHub Actions.
- **Build & Test**: Triggered on every push and pull request.
- **SonarCloud**: Static analysis and quality gate checks.
- **Automated Deployment**: Configurable to deploy to AWS on successful master branch builds.
