I have created the AWS ECS Fargate Task Definition configuration files for both the frontend and backend. These files define how your containers should run on AWS, including their resource requirements, networking, and environment variables.

The files are located in the new `deploy/aws/` directory:

### 1. Backend Task Definition
**File**: `deploy/aws/backend-task-definition.json`
*   **CPU/Memory**: 256 CPU units, 512 MiB Memory.
*   **Port**: 8080.
*   **Configuration**: Includes environment variable placeholders for connecting to your AWS RDS PostgreSQL database (`SPRING_DATASOURCE_URL`, etc.).
*   **Profile**: Automatically activates the `postgres` profile.

### 2. Frontend Task Definition
**File**: `deploy/aws/frontend-task-definition.json`
*   **CPU/Memory**: 256 CPU units, 512 MiB Memory.
*   **Port**: 80 (Nginx).
*   **Logging**: Configured to send logs to CloudWatch.

---

### How to use these files

#### A. Prerequisites
1.  **ECR Repositories**: Ensure you have pushed your Docker images to Amazon ECR.
2.  **IAM Role**: Ensure the `ecsTaskExecutionRole` exists in your AWS account (standard role created by AWS).
3.  **RDS Database**: Your PostgreSQL database should be running and accessible.

#### B. Registering Tasks via CLI
You can register these task definitions using the AWS CLI:

```bash
# Register Backend
aws ecs register-task-definition --cli-input-json file://deploy/aws/backend-task-definition.json

# Register Frontend
aws ecs register-task-definition --cli-input-json file://deploy/aws/frontend-task-definition.json
```

#### C. Final Deployment
1.  **Cluster**: Create an ECS Cluster (Fargate type).
2.  **Services**: Create two ECS Services (one for frontend, one for backend) using the registered task definitions.
3.  **Networking**:
    *   The **Backend Service** should be in a private subnet.
    *   The **Frontend Service** should be associated with an Application Load Balancer (ALB) to be accessible from the internet.
    *   The ALB should route `/api/*` requests to the Backend Service and all other requests to the Frontend Service.

> **Note**: Remember to replace the placeholders in the JSON files (like `<AWS_ACCOUNT_ID>`, `<REGION>`, `<RDS_ENDPOINT>`, etc.) with your actual AWS resource details before registering.