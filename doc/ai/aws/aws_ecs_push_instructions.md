# Instructions for Creating and Pushing Docker Images to Amazon ECR

This guide provides step-by-step instructions on how to build your AngularAI Docker images and push them to Amazon Elastic Container Registry (ECR).

## Prerequisites

1.  **AWS CLI Installed**: Ensure the [AWS CLI](https://aws.amazon.com/cli/) is installed and configured with your credentials.
2.  **Docker Installed**: Ensure Docker is running on your machine.
3.  **AWS Permissions**: Your AWS user/role must have permissions to create ECR repositories and push images (`AmazonEC2ContainerRegistryFullAccess` or similar).

---

## Step 1: Authenticate Docker to ECR

Before pushing, you must authenticate your Docker client to your AWS registry.

**Note on AWS CLI Authentication:** If you use AWS SSO, ensure you are logged in first:
```bash
aws sso login
```
*If you see "Missing the following required SSO configuration values", you need to configure your SSO profile first:*
```bash
aws configure sso
```
**SSO Configuration Values:**
- **SSO session name**: A name for your session (e.g., `angularai-session`).
- **SSO start URL**: Your AWS access portal URL (e.g., `https://d-xxxxxxxxxx.awsapps.com/start`). You can find this in the AWS IAM Identity Center console.
- **SSO region**: The AWS Region where IAM Identity Center is located (e.g., `eu-central-1`).
- **SSO registration scopes**: Leave as default (`sso:account:access`).

**Note for PowerShell users:** Do not use angle brackets (e.g., `<REGION>`) as they are reserved for redirection. Use plain text instead.

```bash
aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 426141506813.dkr.ecr.eu-central-1.amazonaws.com
---

## Step 2: Create ECR Repositories

If you haven't already created the repositories in the AWS Console, you can do it via the CLI:

```bash
# Create Backend Repository
aws ecr create-repository --repository-name angularai-backend --region eu-central-1

# Create Frontend Repository
aws ecr create-repository --repository-name angularai-frontend --region eu-central-1
```

---

## Step 3: Build and Tag Docker Images

**CRITICAL**: You must run these commands from the **project root directory** (`angularai/`), not from the `doc/` directory.

```bash
# Verify you are in the root directory (should see backend/ and frontend/ folders)
ls
```

### 3.1 Backend Image
```bash
# Build the image
docker build -t angularai-backend -f backend/Dockerfile .
```
```bash
# Tag for ECR
docker tag angularai-backend:latest 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend:latest
```

### 3.2 Frontend Image
```bash
# Build the image
docker build -t angularai-frontend -f frontend/Dockerfile .
```
```bash
# Tag for ECR
docker tag angularai-frontend:latest 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-frontend:latest
```

---

## Step 4: Push Images to ECR

Before pushing, ensure your session is still authenticated (logins expire after 12 hours).

**Note on "session expired":** If you see an error like `Your session has expired. Please reauthenticate using 'aws login'`, or `Missing the following required SSO configuration values`, run:
```bash
# If configuration is missing
aws configure sso
```
```bash
# To log in
aws sso login
```
*Note: Depending on your AWS CLI configuration, you might need to use `aws login` or follow the specific authentication method your organization uses.*

```bash
# To log in
aws login
```

```bash
# Re-authenticate Docker to ECR if necessary
aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 426141506813.dkr.ecr.eu-central-1.amazonaws.com
```

Now push the tagged images to your AWS registry.

```bash
# Push Backend
docker push 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend:latest
```
```bash
# Push Frontend
docker push 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-frontend:latest
```

---

## Summary of URLs

After pushing, your image URLs will follow this pattern:
- **Backend**: `426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend:latest`
- **Frontend**: `426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-frontend:latest`

Use these URLs in your **ECS Task Definitions** (e.g., in `deploy/aws/backend-task-definition.json` and `deploy/aws/frontend-task-definition.json`).
