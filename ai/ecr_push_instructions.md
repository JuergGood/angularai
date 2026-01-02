# Instructions for Creating and Pushing Docker Images to Amazon ECR

This guide provides step-by-step instructions on how to build your AngularAI Docker images and push them to Amazon Elastic Container Registry (ECR).

## Prerequisites

1.  **AWS CLI Installed**: Ensure the [AWS CLI](https://aws.amazon.com/cli/) is installed and configured with your credentials.
2.  **Docker Installed**: Ensure Docker is running on your machine.
3.  **AWS Permissions**: Your AWS user/role must have permissions to create ECR repositories and push images (`AmazonEC2ContainerRegistryFullAccess` or similar).

---

## Step 1: Authenticate Docker to ECR

Before pushing, you must authenticate your Docker client to your AWS registry. Replace `<REGION>` with your AWS region (e.g., `us-east-1`) and `<AWS_ACCOUNT_ID>` with your 12-digit account ID.

```bash
aws ecr get-login-password --region <REGION> | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com
```

---

## Step 2: Create ECR Repositories

If you haven't already created the repositories in the AWS Console, you can do it via the CLI:

```bash
# Create Backend Repository
aws ecr create-repository --repository-name angularai-backend --region <REGION>

# Create Frontend Repository
aws ecr create-repository --repository-name angularai-frontend --region <REGION>
```

---

## Step 3: Build and Tag Docker Images

Navigate to the project root directory.

### 3.1 Backend Image
```bash
# Build the image
docker build -t angularai-backend -f backend/Dockerfile .

# Tag for ECR
docker tag angularai-backend:latest <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/angularai-backend:latest
```

### 3.2 Frontend Image
```bash
# Build the image
docker build -t angularai-frontend -f frontend/Dockerfile .

# Tag for ECR
docker tag angularai-frontend:latest <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/angularai-frontend:latest
```

---

## Step 4: Push Images to ECR

Now push the tagged images to your AWS registry.

```bash
# Push Backend
docker push <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/angularai-backend:latest

# Push Frontend
docker push <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/angularai-frontend:latest
```

---

## Summary of URLs

After pushing, your image URLs will follow this pattern:
- **Backend**: `<AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/angularai-backend:latest`
- **Frontend**: `<AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/angularai-frontend:latest`

Use these URLs in your **ECS Task Definitions** (e.g., in `deploy/aws/backend-task-definition.json` and `deploy/aws/frontend-task-definition.json`).
