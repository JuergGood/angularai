I have created the AWS ECS Fargate Task Definition configuration files for both the frontend and backend. These files define how your containers should run on AWS, including their resource requirements, networking, and environment variables.

The files are located in the new `deploy/aws/` directory:

### 1. Backend Task Definition
**File**: `deploy/aws/backend-task-definition.json`
*   **CPU/Memory**: 256 CPU units, 512 MiB Memory.
*   **Port**: 8080.
*   **Configuration**: Includes environment variable placeholders for connecting to your AWS RDS PostgreSQL database (`SPRING-DATASOURCE-URL`, etc.).
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
3.  **CloudWatch Log Groups**: You must manually create the Log Groups in CloudWatch for both services to prevent a `ResourceInitializationError` at startup:
    *   `/ecs/angularai-frontend`
    *   `/ecs/angularai-backend`
    *   `/ecs/angularai-backend-test` (if using the test definition)
    
    You can create them via the AWS Console or CLI:
    ```bash
    aws logs create-log-group --log-group-name /ecs/angularai-frontend
    aws logs create-log-group --log-group-name /ecs/angularai-backend
    aws logs create-log-group --log-group-name /ecs/angularai-backend-test
    ```
4.  **RDS Database**: Your PostgreSQL database should be running and accessible (Only required for `backend-task-definition.json`).

#### B. Registering Tasks via CLI
**CRITICAL**: You must manually edit the `.json` files in `deploy/aws/` and replace all placeholders (e.g., `426141506813`, `eu-central-1`, `YOUR-RDS-ENDPOINT`) with your actual AWS values. 

Failure to replace `426141506813` in the `executionRoleArn` and `taskRoleArn` fields will result in a `ClientException: Role is not valid` error.

**Note for PowerShell users:** Placeholders like `YOUR-RDS-ENDPOINT` are written in plain text. If you see documentation with angle brackets like `<PLACEHOLDER>`, avoid using them in PowerShell as they are reserved for redirection.

Once edited, you can register these task definitions using the AWS CLI:

```bash
# Register Backend
aws ecs register-task-definition --cli-input-json file://deploy/aws/backend-task-definition.json

# Register Test Backend
aws ecs register-task-definition --cli-input-json file://deploy/aws/backend-test-task-definition.json

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

> **Note**: Remember to replace the placeholders in the JSON files (like `<AWS-ACCOUNT-ID>`, `<REGION>`, `<RDS-ENDPOINT>`, etc.) with your actual AWS resource details before registering.
