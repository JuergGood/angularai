# User Workflows Documentation

This document describes the technical workflows for user management in the AngularAI project.

## 1. User Registration Workflow

The user registration process ensures that new users are verified via email and that the system is protected against automated registrations.

For a step-by-step visual guide with screenshots, see the [User Registration UI Guide](../../user-guide/workflows/registration-ui-guide.md).

### 1.1. Registration Sequence Diagram

```mermaid
sequenceDiagram
    participant U as User (Browser)
    participant F as Frontend (Angular)
    participant B as Backend (Spring Boot)
    participant C as Captcha Service
    participant E as Email Service
    participant DB as Database

    U->>F: Enters registration details
    F->>F: Validates form (client-side)
    F->>U: Requests reCAPTCHA token
    U->>F: Provides token
    F->>B: POST /api/auth/register (UserDTO + Captcha)
    
    B->>C: Verify Captcha token
    C-->>B: Captcha valid
    
    B->>DB: Check if login/email exists (non-PENDING)
    DB-->>B: Does not exist (or PENDING only)
    
    Note over B,DB: If PENDING user exists with same login/email, it is deleted (re-registration)
    
    B->>DB: Delete existing PENDING user (if any)
    B->>DB: Create User (Status: PENDING)
    B->>DB: Generate & Save Verification Token (Expiry: 24h)
    
    B->>E: Send Verification Email (with token link)
    E-->>U: Email delivered
    
    B-->>F: 200 OK (UserDTO)
    F-->>U: Show success message (Check email)
```

### 1.2. Email Verification Sequence Diagram

```mermaid
sequenceDiagram
    participant U as User (Email Client)
    participant F as Frontend (Angular)
    participant B as Backend (Spring Boot)
    participant E as Email Service
    participant DB as Database

    U->>F: Clicks verification link (/verify?token=...)
    F->>B: GET /api/auth/verify?token=...
    
    B->>DB: Find token
    DB-->>B: Token found
    
    alt Token valid
        B->>DB: Update User Status to ACTIVE
        B->>DB: Delete Verification Token
        B-->>F: 200 OK
        F-->>U: Show verification success screen
    else Token expired
        B-->>F: 400 Bad Request (reason: expired)
        F-->>U: Show expired message + "Resend Link" button
        U->>F: Clicks "Resend Link"
        F->>B: POST /api/auth/resend-verification?email=...
        B->>DB: Find User
        B->>DB: Generate & Save new Token
        B->>E: Send new Verification Email
        B-->>F: 200 OK
        F-->>U: Show success message
    else Token invalid
        B-->>F: 400 Bad Request (reason: invalid)
        F-->>U: Show invalid token error
    end
```

### 1.3. User Status Transitions

```mermaid
stateDiagram-v2
    [*] --> PENDING: Registration
    PENDING --> ACTIVE: Email Verified
    PENDING --> [*]: Re-registration (Cleanup)
    ACTIVE --> [*]: Self-Delete
```

---

## 2. Forgot Password Workflow

The forgot password workflow allows users to regain access to their account by resetting their password via a secure email link.

For a step-by-step visual guide with screenshots, see the [Forgot Password UI Guide](../../user-guide/workflows/forgot-password-ui-guide.md).

### 2.1. Forgot Password Sequence Diagram

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend (Angular)
    participant B as Backend (Spring Boot)
    participant E as Email Service
    participant DB as Database

    U->>F: Enters email for password recovery
    F->>B: POST /api/auth/forgot-password (email)
    
    B->>DB: Find user by email
    DB-->>B: User found
    
    B->>DB: Generate & Save Recovery Token (Expiry: 1h)
    B->>E: Send Recovery Email (with link)
    E-->>U: Email delivered
    
    B-->>F: 200 OK (Avoid leaking email existence)
    F-->>U: Show success message (Check email)

    U->>F: Clicks recovery link (/reset-password?token=...)
    F->>U: Show Reset Password form
    U->>F: Enters new password
    F->>B: POST /api/auth/reset-password (token, password)
    
    B->>DB: Find & Validate token
    DB-->>B: Token valid
    
    B->>B: Hash new password
    B->>DB: Update User password
    B->>DB: Delete Recovery Token
    
    B-->>F: 200 OK
    F-->>U: Redirect to Login (Success message)
```

---

## 3. User Self-Delete Workflow

Users have the right to delete their own account. Certain system accounts are protected.

### 3.1. Self-Delete Sequence Diagram

```mermaid
sequenceDiagram
    participant U as User (Logged in)
    participant F as Frontend (Angular)
    participant B as Backend (Spring Boot)
    participant DB as Database

    U->>F: Clicks "Delete Account"
    F-->>U: Show confirmation dialog
    U->>F: Confirms deletion
    
    F->>B: DELETE /api/users/me
    
    alt User is protected (admin, user, etc.)
        B-->>F: 403 Forbidden
        F-->>U: Show "Cannot delete this account"
    else Regular user
        B->>DB: Find User
        B->>DB: Delete User
        B->>B: Log action (USER_DELETED)
        B-->>F: 204 No Content
        F->>F: Clear session / Logout
        F-->>U: Redirect to Landing Page
    end
```
