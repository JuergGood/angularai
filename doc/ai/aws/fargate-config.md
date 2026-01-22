# Deployment Configuration for AWS Fargate and Docker

This document outlines the required environment variables and configuration for deploying the AngularAI backend to containerized environments.

## Environment Variables

The following environment variables should be configured in the container environment (e.g., in the ECS Task Definition or `docker-compose.yml`).

| Variable Name | Description | Default Value (Dev/Test) |
|---------------|-------------|--------------------------|
| `SPRING-PROFILES-ACTIVE` | Active Spring profile (e.g., `prod`, `h2`, `default`). | `default` |
| `IPSTACK-API-KEY` | API Access Key for [ipstack.com](https://ipstack.com) geographic location services. | `(Required)` |

## Configuration Details

### 1. Spring Boot (`application.properties`)
The backend is configured to look for the `IPSTACK-API-KEY` environment variable.

```properties
ipstack.api.key=${IPSTACK-API-KEY}
```

### 2. AWS Fargate (Task Definitions)
The ECS task definitions in `deploy/aws/` have been updated to include these environment variables. When deploying, ensure that the `IPSTACK-API-KEY` in the task definition is set to your production key.

### 3. Docker Compose
The `docker-compose.yml` file uses the following syntax to allow overriding via local environment variables (typically from a `.env` file):

```yaml
environment:
  - IPSTACK-API-KEY=${IPSTACK-API-KEY}
```

## Security Best Practices
- **Never** commit sensitive production API keys to the repository.
- Use **AWS Secrets Manager** or **Parameter Store** to manage production secrets and reference them in your ECS Task Definition.
