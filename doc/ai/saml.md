# Proposal: Introducing SAML for AngularAI

This document assesses the necessity and feasibility of introducing SAML (Security Assertion Markup Language) as an authentication mechanism for the AngularAI project, comparing it with the current Login/Password (Basic Auth) setup.

## 1. Executive Summary

While SAML provides significant benefits for enterprise-level Single Sign-On (SSO) and centralized identity management, it introduces considerable complexity for a multi-client ecosystem (Web, Android, CLI). For the current scope of AngularAI, SAML is **not strictly required** for security but would be a strategic move if the application aims for enterprise integration.

## 2. SAML vs. Login/Password (Current Setup)

### 2.1. Comparison Matrix

| Feature | Current (Login/Password + Basic Auth) | SAML 2.0 |
| :--- | :--- | :--- |
| **User Experience** | Simple login form. | Redirects to Identity Provider (IdP); SSO. |
| **Security** | Credentials stored in local DB (BCrypt). | Credentials managed by IdP; no local passwords. |
| **Complexity** | Low; already implemented. | High; requires IdP setup and SP integration. |
| **MFA Support** | Requires custom implementation. | Inherited from IdP (e.g., AWS, Okta). |
| **Centralization** | Decentralized (local to app). | Centralized (across multiple apps). |

### 2.2. Impact on Clients

#### Angular (Web)
- **Pros**: Seamless SSO if already logged into the IdP.
- **Cons**: Complex flow (Redirects, HTTP POST binding). Requires handling session expiration via IdP.

#### Android (Mobile)
- **Pros**: Can use system browser for login (more secure).
- **Cons**: Challenging implementation. Requires Custom Tabs or WebView. Handling the callback (Deep Linking) is necessary.

#### TestClient (CLI)
- **Pros**: None.
- **Cons**: **Major Drawback.** SAML is designed for browser-based flows. For a CLI/REST client, "Programmatic SAML" is difficult and usually requires an "Exchange" flow (e.g., getting a Token) or using an API Key/Personal Access Token as a bypass.

## 3. Is SAML Required for Security?

**Short Answer: No.**
Application security is currently well-handled by:
1.  **Spring Security**: Robust authorization and authentication.
2.  **BCrypt**: Secure password hashing.
3.  **HTTPS**: Encrypts data in transit (AWS Deployment).

SAML becomes "required" only when:
-   The client demands **Single Sign-On** across multiple company applications.
-   There is a requirement to **never store passwords** in the application database.
-   Compliance (SOC2, etc.) mandates centralized identity auditing.

## 4. Recommended SAML Identity Provider (IdP) Setup

If SAML is to be introduced, here are the recommendations:

### Option A: AWS IAM Identity Center (Successor to AWS SSO) - *Recommended*
-   **Pros**: Native integration with AWS environment; Managed service (no maintenance); Scales automatically.
-   **Cons**: Cloud-only; Costly if advanced features are needed (though basic SSO is often free/low cost).

### Option B: Keycloak in Docker on AWS (Fargate)
-   **Pros**: Full control; Open source; Can be customized.
-   **Cons**: You must manage the database, backups, and security updates for Keycloak itself.

### Option C: Keycloak in Docker on Local Windows
-   **Pros**: Good for development/testing.
-   **Cons**: Not suitable for production AWS deployment unless bridged via VPN/Complex networking.

### Option D: Windows Installation (ADFS)
-   **Pros**: Uses existing Active Directory.
-   **Cons**: Extremely high complexity; requires specialized Windows Server knowledge.

**Recommendation**: Use **AWS IAM Identity Center** for production and a local **Keycloak Docker container** for development.

## 5. IdP and User Admin Integration

### Can the IdP use the users of the User Admin page?
Normally, SAML works the other way around: the **IdP is the source of truth**, and the application (Service Provider) creates/updates local users based on the SAML assertion (Just-In-Time Provisioning).

-   **Integration Path**: To keep using the User Admin page while using SAML, the application would need to act as an IdP (very complex) or use a "Federated" approach where Keycloak/AWS SSO connects to the AngularAI database as a user source (User Storage Provider).
-   **Recommendation**: Transition the "User Admin" to manage roles/permissions only, while the IdP manages the actual identities (Login/Email/Password).

## 6. Proposed Implementation Strategy (Phased)

1.  **Phase 1: Token-based Auth (Preparation)**: Switch from Basic Auth to JWT. This simplifies SAML integration as SAML can be used to *get* a JWT.
2.  **Phase 2: SAML SP Implementation**: Add `spring-security-saml2-service-provider` to the backend.
3.  **Phase 3: Hybrid Login**: Allow both SAML and local login (for the TestClient and initial admin access).
4.  **Phase 4: Full Migration**: Disable local passwords for standard users.

---
*Proposal created on 2026-01-09*
