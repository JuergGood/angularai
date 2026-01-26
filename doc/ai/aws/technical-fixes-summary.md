### Technical Summary of Deployment & Security Fixes

This document provides a technical deep-dive into the critical issues resolved during the preparation of the AWS Fargate and local Docker deployments.

---

#### 1. Local Docker Environment Optimization

**Symptoms:**
- Build times were very slow (several minutes).
- Build context transfer was massive (~5GB).
- Email verification links failed with `ERR_CONNECTION_REFUSED`.
- reCAPTCHA was missing or throwing `401 Unauthorized` errors.

**Root Causes:**
1.  **Build Context Bloat**: The Docker daemon was receiving the entire project directory, including `node_modules`, `target`, and `.git` folders, which are unnecessary for the container image.
2.  **Environment Mismatch**: The `APP_BASE_URL` was defaulting to `http://localhost:4200` (Angular Dev Server) in the backend, but in Docker, Nginx handles traffic on port `80`.
3.  **Strict Security**: Public endpoints like `/api/system/recaptcha-site-key` were requiring authentication by default.

**Solutions:**
- **Docker Context Optimization**: Created a comprehensive `.dockerignore` file. This reduced the build context from **5GB to <1MB**, drastically speeding up builds.
- **Dynamic APP_BASE_URL**: Updated `docker-compose.yml` to use `${APP_BASE_URL:-http://localhost}`. This ensures that the backend automatically generates correct links for the Docker environment (port 80) without manual `.env` edits.
- **Security Bypass**: Configured `SecurityConfig.java` to `permitAll()` for `/api/system/**` and `/api/auth/**`, ensuring reCAPTCHA and registration work without a session.
- **Email Flow Refactoring**: Moved the verification logic to a dedicated frontend route (`/verify?token=...`). The backend now returns JSON instead of 302 redirects, allowing the UI to handle error states gracefully.

---

#### 2. Google reCAPTCHA Integration Fixes

**Symptoms:** 
- The registration page displayed "Invalid site key" or didn't show the reCAPTCHA at all.
- Browser console showed `401 Unauthorized` for `/api/system/recaptcha-site-key`.

**Root Causes:**
1.  **Authentication Filter**: The endpoint providing the site key was behind Spring Security's default filter. 
2.  **Config Selection**: The system lacked a way to switch between local-friendly keys (v2 checkbox) and production-grade Enterprise keys (score-based).

**Solutions:**
- **Public Accessibility**: Updated `SecurityConfig.java` to allow unauthenticated access to system configuration endpoints.
- **Robust Config Management**: Enhanced `SystemSettingService.java` to support multiple reCAPTCHA modes. 
- **Migration-Driven Defaults**: Added Flyway migration `V5__add_system_settings.sql` to initialize the `recaptcha_config_index` to `2` (Localhost visible), ensuring immediate functionality in local Docker.

---

#### 3. AWS Secrets Manager & Password Store

**Symptoms:**
- The Backend container failed to start in AWS or used the default password (`admin123`) instead of the one in Secrets Manager.
- ECS logs showed errors related to environment variable injection.

**Root Causes:**
1.  **Format Mismatch**: AWS Secrets Manager was originally storing the configuration as a plain text string. However, the ECS Task Definition expected a JSON structure where specific keys (like `ADMIN_PASSWORD`) could be addressed individually using the `::` suffix in the ARN.
2.  **Incomplete Secret**: Several required variables (like `SES_USERNAME`, `RECAPTCHA_3_SITE_KEY`) were missing from the secret object.

**Solutions:**
- **JSON Formatting**: Re-formatted the AWS Secret as a valid JSON object. This allows ECS to perform "Secret Injection" where it pulls specific fields from the JSON and maps them to individual environment variables in the container.
- **Task Definition Update**: Refined the `secrets` section in `backend-task-definition.json` to use the correct `valueFrom` syntax: `arn:aws:secretsmanager:[region]:[account]:secret:[secret-name]:[json-key]::`.

---

#### 4. IAM Permissions (Execution Role)

**Symptoms:**
- ECS Tasks stayed in `PENDING` state or failed immediately with `ResourceInitializationError`.
- Error message: `AccessDenied` when calling `GetSecretValue`.

**Root Cause:**
- The **ECS Task Execution Role** (`ecsTaskExecutionRole`) is responsible for pulling the Docker image from ECR and fetching secrets from Secrets Manager *before* the container starts. By default, this role only has basic logging and ECR pull rights. It lacked the `secretsmanager:GetSecretValue` permission.

**Solution:**
- **IAM Policy Attachment**: Attached a policy (or added inline permissions) to the `ecsTaskExecutionRole` granting it `secretsmanager:GetSecretValue` for the specific ARN of the `goodone-config` secret. This "unlocked" the ability for Fargate to inject the passwords into the backend.

---

#### 5. Backend Crash (Data Integrity Violation)

**Symptoms:**
- Backend crashed during startup in Fargate with `ConstraintViolationException` on the `USERS` table.

**Root Cause:**
- `DataInitializer.java` was attempting to create default users (`admin`, `user`). Because the environment variables for `ADMIN_EMAIL` etc., were initially empty or identical in the AWS secret, the code tried to insert multiple users with an empty string as their email. This violated the `UNIQUE` constraint on the `EMAIL` column.

**Solution:**
- **Unique Fallbacks**: Updated `DataInitializer.java` with logic that generates unique internal emails (e.g., `admin@system.local`) if the configured ones are missing or duplicated. This ensures the application always boots successfully regardless of configuration quality.

---

#### Technical Verification
All fixes were verified using:
1.  **Integration Tests**: Created `PublicEndpointsTest.java` and `VerificationIntegrationTest.java` to ensure the logic holds under security constraints.
2.  **AWS Connectivity**: Verified `goodone.ch/api/system/info` returns the correct version and custom landing message from the secret.
3.  **UI Feedback**: Confirmed reCAPTCHA loads correctly on the registration page using the production Enterprise site key.
4.  **Local Docker**: Verified `docker compose up --build` starts in seconds and allows successful user registration and login.