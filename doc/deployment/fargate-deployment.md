# AWS Fargate Deployment Guide

This document outlines the specific configurations and considerations for deploying the AngularAI application to AWS Fargate.

## Infrastructure Requirements

### 1. Application Load Balancer (ALB)
Fargate tasks should be placed behind an ALB. The ALB must be configured to handle:
- **HTTP/HTTPS**: Listen on port 80/443 and forward to the Fargate tasks on port 8080.
- **Health Checks**: Configure health checks to point to `/api/system/info`.

### 2. Sticky Sessions
Because the application uses **Stateful Sessions** (stored in the application's memory), you **MUST** enable Sticky Sessions (Target Group Stickiness) on the ALB.
- **Why**: Without sticky sessions, subsequent requests (containing the `JSESSIONID` cookie) might be routed to a different Fargate task that doesn't have the session data, leading to unauthorized errors.
- **Configuration**:
    - Stickiness type: `Load balancer generated cookie`.
    - Duration: e.g., 1 day.

### 3. Distributed Session Store (Optional/Recommended for High Availability)
If you need true high availability without relying on sticky sessions, you should transition to a distributed session store like **Redis**.
- **Implementation**: Add `spring-session-data-redis` to `pom.xml` and configure `spring.data.redis.host`.
- **Note**: The current implementation is optimized for single-node or sticky-session multi-node deployments.

## Backend Configuration

### 1. Trusted Proxies
The application is configured to trust `X-Forwarded-*` headers via `server.forward-headers-strategy=native` in `application.properties`. 

### 2. Security Headers
The application automatically provides security headers (`HSTS`, `CSP`, etc.). Ensure that the ALB does not strip these headers.

## Android and Test Client
Both the Android application and the Test Client are compatible with stateful sessions and CSRF protection. They automatically:
1. Store the `JSESSIONID` cookie returned after login.
2. Store the `XSRF-TOKEN` cookie.
3. Send the `X-XSRF-TOKEN` header for all mutating requests (POST, PUT, DELETE).

### 4. Service Decommissioning (Cleanup)
Since the application is now deployed as a single container (Backend + Frontend), the previous separate frontend service is no longer needed.
- **Action**: You can safely **stop and delete** the `angularai-frontend-service` on ECS once you have verified that the `angularai-backend-test-service` is handling all traffic correctly via the ALB.
- **Cost Saving**: Removing the redundant service reduces your Fargate operational costs.

## Deployment Steps

### 1. Prerequisites & Login
Before deploying, ensure you are authenticated with AWS and Docker.

#### Disable AWS Pager (Recommended)
To avoid having to press space for long outputs (the `--more--` prompt), you can disable the AWS CLI pager for your session:

PowerShell:
```bash
$env:AWS_PAGER = ""
```

Bash:
```bash
export AWS_PAGER=""
```

Alternatively, you can append `--no-cli-pager` to any AWS CLI command.

#### Permanent Fix (Windows)
If you want to disable the pager permanently on your Windows machine, run this command in an Administrator PowerShell:
```bash
setx AWS_PAGER ""
```
*Note: You will need to restart your terminal for this to take effect.*

#### Login to AWS
Login to AWS:
```bash
aws sso login
```

Create ECR Repository (one-time prerequisite):
```bash
aws ecr create-repository --repository-name angularai-app --region eu-central-1 --image-scanning-configuration scanOnPush=true --encryption-configuration encryptionType=AES256
```
*Note: If the repository already exists, you can safely ignore the error.*

Authenticate Docker with Amazon ECR:
```bash
aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 426141506813.dkr.ecr.eu-central-1.amazonaws.com
```

### 2. Build and Deploy
1. Build the Docker image using the provided `Dockerfile`.
2. Push the image to Amazon ECR.
3. Create a Fargate Task Definition.
4. Create a Fargate Service with an ALB.
5. **CRITICAL**: Enable Sticky Sessions on the Target Group.
