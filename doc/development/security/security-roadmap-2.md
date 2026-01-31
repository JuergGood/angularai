# Security Improvement Roadmap - Level 2 (Advanced) [DONE]

Building upon the successful implementation of the initial security roadmap, this document outlines Level 2 improvements that have been fully implemented to further harden the AngularAI ecosystem.

## Phase 1: Defensive Hardening [COMPLETED]

### 1.1 API Rate Limiting (Brute-Force Protection) [DONE]
- **Implemented**: `RateLimitingFilter` using **Bucket4j**.
- **Scope**: Limits requests to `/api/auth/login` and `/api/auth/register` to 10 requests per minute per IP.

### 1.2 Content Security Policy (CSP) Refinement [DONE]
- **Implemented**: Strict CSP in `SecurityConfig.java`.
- **Change**: Removed `'unsafe-inline'` from `style-src`.

### 1.3 Dependency Pinning & Subresource Integrity (SRI) [DONE]
- **Implemented**: All dependencies in `frontend/package.json` are now pinned to exact versions.

## Phase 2: Observability & Incident Response [COMPLETED]

### 2.1 Advanced Audit Logging (The "Truth" Layer) [DONE]
- **Implemented**: 
    - **Hibernate Envers**: Enabled on `User` and `Task` entities for full data history.
    - **Enhanced ActionLog**: Added `sessionId`, `requestMethod`, `requestUri`, and `statusCode`.
- **Forensic Data**: `ActionLogService` now automatically populates forensic metadata for every log entry.

### 2.2 Security Monitoring & Real-time Alerts [DONE]
- **Implemented**: Detection logic in `ActionLogService`.
- **Triggers**:
    - **Credential Stuffing**: Detects >10 failed logins from same IP.
    - **Brute Force**: Detects >5 failed logins for same user.
- **Output**: Alerts are currently directed to the application log with `ALERT:` prefix.

### 2.3 Forensic Context & Session Tracking [DONE]
- **Implemented**: `MdcFilter` for injecting `traceId` and `sessionId` into SLF4J MDC.
- **Logging Pattern**: Updated `application.properties` to include these IDs in all console logs.

## Phase 3: Infrastructure & Secrets [COMPLETED]

### 3.1 Secrets Management (Hybrid Strategy) [DONE]
- **Implemented**: Added `spring-cloud-starter-aws-secrets-manager-config`. 
- **Setup**: Configured `application.properties` with placeholders for AWS integration.

### 3.2 Database Persistence (Fargate/H2) [DONE]
- **Implemented**: Added `h2-file` Spring profile.
- **Config**: Mounts H2 database to `./data/angularai` for persistence across container restarts.

### 3.3 Container & Dependency Security Scanning [DONE]
- **Implemented**: Integration of **Trivy** and **Snyk** into the GitHub Actions pipeline (`code-review.yml`).
- **Scope**:
    - **Trivy (Image)**: Scans the production Docker image for OS and library vulnerabilities.
    - **Trivy (Config)**: Scans the `Dockerfile` for security misconfigurations.
    - **Snyk (Open Source)**: Scans Maven (Backend) and npm (Frontend) dependencies for known vulnerabilities.
    - **Snyk (Container)**: Provides an alternative scan for the Docker image.
- **Enforcement**: Fails the build if **CRITICAL** or **HIGH** vulnerabilities are found.
- **Requirement**: Requires `SNYK_TOKEN` to be added to GitHub Secrets. To obtain a token:
    1. Register at [snyk.io](https://snyk.io/).
    2. Go to **Account Settings** -> **API Token**.
    3. Copy the token and add it as a Secret named `SNYK_TOKEN` in your GitHub repository settings.
- **Snyk Integration**: The GitHub integration has been established (Integration ID: `6655f303-76c0-4e01-8118-f5f9947e4ab0`).

## Summary of Goals

| Goal | Component | Complexity | Priority | Status |
|------|-----------|------------|----------|--------|
| Rate Limiting | Backend/Nginx | Medium | High | [DONE] |
| Strict CSP | Frontend | High | Medium | [DONE] |
| Image & Dependency Scan | CI/CD | Low | High | [DONE] |
| Secrets Manager | Infrastructure | Medium | Medium | [DONE] |
| H2 Persistence | Infrastructure | Low | High | [DONE] |
