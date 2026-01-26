@echo off
setlocal

echo INFO: This script is a legacy wrapper. 
echo INFO: It is highly recommended to use scripts\deploy-aws.ps1 for a more robust deployment
echo INFO: that correctly handles versioning and Task Definition updates.
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0deploy-aws.ps1"

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: PowerShell deployment failed.
    exit /b %ERRORLEVEL%
)

echo.
echo AWS Deployment complete via PowerShell!
endlocal
