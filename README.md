# AngularAI Application

This is a full-stack application with a Spring Boot backend and an Angular frontend.

## Prerequisites

- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/install/)

## Running with Docker

To run the entire application using Docker Compose, navigate to the root directory. First, create a `.env` file in the root directory (you can copy `.env.example` as a template):

```bash
cp .env.example .env
```

Then run:

```bash
docker compose up --build
```

The application will be available at:
- Frontend: [http://localhost](http://localhost)
- Backend API: [http://localhost:8080/api](http://localhost:8080/api)
- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:testdb`)

## Deployment Scripts

Scripts are available in the `scripts/` folder for common deployment tasks (PowerShell and Windows CMD). **Note: PowerShell scripts automatically load variables from your local `.env` file.**

- **Local Docker Deployment**: `.\scripts\deploy-local.ps1` or `.\scripts\deploy-local.bat`
  - Runs `docker compose up --build -d` to start the application locally in the background.
- **AWS Deployment**: `.\scripts\deploy-aws.ps1` or `.\scripts\deploy-aws.bat`
  - Authenticates with AWS ECR, builds, tags, and pushes frontend and backend images, and forces a new deployment on ECS services.
- **Environment Loading**: The `load-env.ps1` script is used by other PowerShell scripts to ensure sensitive keys (like `IPSTACK_API_KEY`) are available in the session.

## Project Structure

- `backend/`: Spring Boot application.
- `frontend/`: Angular application.
- `android/`: Android Jetpack Compose application.
- `docker-compose.yml`: Orchestration for both services.

## Development

### IntelliJ IDEA Setup
To run the backend from IntelliJ, you must ensure that required environment variables (like `IPSTACK_API_KEY`) are available. 
- You can manually add them to your Run Configurations.
- Alternatively, use a plugin like **EnvFile** to automatically load the `.env` file into your Run Configurations. **Do not commit these keys to Git.**

### Frontend (Web)
Navigate to `frontend/` and run `npm install` and then `npm start`.
The Angular development server is configured to proxy `/api` requests to `http://localhost:8080`. Ensure the backend is running.

## Documentation

- [User Guide](doc/userguide/user-guide.md)
- [Release Notes](doc/userguide/release-notes.md)
- [Admin Guide](doc/userguide/admin-guide.md)
- [FAQ](doc/userguide/faq.md)
- [Confluence Export Script](scripts/md_to_confluence.py)
- [Android Build Instructions](doc/ai/android/android-build-instructions.md)
- [AWS Setup and Infrastructure](doc/ai/aws/aws_setup.md)
- [PostgreSQL Setup](doc/ai/backend/postgres_setup.md)
- [ECS Fargate Configuration](doc/ai/aws/aws_fargate_config.md)
- [Creating a Backend Target Group](doc/ai/aws/aws_create_target_group.md)
- [ALB and Connectivity Troubleshooting](doc/ai/aws/aws_alb_troubleshooting.md)
- [Pushing Images to Amazon ECR](doc/ai/aws/aws_ecs_push_instructions.md)
