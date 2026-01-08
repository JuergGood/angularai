@echo off
echo Starting Local Docker Deployment...
docker compose up --build -d
if %ERRORLEVEL% neq 0 (
    echo Local deployment failed!
    exit /b %ERRORLEVEL%
)
echo Local deployment complete!
