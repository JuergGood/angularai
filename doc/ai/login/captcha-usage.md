### reCAPTCHA Configuration and Usage

To answer your question: No, you should **not** set it to `enabled`. 

The configuration uses a special value `disabled` to skip verification during development. If you set it to `enabled`, the backend will treat "enabled" as a literal (and invalid) Google API secret key, which will cause all registration attempts to fail.

#### How it works:
*   **`disabled`**: If `google.recaptcha.secret.key` is set to `disabled`, the backend skip the verification check and allows the registration to proceed without a valid token. The frontend also detects this and hides the reCAPTCHA widget.
*   **Real Key**: If you provide a real secret key (e.g., `6Lcz...`), the backend will attempt to verify the token with Google's servers.

#### Registration and Cost:
*   **Registration**: To enable real protection, you must register your site at [Google reCAPTCHA Admin Console](https://www.google.com/recaptcha/admin/). Choose **reCAPTCHA v2 ("I'm not a robot" Checkbox)**.
*   **Cost**: Google reCAPTCHA v2 is **free** for most users. It only starts costing money if you exceed 1 million assessments per month (Standard plan), which is more than enough for most project prototypes or small-to-medium applications.

#### Updated `.env.example`
I have updated the `.env.example` file to include these keys so you can easily configure them in your local `.env` file:

```properties
# Google reCAPTCHA v2 (Checkbox)
# Set to 'disabled' to skip verification during development
RECAPTCHA_SITE_KEY=6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe
RECAPTCHA_SECRET_KEY=disabled
```
*(Note: The `RECAPTCHA_SITE_KEY` above is Google's public test key, which is safe to use for testing UI rendering.)*