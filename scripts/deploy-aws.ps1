# AWS Deployment Script
# This script builds Docker images, pushes them to ECR, and restarts ECS services.

# Configuration - Update these values if necessary
$REGION = "eu-central-1"
$AWS_ACCOUNT_ID = "426141506813"
$ECR_REGISTRY = "$AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"
$CLUSTER_NAME = "angularai-cluster" # Update with your actual cluster name
$BACKEND_SERVICE = "backend-test-service"
# $BACKEND_SERVICE = "backend-service"
$FRONTEND_SERVICE = "frontend-service"

Write-Host "Starting AWS Deployment..." -ForegroundColor Cyan

# Step 1: Authenticate Docker to ECR
Write-Host "Authenticating with ECR..." -ForegroundColor Yellow
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_REGISTRY
if ($LASTEXITCODE -ne 0) { Write-Error "ECR Authentication failed"; exit }

#  aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 426141506813.dkr.ecr.eu-central-1.amazonaws.com

# Step 2: Build and Tag Backend Image
Write-Host "Building and tagging Backend image..." -ForegroundColor Yellow
docker build -t angularai-backend -f backend/Dockerfile .
docker tag angularai-backend:latest "$ECR_REGISTRY/angularai-backend:latest"
if ($LASTEXITCODE -ne 0) { Write-Error "Backend build/tag failed"; exit }

# Step 3: Build and Tag Frontend Image
Write-Host "Building and tagging Frontend image..." -ForegroundColor Yellow
docker build -t angularai-frontend -f frontend/Dockerfile .
docker tag angularai-frontend:latest "$ECR_REGISTRY/angularai-frontend:latest"
if ($LASTEXITCODE -ne 0) { Write-Error "Frontend build/tag failed"; exit }

# Step 4: Push Images to ECR
Write-Host "Pushing Backend image to ECR..." -ForegroundColor Yellow
docker push "$ECR_REGISTRY/angularai-backend:latest"
if ($LASTEXITCODE -ne 0) { Write-Error "Backend push failed"; exit }

Write-Host "Pushing Frontend image to ECR..." -ForegroundColor Yellow
docker push "$ECR_REGISTRY/angularai-frontend:latest"
if ($LASTEXITCODE -ne 0) { Write-Error "Frontend push failed"; exit }

# Step 5: Restart ECS Services
Write-Host "Restarting ECS services to pick up latest images..." -ForegroundColor Yellow

Write-Host "Updating Backend service..."
aws ecs update-service --cluster $CLUSTER_NAME --service $BACKEND_SERVICE --desired-count 1 --force-new-deployment --region $REGION
if ($LASTEXITCODE -ne 0) { Write-Warning "Backend service update failed. Check if service name/cluster is correct." }

Write-Host "Updating Frontend service..."
aws ecs update-service --cluster $CLUSTER_NAME --service $FRONTEND_SERVICE --desired-count 1 --force-new-deployment --region $REGION
if ($LASTEXITCODE -ne 0) { Write-Warning "Frontend service update failed. Check if service name/cluster is correct." }

Write-Host "AWS Deployment complete!" -ForegroundColor Green
