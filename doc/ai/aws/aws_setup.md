To run your AngularAI application on AWS using the existing Docker containers, I recommend the following architecture and steps.

### Recommended Database Provider: AWS RDS
For a Spring Boot application using JPA/Hibernate, **AWS RDS (Relational Database Service)** is the highly recommended choice.
*   **Engine**: **PostgreSQL** or **MySQL**. Given the project's current H2 configuration, either will work seamlessly with Spring Boot. PostgreSQL is generally preferred for modern enterprise applications.
*   **Why**: It provides managed backups, patching, and high availability without the overhead of managing a database server yourself.

---

### Deployment Steps on AWS

#### 1. Prepare Docker Images for AWS
You need to host your Docker images in a private registry.
*   **AWS Service**: **Amazon ECR (Elastic Container Registry)**.
*   **Steps**:
    1. Create two ECR repositories: `angularai-frontend` and `angularai-backend`.
    2. Authenticate your local Docker CLI to ECR (see [ECR Push Instructions](aws_ecs_push_instructions.md) for details on authentication and SSO).
    3. Tag your local images with the ECR repository URLs.
    4. Push the images: `docker push 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend`.

#### 2. Set Up the Database
*   **AWS Service**: **Amazon RDS**.
*   **Steps**:
    1. Create a DB Instance (e.g., PostgreSQL).
    2. Configure a Security Group to allow traffic on the DB port (5432 for Postgres) only from the application's future Security Group.
    3. Note the endpoint, database name, username, and password.

#### 3. Configure the Network Infrastructure
*   **AWS Service**: **VPC (Virtual Private Cloud)**.
*   **Steps**:
    1. Use a VPC with public and private subnets.
    2. Create an **Application Load Balancer (ALB)** to handle incoming web traffic.

#### 4. Deploy Containers
*   **AWS Service**: **Amazon ECS (Elastic Container Service) with Fargate**.
*   **Why Fargate**: It's "serverless" for containers—you don't have to manage the underlying EC2 instances.
*   **Steps**:
    1. **Define Task Definitions**:
        - **Backend Task**: Add the backend image URL. Set environment variables to override `application.properties` (e.g., `SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/dbname`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).
        - **Frontend Task**: Add the frontend image URL.
    2. **Create ECS Cluster**: A logical grouping for your services.
    3. **Create ECS Services**:
        - **Backend Service**: Run the backend task. Connect it to a Target Group on the ALB (e.g., path `/api/*`).
        - **Frontend Service**: Run the frontend task. Connect it to the default Target Group on the ALB (path `/`).

#### 5. Domain and SSL (Optional but Recommended)
*   **AWS Services**: **Route 53** and **AWS Certificate Manager (ACM)**.
*   **Steps**:
    1. Provision an SSL certificate in ACM.
    2. Attach the certificate to the ALB listener (Port 443).
    3. Point your domain (Route 53) to the ALB's DNS name.

### Summary of Recommended Stack
| Component | AWS Service |
| :--- | :--- |
| **Frontend** | ECS Fargate (Nginx container) |
| **Backend** | ECS Fargate (Spring Boot container) |
| **Database** | RDS (PostgreSQL) |
| **Registry** | ECR |
| **Routing** | Application Load Balancer |
| **Networking** | VPC |