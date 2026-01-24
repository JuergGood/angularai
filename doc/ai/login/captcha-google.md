### reCAPTCHA Enterprise Integration Completed

I have integrated the reCAPTCHA Enterprise keys you provided. The system now supports both the standard v2 API and the new Enterprise Assessments API.

#### 1. Backend Integration (`CaptchaService.java`)
The backend now detects if Enterprise configuration is present (Project ID and API Key). If so, it uses the Google Enterprise Assessments API (`/assessments`) to verify tokens. If these are missing, it falls back to the legacy v2 API.

#### 2. Frontend Integration (`RegisterComponent.ts`)
The frontend has been updated to load `enterprise.js` and use the `grecaptcha.enterprise` namespace for rendering the checkbox widget.

#### 3. Configuration (`.env.example`)
I have updated the environment template to include the necessary fields for Enterprise:
```properties
# Google reCAPTCHA Enterprise
RECAPTCHA_SITE_KEY=your_site_key
RECAPTCHA_SECRET_KEY=your_secret_key (only for legacy fallback)
RECAPTCHA_PROJECT_ID=goodstar-1769235355840
RECAPTCHA_API_KEY=your_google_cloud_api_key
```

### Addressing Your Questions

#### Is separating local and production keys good practice?
**Yes, absolutely.** Separating keys for `localhost` and your production domain (`goodone.ch`) is highly recommended for several reasons:
*   **Domain Security**: You can restrict the production key to *only* work on `goodone.ch`, preventing others from using your key on their sites.
*   **Analytics Integrity**: Local testing won't pollute your production risk metrics and dashboard data.
*   **Security Policies**: You can have stricter security thresholds for production while keeping local development more permissive.

#### Do you recommend another approach?
Your current approach of using **reCAPTCHA Enterprise** with the **Checkbox (v2-compatible)** widget is a solid balance between security and user experience. 

For the future, if you want a completely seamless UX (no checkbox at all), you could switch to **Score-based reCAPTCHA**, which runs in the background and returns a risk score (0.0 to 1.0). However, the checkbox version you chose is often preferred as it provides a clear "real person" signal to users.

**Next Steps:**
1. Copy the keys from `doc/ai/login/captcha-google.txt` to your local `.env` file.
2. Note that you still need a **Google Cloud API Key** (with reCAPTCHA Enterprise API enabled) to fill the `RECAPTCHA_API_KEY` field for the backend to successfully verify Enterprise tokens.