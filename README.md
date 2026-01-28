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

Note: A `.dockerignore` file is included in the root directory to ensure that only necessary files are sent to the Docker daemon during builds. This significantly reduces build times and keeps image sizes optimized by excluding local `node_modules`, `dist`, `target` folders, and other build artifacts. This optimization applies to both Frontend and Backend builds.

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
To run the backend from IntelliJ, you must ensure that required environment variables (like `IPSTACK_API_KEY` and `APP_BASE_URL`) are available. 
- You can manually add them to your Run Configurations.
- Alternatively, use a plugin like **EnvFile** to automatically load the `.env` file into your Run Configurations.
- **Note on `APP_BASE_URL`**: By default, the backend uses `http://localhost:4200` for `app.base-url`. This ensures that email verification links point to the Angular development server during local development. If you need to override this, set the `APP_BASE_URL` environment variable.

### Frontend (Web)
Navigate to `frontend/` and run `npm install` and then `npm start`.
The Angular development server is configured to proxy `/api` requests to `http://localhost:8080`. Ensure the backend is running.

## Documentation

The project documentation is organized in the `doc/` directory. For a comprehensive overview, start with the **[Documentation Hub](doc/architecture/Home.md)**.

### Core Guides
- [User Guide](doc/user-guide/user-guide.md)
- [Admin Guide](doc/admin-guide/admin-guide.md)
- [Release Notes](doc/user-guide/release-notes.md)
- [FAQ](doc/user-guide/faq.md)

### Technical Details
- [Architecture Overview](doc/architecture/Architecture.md)
- [Core Workflows](doc/architecture/workflows/use-cases.md)
- [Development Standards](doc/development/common/Development-Standards.md)
- [Frontend Development](doc/development/frontend/Frontend-Development.md)
- [Backend Development](doc/development/backend/Backend-Development.md)
- [Deployment & Infrastructure](doc/infrastructure/Deployment.md)

### Deployment & Tools
- [Confluence Export Script](scripts/md_to_confluence.py)
- [Android Development](doc/development/android/Android-Development.md)
- [PostgreSQL Setup](doc/development/backend/postgres_setup.md)

## Release Process

To create a new release (e.g., version 1.0.3):

1.  **Preparation**: Ensure all changes are committed and tested.
2.  **Run Release Script**: Execute the following command in PowerShell:
    ```bash
    .\scripts\release.ps1 -NewVersion "1.0.3"
    ```
    This script will:
    - Update the version in `pom.xml`.
    - Synchronize the version across all project files (package.json, build.gradle, etc.).
    - Add a header for the new version in `release-notes.md`.
    - Regenerate the help data JSON files.
    - Create a git commit and a git tag (e.g., `v1.0.3`).
3.  **Manual Step**: Edit `doc/userguide/release-notes.md` to provide meaningful details for the release.
4.  **Push**: Push the changes and tags to the repository:
    ```powershell
    git push origin main --tags
    ```
5.  **Next Version**: To start development on the next version (e.g., 1.0.4), simply run the script again with the new version number when you are ready to release it. During development, you can manually update the version in `pom.xml` if needed and run `.\scripts\sync-version.ps1`.
