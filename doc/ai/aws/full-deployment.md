# Full AWS Deployment Guide: Build & Deploy

This document provides the consolidated, step-by-step instructions for building the AngularAI project, pushing Docker images to ECR, and deploying them to AWS ECS.

---

## 1. Prerequisites & Authentication

### 1.1 AWS SSO Login
If you are using AWS SSO, authenticate your session first:
```bash
aws sso login
```

### 1.2 Authenticate Docker to ECR
Run this to allow Docker to push to your private ECR registry:
```bash
aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 426141506813.dkr.ecr.eu-central-1.amazonaws.com
```

---

## 2. Define Version & Sync
The `$VERSION` variable must match the version defined in `pom.xml` and `package.json`.

```bash
VERSION="1.0.5"
```

---

## 3. Build and Tag Images
Run these commands from the **project root directory**.

### 3.1 Backend
```bash
# Build
docker build -t angularai-backend:$VERSION -f backend/Dockerfile .
```
```bash
# Tag for ECR
docker tag angularai-backend:$VERSION 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend:$VERSION
docker tag angularai-backend:$VERSION 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend:latest
```

*Note: If the `docker push` in the next step fails with "tag does not exist", ensure you didn't miss the tagging commands above or that the `$VERSION` variable is still set in your current terminal session.*

### 3.2 Frontend
```bash
# Build (Uses --legacy-peer-deps internally for Angular 21 compatibility)
docker build -t angularai-frontend:$VERSION -f frontend/Dockerfile .
```
```bash
# Tag for ECR
docker tag angularai-frontend:$VERSION 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-frontend:$VERSION
docker tag angularai-frontend:$VERSION 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-frontend:latest

```

---

## 4. Push to ECR

```bash
# Push Backend
docker push 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend:$VERSION
docker push 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-backend:latest
```
```bash
# Push Frontend
docker push 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-frontend:$VERSION
docker push 426141506813.dkr.ecr.eu-central-1.amazonaws.com/angularai-frontend:latest
```

---

## 5. Deploy to ECS

### 5.1 Update Task Definitions
1.  Open `deploy/aws/backend-task-definition.json` and `deploy/aws/frontend-task-definition.json`.
2.  Update the `image` field to use the new `$VERSION` tag.
3.  For Backend: Ensure `SPRING-PROFILES-ACTIVE` is set to `h2` for troubleshooting.

### 5.2 Backend Deployment
```bash
# 1. Register new task definition and get the numeric REVISION
aws ecs register-task-definition --cli-input-json file://deploy/aws/backend-task-definition.json --query "taskDefinition.revision" --output text --region eu-central-1
```
```bash
# 2. Update service (Replace REVISION with the number from step 1)
# Use 'angularai-backend-test-service' as per requirements
aws ecs update-service --cluster angular-boot --service angularai-backend-test-service --task-definition angularai-backend:6 --query "service.taskDefinition" --output text --region eu-central-1
```

### 5.3 Frontend Deployment
```bash 
# 1. Register new task definition and get the numeric REVISION
aws ecs register-task-definition --cli-input-json file://deploy/aws/frontend-task-definition.json --query "taskDefinition.revision" --output text --region eu-central-1
```
```bash
# 2. Update service (Replace REVISION with the number from step 1)
aws ecs update-service --cluster angular-boot --service angularai-frontend-service --task-definition angularai-frontend:4 --query "service.taskDefinition" --output text --region eu-central-1
```

---

## 6. Verification & Troubleshooting

### 6.1 Verification
Confirm the deployment by checking the system info endpoint:
- **URL**: `https://<your-alb-dns>/api/system/info`
- **Expected Version**: `1.0.5`
- **Expected Mode**: `H2`

### 6.2 Troubleshooting "Old Version Still Displayed"
If you still see `0.0.1-SNAPSHOT` after a successful deployment:

1.  **Check ECR Timestamps**: Go to the AWS Console -> ECR -> `angularai-backend` repository. Check the "Pushed at" time for the `latest` tag and the `1.0.5` tag. If they are old, your `docker push` didn't work.
2.  **Verify Local Tagging**: Run `docker images` and ensure that `angularai-backend:1.0.5` and the ECR-tagged version exist and have a recent "CREATED" time.
3.  **Ensure Push Succeeded**: Re-run the `docker push` commands and look for "Layer already exists" vs "Pushed". If it says "Layer already exists" for everything but the version is wrong, you might be pushing an old local image.
4.  **Clean Local Build**: Sometimes Docker caches old layers. Try building with `--no-cache`:
    ```bash
    docker build --no-cache -t angularai-backend:$VERSION -f backend/Dockerfile .
    ```
5.  **Force ECS Pull**: When using the `latest` tag, ECS sometimes doesn't pull the new image if the tag hasn't changed. The `aws ecs update-service --force-new-deployment` command *should* fix this, but using an explicit version (e.g., `:1.0.5`) in your Task Definition is **much safer**.
6.  **Check Task Definition**: Double-check that you actually ran `aws ecs register-task-definition` **AFTER** updating the JSON file with the new version, and that you used the **new numeric REVISION** in the `update-service` command.

