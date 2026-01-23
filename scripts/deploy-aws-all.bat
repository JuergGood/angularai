@echo off
SETLOCAL EnableDelayedExpansion

:: Configuration
SET REGION=eu-central-1
SET AWS_ACCOUNT_ID=426141506813
SET ECR_URL=%AWS_ACCOUNT_ID%.dkr.ecr.%REGION%.amazonaws.com
SET CLUSTER_NAME=angular-boot
SET BACKEND_SERVICE=angularai-backend-test-service
SET FRONTEND_SERVICE=angularai-frontend-service
SET BACKEND_REPO=angularai-backend
SET FRONTEND_REPO=angularai-frontend

echo ==========================================
echo AngularAI: 1-Click Build and Deploy to AWS
echo ==========================================

:: Step 1: Authenticate Docker to ECR
echo [1/5] Authenticating Docker to ECR...
aws ecr get-login-password --region %REGION% | docker login --username AWS --password-stdin %ECR_URL%
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker login failed. Please ensure you are logged in to AWS (aws sso login).
    exit /b %ERRORLEVEL%
)

:: Step 2: Build and Tag Backend
echo [2/5] Building and Tagging Backend Image...
docker build -t %BACKEND_REPO% -f backend/Dockerfile .
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Backend build failed.
    exit /b %ERRORLEVEL%
)
docker tag %BACKEND_REPO%:latest %ECR_URL%/%BACKEND_REPO%:latest

:: Step 3: Build and Tag Frontend
echo [3/5] Building and Tagging Frontend Image...
docker build -t %FRONTEND_REPO% -f frontend/Dockerfile .
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Frontend build failed.
    exit /b %ERRORLEVEL%
)
docker tag %FRONTEND_REPO%:latest %ECR_URL%/%FRONTEND_REPO%:latest

:: Step 4: Push Images to ECR
echo [4/5] Pushing Images to ECR...
docker push %ECR_URL%/%BACKEND_REPO%:latest
docker push %ECR_URL%/%FRONTEND_REPO%:latest

:: Step 5: Trigger ECS Deployment
echo [5/5] Triggering Force New Deployment on ECS...
echo Updating Backend Service: %BACKEND_SERVICE%
aws ecs update-service --cluster %CLUSTER_NAME% --service %BACKEND_SERVICE% --desired-count 1 --force-new-deployment --query "service.serviceName" --output text --region %REGION%

echo Updating Frontend Service: %FRONTEND_SERVICE%
aws ecs update-service --cluster %CLUSTER_NAME% --service %FRONTEND_SERVICE% --desired-count 1 --force-new-deployment --query "service.serviceName" --output text --region %REGION%

echo ==========================================
echo Deployment Triggered Successfully!
echo ==========================================
echo Monitor progress in the AWS ECS Console:
echo https://%REGION%.console.aws.amazon.com/ecs/v2/clusters/%CLUSTER_NAME%/services
pause