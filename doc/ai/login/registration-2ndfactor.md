### Proposal: 2nd-Factor Registration Confirmation

This document proposes several options for implementing a 2nd-factor confirmation (email or phone) during the registration process to ensure user authenticity and prevent spam.

#### 1. Current State
- Users register via `/api/auth/register`.
- Registration is protected by Google reCAPTCHA Enterprise/Legacy.
- Upon successful registration, the user is immediately created in the database and redirected to login.
- No verification of the provided email or phone number is performed.

#### 2. Options for 2nd-Factor Confirmation

##### Option A: Google reCAPTCHA Enterprise (Multi-factor Authentication)
Google reCAPTCHA Enterprise provides a "Multi-factor Authentication" (MFA) feature that can send SMS or email verification codes.

- **How it works:**
    1. During registration, the frontend triggers a reCAPTCHA assessment.
    2. If the risk score is high or by policy, you can trigger a "verification" challenge.
    3. Google handles the sending of the SMS/Email and the verification of the code.
- **Pros:**
    - Integrated with existing reCAPTCHA setup.
    - Google handles the delivery infrastructure.
- **Cons:**
    - **Cost:** This is a paid feature of reCAPTCHA Enterprise (requires a linked Google Cloud Billing account).
    - **Complexity:** Requires more advanced integration with Google Cloud Identity Platform or Firebase.

##### Option B: Firebase Authentication (Phone/Email Verification)
Firebase (a Google service) provides robust, easy-to-use verification services.

- **How it works:**
    - **Phone:** Frontend uses Firebase SDK to send an SMS. User enters the code, and Firebase returns a verification token.
    - **Email:** Backend (or Frontend) triggers a verification email via Firebase.
- **Pros:**
    - Very high delivery reliability.
    - Free tier available for SMS (limited) and Email.
    - Excellent Angular support (`@angular/fire`).
- **Cons:**
    - Adds another external dependency (Firebase).
    - Requires syncing Firebase users with the local database.

##### Option C: Manual Implementation (SMTP / Twilio)
Build a custom verification flow within the existing Spring Boot backend.

- **How it works:**
    1. **Registration:** Create the user with a `status = 'PENDING'` and generate a random `verification_token`.
    2. **Notification:** 
        - **Email:** Backend sends an email with a link (e.g., `/api/auth/verify?token=...`) using Spring Boot Starter Mail (SMTP).
        - **Phone:** Backend sends an SMS with a code using a service like Twilio.
    3. **Confirmation:** User clicks the link or enters the code, backend sets `status = 'ACTIVE'`.
- **Pros:**
    - Full control over the user experience and data.
    - No dependence on Google/Firebase for user state.
- **Cons:**
    - **Infrastructure:** Requires setting up an SMTP server (e.g., AWS SES, SendGrid) or a Twilio account.
    - **Maintenance:** You are responsible for handling delivery failures, expiration of tokens, and resending logic.

#### 3. Comparison Table

| Feature | reCAPTCHA MFA | Firebase | Manual (SES/Twilio) |
| :--- | :--- | :--- | :--- |
| **Ease of Integration** | Medium | High | Medium |
| **Cost** | High (Enterprise) | Low/Free Tier | Low (Pay-per-use) |
| **User Experience** | Seamless | Good | Customizable |
| **Delivery Reliability** | Excellent | Excellent | Depends on Provider |

#### 4. Recommendation (Adopted)

For a project already using Angular and Spring Boot, **Option C (Manual Implementation with AWS SES for Email)** has been implemented.

**Implementation Details:**
1. **User Entity:** Added `phone` and `status` fields. Statuses: `PENDING`, `ACTIVE`, `DISABLED`.
2. **Mandatory Fields:** Both `email` and `phone` are now mandatory for registration.
3. **Verification Token:** A `VerificationToken` entity stores random UUID tokens with a 24-hour expiration.
4. **Flow:** 
   - User registers -> Status is `PENDING`.
   - Backend generates a token and sends a verification email via **Amazon SES**.
   - User clicks the link -> Status becomes `ACTIVE`.
   - Login is only allowed for `ACTIVE` users.
5. **Amazon SES & SMS:**
   - **Email:** SES is used for sending the verification link.
   - **SMS:** While SES itself is primarily for Email, AWS supports SMS through **Amazon SNS (Simple Notification Service)** or **Amazon Pinpoint**. If SMS verification is needed in the future, SNS can be integrated using the AWS SDK for Java. Currently, the system provides a hint that verification is required and sends an email.

#### 5. Next Steps
- Verify Amazon SES sandbox/production status in AWS Console.
- Ensure `SES_USERNAME`, `SES_PASSWORD`, `SES_FROM`, and `APP_BASE_URL` environment variables are set.
