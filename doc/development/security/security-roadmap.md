# Security Improvement Roadmap

Based on the [Security Assessment](security-assessment.md), the following improvements are proposed to enhance the security posture of the AngularAI project.

## Phase 1: High Priority (Short Term)

### 1.1 Secure Authentication Mechanism
- **Task**: Replace HTTP Basic Authentication with JWT (JSON Web Tokens) or Statefull Sessions.
- **Benefit**: Decouples credentials from every request. Allows for better token management (expiry, revocation).
- **Impact**: Backend (`SecurityConfig`, `AuthController`) and Frontend (`AuthService`, Interceptor needed).

### 1.2 Enable CSRF Protection
- **Task**: Enable CSRF protection in Spring Security.
- **Benefit**: Protects against Cross-Site Request Forgery attacks.
- **Impact**: Requires Angular to handle CSRF tokens (usually via `HttpClientXsrfModule`).

### 1.3 Secure Storage in Frontend
- **Task**: Move away from storing Base64 credentials in `localStorage`.
- **Benefit**: Reduces impact of XSS attacks.
- **Impact**: If using JWT, store in a `HttpOnly` cookie if possible, or at least use shorter-lived tokens.

## Phase 2: Medium Priority (Medium Term)

### 2.1 Tighten CORS Configuration
- **Task**: Replace wildcard origin patterns in `SecurityConfig` with a whitelist of allowed production domains.
- **Benefit**: Prevents unauthorized domains from making requests to the API.

### 2.2 Security Headers
- **Task**: Configure standard security headers in the backend and the Nginx frontend proxy.
    - `Content-Security-Policy` (CSP)
    - `Strict-Transport-Security` (HSTS)
    - `X-Content-Type-Options: nosniff`
    - `X-Frame-Options: DENY` (or `SAMEORIGIN`)
- **Benefit**: Hardens the application against various browser-based attacks.

### 2.3 Automated Security Scanning
- **Task**: Integrate `OWASP Dependency-Check` into the Maven build and `npm audit` into the CI/CD pipeline.
- **Benefit**: Early detection of known vulnerabilities in third-party libraries.

## Phase 3: Best Practices (Ongoing)

### 3.1 Rate Limiting
- **Task**: Implement rate limiting on sensitive endpoints (Login, Registration, Forgot Password).
- **Benefit**: Mitigates brute-force and Denial of Service (DoS) attacks.

### 3.2 Enhanced Input Validation
- **Task**: Use Bean Validation (`jakarta.validation`) across all DTOs to ensure strict data types and constraints.
- **Benefit**: Prevents malformed data from reaching the service layer.

### 3.3 Penetration Testing
- **Task**: Conduct regular manual security reviews or use DAST (Dynamic Application Security Testing) tools like OWASP ZAP.

## Summary of Changes Required

| Component | Target File(s) | Change Summary |
|-----------|----------------|----------------|
| Backend | `SecurityConfig.java` | Enable CSRF, Switch to JWT/Session, tighten CORS. |
| Backend | `pom.xml` | Add security scanning plugins. |
| Frontend | `AuthService.ts` | Update login/init to handle JWT/Session. Remove credentials from `localStorage`. |
| Frontend | `app.config.ts` | Add `HttpClientXsrfModule` support. |
| Infrastructure | `nginx.conf` | Add security headers. |
