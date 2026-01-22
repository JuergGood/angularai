# Local Docker Deployment
# Load environment variables from .env file
if (Test-Path ".\scripts\load-env.ps1") {
    . .\scripts\load-env.ps1
}

docker compose up --build -d

# docker compose stop
# docker compose down
