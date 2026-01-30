# Security Improvement Roadmap - Level 2 (Advanced)

Building upon the successful implementation of the initial security roadmap, this document outlines Level 2 improvements to further harden the AngularAI ecosystem.

## Phase 1: Defensive Hardening

### 1.1 API Rate Limiting (Brute-Force Protection)
- **Task**: Implement rate limiting on sensitive endpoints (Login, Registration, Password Reset).
- **Proposed Solution**: 
    - Use **Bucket4j** (Java-based library) integrated into Spring Boot. 
    - This solution is preferred as it works directly within the application container, maintaining compatibility with the "Frontend + Backend in one container" architecture (no Nginx dependency).
    - Focus on limiting by IP and by username for login attempts.
- **Benefit**: Protects against automated brute-force attacks and prevents resource exhaustion (DoS) regardless of the infrastructure setup.

### 1.2 Content Security Policy (CSP) Refinement
- **Task**: Move from a "permissive" CSP to a "strict" CSP by removing `'unsafe-inline'`.
- **Challenge**: Requires refactoring Angular styles to avoid inline `<style>` blocks and potentially using nonces for scripts.
- **Benefit**: Significantly reduces the remaining XSS attack surface.

### 1.3 Dependency Pinning & Subresource Integrity (SRI)
- **Task**: 
    - Ensure all `package.json` dependencies are pinned to exact versions.
    - Implement SRI hashes for any externally hosted scripts/styles (if applicable, though we mostly use local assets).
- **Benefit**: Protects against Supply Chain Attacks where a package update might introduce malicious code.

## Phase 2: Observability & Incident Response (High Detail)

### 2.1 Advanced Audit Logging (The "Truth" Layer)
- **Task**: Transition `ActionLogService` to a structured, high-fidelity auditing system.
- **Data to Persist (The 5 Ws)**:
    - **Who**: Login, User ID, Session ID, and **Client Fingerprint** (User-Agent + IP).
    - **When**: ISO-8601 timestamp with millisecond precision.
    - **Where**: Request URI, HTTP Method, and Source IP (handling `X-Forwarded-For`).
    - **What (Mutation Diffs)**: For all `POST/PUT/PATCH/DELETE` actions, log the JSON "Before" and "After" state (excluding sensitive fields like passwords).
    - **Why**: Success/Failure status, HTTP Status Code, and detailed error messages (e.g., "Invalid CSRF token" vs "Expired Session").
- **Implementation Options Comparison**:

| Feature | **Spring AOP** | **Hibernate Envers** | **JSON File Appender** |
| :--- | :--- | :--- | :--- |
| **Primary Goal** | Log API interactions. | Track database state changes. | External log aggregation. |
| **Scope** | Controller layer (Request/Response). | Database layer (Entities). | All application logs. |
| **Effort** | Low-Medium (Custom Aspects). | Low (Annotations). | Low (Configuration). |
| **Forensic Value** | High for "What was requested". | Highest for "What actually changed". | Medium (Requires parsing). |
| **Performance** | Minimal overhead. | Slight DB write overhead. | Minimal (Asynchronous). |
| **Best Used For** | Logging *intent* and *access*. | Logging *truth* and *history*. | Compliance and ELK/Splunk. |

- **Recommendation**: Use a **Hybrid Approach**. Use **Hibernate Envers** for critical data (Users, Tasks) to ensure a 100% reliable audit trail of data changes, and **Spring AOP** or a Filter for logging authentication attempts and metadata (IP, Session ID).
- **H2 Compatibility**: H2 fully supports Hibernate Envers. Since you are using a single-container Fargate setup with an H2 file, ensure the H2 database file is stored on a **persistent volume (e.g., AWS EFS)**. This ensures that your audit trail (the "Truth" layer) survives container restarts and deployments.

### 2.2 Security Monitoring & Real-time Alerts
- **Task**: Proactive detection of attack patterns.
- **Triggers for Alerts**:
    - **Credential Stuffing**: >10 failed logins for different users from the same IP within 1 minute.
    - **Brute Force**: >5 failed logins for a single user from any IP within 5 minutes.
    - **Privilege Escalation**: Any successful Role change performed by a non-ADMIN or any attempt to access `/api/admin/**` by a non-privileged user.
    - **Session Hijacking**: A single Session ID changing its User-Agent or IP address during its lifetime.
- **Notification Channels**:
    - **Email/SES**: Direct notification to security admins.
    - **Slack/Webhook**: Real-time channel for development/ops teams.
    - **Health Check Status**: Expose an `/actuator/health/security` endpoint that turns "DOWN" if active attacks are detected.

### 2.3 Forensic Context & Session Tracking
- **Task**: Link all logs across the stack.
- **Proposed Solution**: 
    - Use **MDC (Mapped Diagnostic Context)** to inject `traceId` and `sessionId` into every single application log line.
    - **Benefit**: Allows you to filter logs in any monitoring tool to see the *entire* journey of a single user session across multiple requests.

## Phase 3: Infrastructure & Secrets

### 3.1 Secrets Management (Hybrid Strategy)
- **Task**: Transition from plain-text `.env` files to a secure secrets management solution that works for both local development and production.
- **Proposed Solution (Hybrid Strategy)**:
    - **Production (AWS Fargate)**: Use **AWS Secrets Manager**. Spring Boot can integrate natively via the `spring-cloud-starter-aws-secrets-manager-config` dependency to pull secrets directly into the environment.
    - **Local Development (IDE/Docker)**:
        - **LocalStack**: The best solution for local development. It runs as a Docker container and provides a mock AWS Secrets Manager API. This allows you to use the exact same code and configuration locally as in production.
        - **Mozilla SOPS**: If you prefer to keep it simple without LocalStack, use SOPS to encrypt your local `.env` files. You can safely check the encrypted files into Git.
- **Benefit**: Ensures a high security posture in production while providing a developer-friendly, "cloud-mirroring" experience locally.

### 3.2 Database Persistence (Fargate/H2)
- **Task**: Ensure the H2 database file persists across Fargate task restarts.
- **Proposed Solution**: 
    - Use **AWS EFS (Elastic File System)** mounted to the Fargate container.
    - Configure Spring Boot to store the H2 file on the EFS mount point (e.g., `/data/angularai.mv.db`).
- **Benefit**: Retains all application data and the complete Hibernate Envers audit history without the cost of an RDS instance.

### 3.3 Container Security Scanning
- **Task**: Integrate **Trivy** or **Snyk** into the CI/CD pipeline to scan Docker images for vulnerabilities in the OS layer.
- **Benefit**: Detects vulnerabilities in the base images (Alpine/Temurin) before they are deployed.

## Summary of Goals

| Goal | Component | Complexity | Priority |
|------|-----------|------------|----------|
| Rate Limiting | Backend/Nginx | Medium | High |
| Strict CSP | Frontend | High | Medium |
| Image Scanning | CI/CD | Low | High |
| Secrets Manager | Infrastructure | Medium | Medium |
| H2 Persistence | Infrastructure | Low | High |
