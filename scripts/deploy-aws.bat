@echo off
setlocal

:: Configuration - Update these values if necessary
set REGION=eu-central-1
set AWS_ACCOUNT_ID=426141506813
set ECR_REGISTRY=%AWS_ACCOUNT_ID%.dkr.ecr.%REGION%.amazonaws.com
set CLUSTER_NAME=angular-boot
set BACKEND_SERVICE=angularai-backend-test-service
set FRONTEND_SERVICE=angularai-frontend-service

echo Starting AWS Deployment...

:: Step 0: Login to AWS
:: echo Authenticating with AWS...
:: ws login
:: if %ERRORLEVEL% neq 0 (echo ECR Authentication failed & exit /b %ERRORLEVEL%)

:: Step 1: Authenticate Docker to ECR
echo Authenticating with ECR...
aws ecr get-login-password --region %REGION% | docker login --username AWS --password-stdin %ECR_REGISTRY%
if %ERRORLEVEL% neq 0 (echo ECR Authentication failed & exit /b %ERRORLEVEL%)

:: Step 2: Build and Tag Backend Image
echo Building and tagging Backend image...
docker build -t angularai-backend -f backend/Dockerfile .
docker tag angularai-backend:latest %ECR_REGISTRY%/angularai-backend:latest
if %ERRORLEVEL% neq 0 (echo Backend build/tag failed & exit /b %ERRORLEVEL%)

:: Step 3: Build and Tag Frontend Image
echo Building and tagging Frontend image...
docker build -t angularai-frontend -f frontend/Dockerfile .
docker tag angularai-frontend:latest %ECR_REGISTRY%/angularai-frontend:latest
if %ERRORLEVEL% neq 0 (echo Frontend build/tag failed & exit /b %ERRORLEVEL%)

:: Step 4: Push Images to ECR
echo Pushing Backend image to ECR...
docker push %ECR_REGISTRY%/angularai-backend:latest
if %ERRORLEVEL% neq 0 (echo Backend push failed & exit /b %ERRORLEVEL%)

echo Pushing Frontend image to ECR...
docker push %ECR_REGISTRY%/angularai-frontend:latest
if %ERRORLEVEL% neq 0 (echo Frontend push failed & exit /b %ERRORLEVEL%)

:: Step 5: Restart ECS Services
echo Restarting ECS services to pick up latest images...

echo Updating Backend service...
aws ecs update-service --cluster %CLUSTER_NAME% --service %BACKEND_SERVICE% --force-new-deployment --region %REGION%
if %ERRORLEVEL% neq 0 (echo Backend service update failed.)

echo Updating Frontend service...
aws ecs update-service --cluster %CLUSTER_NAME% --service %FRONTEND_SERVICE% --force-new-deployment --region %REGION%
if %ERRORLEVEL% neq 0 (echo Frontend service update failed.)

echo AWS Deployment complete!
endlocal
