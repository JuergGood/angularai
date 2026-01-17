# Export SonarCloud Issues for Junie (JSON via Web API)

This guide explains how to export SonarCloud issues into a **machine-readable JSON file**
that can be consumed by **Junie** for automated fixes.

The approach uses the official **SonarCloud Web API** and works reliably for Angular,
TypeScript, Java, and other supported languages.

---

## Prerequisites

- A SonarCloud account
- Access to the project: `JuergGood_angularai`
- `curl` installed
- A SonarCloud **user token**

---

## Step 1: Create a SonarCloud Token

1. Open SonarCloud
2. Go to **My Account → Security**
3. Generate a new token
4. Copy the token (it will be used as the password in API calls)

> ⚠️ Store the token securely. Do not commit it to git.

---

## Step 2: Call the SonarCloud Issues API

SonarCloud does not provide a UI download button, but all issues are accessible via the API.

### API Endpoint

