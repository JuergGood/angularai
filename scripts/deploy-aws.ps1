# AWS Deployment Script
# This script builds Docker images, pushes them to ECR, and restarts ECS services.

# Load environment variables from .env file
if (Test-Path ".\scripts\load-env.ps1") {
    . .\scripts\load-env.ps1
}

# Configuration - Update these values if necessary
$REGION = "eu-central-1"
$AWS_ACCOUNT_ID = "426141506813"
$ECR_REGISTRY = "$AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"
$CLUSTER_NAME = "angular-boot" # Update with your actual cluster name
$BACKEND_SERVICE = "angularai-backend-test-service"
# $BACKEND_SERVICE = "backend-service"
$FRONTEND_SERVICE = "angularai-frontend-service"

# Set location to the script's directory to ensure paths are relative to it
Set-Location $PSScriptRoot
# Then move up one level to the project root
Set-Location ..

# Dynamically get version from pom.xml
[xml]$pom = Get-Content "pom.xml"
$VERSION = $pom.project.version
Write-Host "Detected version from pom.xml: $VERSION" -ForegroundColor Cyan

Write-Host "Starting AWS Deployment..." -ForegroundColor Cyan

# Step 1: Authenticate Docker to ECR
Write-Host "Authenticating with ECR..." -ForegroundColor Yellow
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_REGISTRY
if ($LASTEXITCODE -ne 0) { Write-Error "ECR Authentication failed"; exit }

#  aws ecr get-login-password --region eu-central-1 | docker login --username AWS --password-stdin 426141506813.dkr.ecr.eu-central-1.amazonaws.com

# Step 2: Build and Tag Backend Image
Write-Host "Building and tagging Backend image..." -ForegroundColor Yellow
docker build --no-cache -t angularai-backend -f backend/Dockerfile .
docker tag angularai-backend:latest "$ECR_REGISTRY/angularai-backend:$VERSION"
docker tag angularai-backend:latest "$ECR_REGISTRY/angularai-backend:latest"
if ($LASTEXITCODE -ne 0) { Write-Error "Backend build/tag failed"; exit }

# Step 3: Build and Tag Frontend Image
Write-Host "Building and tagging Frontend image..." -ForegroundColor Yellow
docker build --no-cache -t angularai-frontend -f frontend/Dockerfile .
docker tag angularai-frontend:latest "$ECR_REGISTRY/angularai-frontend:$VERSION"
docker tag angularai-frontend:latest "$ECR_REGISTRY/angularai-frontend:latest"
if ($LASTEXITCODE -ne 0) { Write-Error "Frontend build/tag failed"; exit }

# Step 4: Push Images to ECR
Write-Host "Pushing Backend image to ECR..." -ForegroundColor Yellow
docker push "$ECR_REGISTRY/angularai-backend:$VERSION"
docker push "$ECR_REGISTRY/angularai-backend:latest"
if ($LASTEXITCODE -ne 0) { Write-Error "Backend push failed"; exit }

Write-Host "Pushing Frontend image to ECR..." -ForegroundColor Yellow
docker push "$ECR_REGISTRY/angularai-frontend:$VERSION"
docker push "$ECR_REGISTRY/angularai-frontend:latest"
if ($LASTEXITCODE -ne 0) { Write-Error "Frontend push failed"; exit }

# Step 5: Update Task Definitions and Restart ECS Services
Write-Host "Updating Task Definitions and ECS services to pick up latest images..." -ForegroundColor Yellow

function Update-EcsService {
    param (
        [string]$ServiceName,
        [string]$ContainerName,
        [string]$RepoName,
        [string]$Version
    )

    Write-Host "Processing service: $ServiceName" -ForegroundColor Gray

    # 1. Get current service to find the Task Definition ARN
    $service = aws ecs describe-services --cluster $CLUSTER_NAME --services $ServiceName --region $REGION | ConvertFrom-Json
    $taskDefArn = $service.services[0].taskDefinition

    # 2. Describe the task definition
    $taskDef = aws ecs describe-task-definition --task-definition $taskDefArn --region $REGION | ConvertFrom-Json
    $taskDef = $taskDef.taskDefinition

    # 3. Create a clean container definition for registration
    # Remove fields not allowed in register-task-definition
    $containerDefinitions = $taskDef.containerDefinitions
    foreach ($container in $containerDefinitions) {
        if ($container.name -eq $ContainerName) {
            $container.image = "$ECR_REGISTRY/" + $RepoName + ":" + $Version
            Write-Host "Updating image to: $($container.image)" -ForegroundColor Gray
        }
    }

    # Prepare Task Definition for registration (clean up fields)
    $registerParams = @{
        family = $taskDef.family
        containerDefinitions = $containerDefinitions
        volumes = $taskDef.volumes
        placementConstraints = $taskDef.placementConstraints
        requiresCompatibilities = $taskDef.requiresCompatibilities
        cpu = $taskDef.cpu
        memory = $taskDef.memory
    }

    if ($taskDef.executionRoleArn) { $registerParams.executionRoleArn = $taskDef.executionRoleArn }
    if ($taskDef.taskRoleArn) { $registerParams.taskRoleArn = $taskDef.taskRoleArn }
    if ($taskDef.networkMode) { $registerParams.networkMode = $taskDef.networkMode }
    if ($taskDef.runtimePlatform) { $registerParams.runtimePlatform = $taskDef.runtimePlatform }

    $registerJson = $registerParams | ConvertTo-Json -Depth 20

    # 4. Register new Task Definition
    Write-Host "Registering new Task Definition revision for $($taskDef.family)..."
    $newFile = [System.IO.Path]::Combine([System.IO.Path]::GetTempPath(), "task-def-$($ServiceName).json")
    [System.IO.File]::WriteAllText($newFile, $registerJson)
    $newTaskDef = aws ecs register-task-definition --cli-input-json "file://$newFile" --region $REGION | ConvertFrom-Json
    Remove-Item $newFile
    
    $newTaskDefArn = $newTaskDef.taskDefinition.taskDefinitionArn
    Write-Host "New Task Definition ARN: $newTaskDefArn" -ForegroundColor Gray

    # 5. Update service with new Task Definition
    Write-Host "Updating service $ServiceName to use new revision..."
    aws ecs update-service --cluster $CLUSTER_NAME --service $ServiceName --task-definition $newTaskDefArn --force-new-deployment --region $REGION --query "service.serviceName" --output text
}

Write-Host "Updating Backend service..."
Update-EcsService -ServiceName $BACKEND_SERVICE -ContainerName "backend" -RepoName "angularai-backend" -Version $VERSION
if ($LASTEXITCODE -ne 0) { Write-Warning "Backend service update failed." }

Write-Host "Updating Frontend service..."
Update-EcsService -ServiceName $FRONTEND_SERVICE -ContainerName "frontend" -RepoName "angularai-frontend" -Version $VERSION
if ($LASTEXITCODE -ne 0) { Write-Warning "Frontend service update failed." }

Write-Host "AWS Deployment complete!" -ForegroundColor Green
