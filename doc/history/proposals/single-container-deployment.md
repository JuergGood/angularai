### Proposal: Single Container Deployment (Frontend + Backend)

#### Status
- **Date**: 2026-01-29
- **Author**: Junie
- **Status**: Proposed

#### Context
Currently, the application is deployed using two separate containers:
1.  **Frontend**: Angular application served by Nginx.
2.  **Backend**: Spring Boot API.

In AWS ECS Fargate, this requires either two separate services or a task with two containers, which increases operational costs and complexity (requiring an Ingress or ALB to route traffic between them).

#### Objective
Reduce operational costs by deploying both Frontend and Backend in a single Docker container.

#### Proposed Solution: Spring Boot Static Content Hosting

Instead of using Nginx to serve the frontend, we will leverage Spring Boot's ability to serve static content.

##### 1. Build Process Changes
- Modify the build pipeline to build the Angular frontend first.
- Copy the resulting static files (`dist/frontend/*`) into the Spring Boot backend's `src/main/resources/static/` directory during the build process.

##### 2. Spring Boot Configuration
- Implement a `WebMvcConfigurer` to handle Single Page Application (SPA) routing. This ensures that deep links (e.g., `/dashboard`) are redirected to `index.html` so Angular can handle the routing, while `/api/**` calls still reach the REST controllers.

##### 3. Dockerfile Consolidation
- Create a multi-stage `Dockerfile` that:
    - **Stage 1**: Builds the Angular frontend (Node.js).
    - **Stage 2**: Builds the Spring Boot backend (Maven), including the static files from Stage 1.
    - **Stage 3**: Final runtime image (JRE only) containing the fat JAR.

##### 4. Infrastructure Simplification
- Remove the Nginx configuration and service.
- The single container will listen on port 8080 (or 80).
- Simplified ECS Task Definition (only one container definition).
- No need for complex internal networking between frontend and backend containers.

#### Pros and Cons

| Pros | Cons |
| :--- | :--- |
| **Reduced Cost**: Only one Fargate instance/container running. | **Coupled Deployment**: Frontend and backend must be deployed together. |
| **Simpler Architecture**: No need for Nginx or internal proxying. | **Backend Load**: The Spring Boot app handles static file serving (though minimal overhead). |
| **Simplified CI/CD**: One image to build, push, and deploy. | **Build Time**: Slightly longer single-image build time. |

#### Implementation Plan (Summary)
1.  **Update `backend/pom.xml`**: Add a plugin (e.g., `frontend-maven-plugin`) or a manual step in CI to copy frontend assets.
2.  **Add `WebConfiguration.java`**: In the backend to handle SPA routing.
3.  **Create Root `Dockerfile`**: A multi-stage Dockerfile at the project root.
4.  **Update `docker-compose.yml`**: Consolidate into a single service for local testing.
5.  **Update Deployment Scripts**: Update `deploy-aws.ps1` to target the single image.

#### Recommendation
For cost optimization in a Fargate environment, the **Spring Boot Static Hosting** approach is the most practical and standard way to achieve a single-container deployment for a Spring Boot + Angular stack.
