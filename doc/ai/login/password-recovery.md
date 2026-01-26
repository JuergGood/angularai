# Password Recovery Workflow Proposal

This document outlines the proposed workflow for the "Forgot Password" feature in the AngularAI project.

## 1. Overview
The password recovery process allows users who have forgotten their password to securely reset it using a link sent to their registered email address.

## 2. Workflow Steps

### Step 1: Request Password Reset
1.  **UI Action**: User clicks the "Forgot Password?" link on the login page.
2.  **Navigation**: User is redirected to the `/forgot-password` page.
3.  **User Input**: User enters their email address and submits the form.
4.  **Security**: A reCAPTCHA or similar mechanism should be used to prevent automated abuse.
5.  **Feedback**: A confirmation message is shown: *"If an account exists for this email, you will receive a password recovery link shortly."*
    *   **Note**: The system MUST NOT reveal whether the email exists in the database to prevent user enumeration.

### Step 2: Backend Processing
1.  **Verification**: Backend checks if the email exists.
2.  **Token Generation**: If the user exists, generate a cryptographically secure, time-limited recovery token (e.g., valid for 1 hour).
3.  **Email Dispatch**: Send an HTML-formatted email to the user.
    *   **Subject**: Password Recovery - AngularAI
    *   **Content**: Contains a link to `https://<domain>/reset-password?token=<token>`.

### Step 3: Password Reset
1.  **UI Action**: User clicks the link in the email.
2.  **Navigation**: User is redirected to the `/reset-password` page with the token as a query parameter.
3.  **User Input**: User enters a new password and confirms it.
4.  **Backend Update**: 
    *   Validate the token.
    *   Update the user's password (hashed using BCrypt).
    *   Invalidate the token immediately after use.
5.  **Feedback**: Show success message and provide a link back to the login page.

## 3. Implementation Details

### API Endpoints
- `POST /api/auth/forgot-password`: Receives email, initiates the process.
- `POST /api/auth/reset-password`: Receives token and new password, completes the process.

### Security Considerations
- **Token Expiry**: Tokens must expire after a short period (e.g., 1 hour).
- **One-time Use**: Tokens must be invalidated after a successful password reset.
- **Rate Limiting**: Implement rate limiting on the `/forgot-password` endpoint to prevent spamming.
- **Generic Responses**: Always return a success response on the forgot password request, regardless of whether the email was found.

## 4. UI/UX
- Add "Forgot your password?" link below the password field or next to the "Login" button.
- Use Angular Material components (`mat-card`, `mat-form-field`, `mat-button`) for the new pages.
- Ensure translations are provided for both English and German.
