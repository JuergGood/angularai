# Welcome to the AngularAI Documentation Hub

AngularAI is a modern, full-stack application featuring an Angular frontend, a Spring Boot backend, and an Android client. This hub serves as the central point for all technical and user documentation.

## 🚀 Getting Started

- **[Quick Start & Installation](Home#running-the-application)**: How to get the project up and running locally using Docker.
- **[Architecture Overview](Architecture)**: High-level design, tech stack, and project structure.

## 📖 Guides

- **[User Guide](../user-guide/user-guide.md)**: Instructions for end-users on how to use the web application.
- **[Admin Guide](../admin-guide/admin-guide.md)**: Documentation for administrators regarding user management and system monitoring.
- **[FAQ](../user-guide/faq.md)**: Frequently Asked Questions.

## 🛠 Development

- **[Frontend Development](../development/frontend/Frontend-Development.md)**: Angular signals, standalone components, and modern patterns.
- **[Backend Development](../development/backend/Backend-Development.md)**: Spring Boot, JPA, and API details.
- **[Android Development](../development/android/Android-Development.md)**: Details about the mobile companion application.
- **[DevOps & Quality](../development/devops/sonar-integration.md)**: Static analysis, SonarCloud, and CI/CD.
- **[MCP Server](../development/common/mcp-server.md)**: Information about the Model Context Protocol server.

## ☁️ Infrastructure & Deployment

- **[Docker Configuration](../infrastructure/Deployment.md#docker)**: Information about the containerized environment.
- **[AWS Deployment](../infrastructure/Deployment.md#aws-deployment)**: Detailed instructions on deploying to AWS ECS Fargate.
- **[Kubernetes Setup](../infrastructure/k8s/k8s-setup.md)**: Documentation for K8s deployments.

## 📜 History & Archive

- **[Project History](../history/)**: Archive of old feature plans, proposals, and development logs.

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
