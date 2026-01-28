# Welcome to the AngularAI Wiki

AngularAI is a modern, full-stack application featuring an Angular frontend, a Spring Boot backend, and an Android client. This wiki serves as the central hub for all technical and user documentation.

## Getting Started

- **[Installation & Run](Home#running-the-application)**: How to get the project up and running locally using Docker.
- **[User Guide](User-Guide)**: Instructions for end-users on how to use the web application.
- **[Admin Guide](Admin-Guide)**: Documentation for administrators regarding user management and system monitoring.
- **[Android App](Android-App)**: Details about the mobile companion application.

## Development & Architecture

- **[Project Structure](Architecture#project-structure)**: Overview of the codebase and its components.
- **[Tech Stack](Architecture#tech-stack)**: The technologies behind AngularAI.
- **[Backend Development](Backend-Development)**: Spring Boot, JPA, and API details.
- **[Frontend Development](Frontend-Development)**: Angular signals, standalone components, and modern patterns.

## Deployment & Infrastructure

- **[AWS Deployment](Deployment#aws-deployment)**: Detailed instructions on deploying to AWS ECS Fargate.
- **[Docker Configuration](Deployment#docker)**: Information about the containerized environment.

## Support

- **[FAQ](FAQ)**: Frequently Asked Questions.

---

## Running the Application

To run the entire application using Docker Compose, navigate to the root directory and run:

```bash
docker compose up --build
```

The application will be available at:
- Frontend: [http://localhost](http://localhost)
- Backend API: [http://localhost:8080/api](http://localhost:8080/api)
- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:testdb`)
