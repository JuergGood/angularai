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

```bash
aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 426141506813.dkr.ecr.eu-central-1.amazonaws.com
```
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

### 3.1 Define Release Version
It is highly recommended to use a specific version tag (e.g., `1.0.3`) instead of just `latest` to ensure predictable deployments.

**IMPORTANT**: Ensure that the version defined here matches the version in your project files:
- **Backend**: `pom.xml` (`<version>1.0.3</version>`)
- **Frontend**: `package.json` (`"version": "1.0.3"`)

```bash
VERSION="1.0.3"
```

### 3.2 Backend Image
```bash
# Build the image with version tag
docker build -t angularai-backend:$VERSION -f backend/Dockerfile .

# Also tag as latest (optional)
docker tag angularai-backend:$VERSION angularai-backend:latest
```

### 3.3 Frontend Image
**Note**: Due to peer dependency conflicts between Angular 21 and some plugins, the `frontend/Dockerfile` is configured to use `--legacy-peer-deps`.

```bash
# Build the image with version tag
docker build -t angularai-frontend:$VERSION -f frontend/Dockerfile .

# Also tag as latest (optional)
docker tag angularai-frontend:$VERSION angularai-frontend:latest
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

**IMPORTANT**: If you see an error like `tag does not exist`, it means the local image wasn't successfully tagged with the remote ECR URL. Ensure you have executed the `docker tag` commands in Step 4.1 for the exact `$VERSION` you are trying to push.

### 4.1 Tag for ECR
```bash
# Tag Backend
docker tag angularai-backend:$VERSION 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend:$VERSION
docker tag angularai-backend:latest 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend:latest

# Tag Frontend
docker tag angularai-frontend:$VERSION 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-frontend:$VERSION
docker tag angularai-frontend:latest 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-frontend:latest
```

### 4.2 Push to ECR
```bash
# Push Backend
docker push 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend:$VERSION
docker push 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend:latest

# Push Frontend
docker push 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-frontend:$VERSION
docker push 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-frontend:latest
```

---

## Summary of URLs

After pushing, your image URLs will follow this pattern:
- **Backend**: `426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend:1.0.3`
- **Frontend**: `426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-frontend:1.0.3`

### Updating ECS Task Definitions
To deploy the new version:
1.  Open your Task Definition JSON (e.g., `deploy/aws/backend-task-definition.json`).
2.  Update the `image` field to use the new version tag (e.g., `1.0.3`) instead of `latest`. **This is highly recommended to avoid version confusion.**
3.  Register the new task definition version:
    ```bash
    aws ecs register-task-definition --cli-input-json file://deploy/aws/backend-task-definition.json --query "taskDefinition.revision" --output text
    ```
4.  Update the service to use the new task definition:
    ```bash
    aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --task-definition angularai-backend:REVISION --query "service.taskDefinition" --output text
    ```
    *(Replace `REVISION` with the number returned by the register command)*

### Verifying the Deployment
To verify that the Task Definition is correctly pointing to the intended image version:

```bash
# Verify Backend Task Definition
aws ecs describe-task-definition --task-definition angularai-backend --region eu-central-1 --query "taskDefinition.containerDefinitions[].{name:name, image:image}" --output table

# Verify Frontend Task Definition
aws ecs describe-task-definition --task-definition angularai-frontend --region eu-central-1 --query "taskDefinition.containerDefinitions[].{name:name, image:image}" --output table
```

---

## Troubleshooting: Version Mismatch
If you see an old version (e.g., `0.0.1-SNAPSHOT`) after deployment:
1.  **Check Local Build**: Ensure you ran `mvn clean package` before building the Docker image if you are building outside of Docker, or ensure `backend/Dockerfile` is running `mvn clean package` (it should).
2.  **Explicit Version**: Change `latest` to `$VERSION` in your `backend-task-definition.json` before registering. This guarantees ECS pulls that specific image.
3.  **ECR Verification**: Check the AWS ECR console to ensure the image with tag `1.0.3` has a recent "Pushed at" timestamp.
