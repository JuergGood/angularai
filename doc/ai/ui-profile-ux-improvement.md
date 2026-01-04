# Proposal: Profile and Settings UX Improvement

## 1. Overview
This proposal outlines changes to the header UX to improve access to user profile actions and application settings.

## 2. Header Changes

### 2.1 User Profile Menu
- **Display**: The login name of the authenticated user will be displayed on the right side of the header toolbar.
- **Icon**: A profile icon (`account_circle`) will be placed next to the login name.
- **Menu**: Clicking the profile icon will open a dropdown menu with the following items:
  - **Profile**: Redirects to the profile edit screen (visible only if logged in).
  - **Logout**: Logs out the user (visible only if logged in).
  - **Login**: Redirects to the login screen (visible only if logged out).
- **Sidenav Cleanup**: The Profile, Login, and Logout items will be removed from the sidebar navigation to avoid redundancy.

### 2.2 Settings Menu
- **Icon**: A new settings icon (`settings`) will be added to the right side of the header.
- **Menu Items**:
  - **Version**: Displays the application version (retrieved from `pom.xml`).
  - **Mode**: Displays the current deployment profile (e.g., Postgres, H2, or Default).
  - **Help**: Displays a summary of the application features and a note about it being a test application for AI code generation.

## 3. Technical Implementation

### 3.1 Backend
- **New Properties**: Create `application-h2.properties` to explicitly define the H2 configuration.
- **System Controller**: Implement a REST controller to expose:
  - Application version (using Spring's `@Value("${project.version}")` or similar).
  - Active Spring profiles to determine the "Mode".
- **DTO**: Create `SystemInfoDTO` for the response.

### 3.2 Frontend
- **System Service**: A new service to fetch system information from the backend.
- **Sidenav Component**:
  - Update `sidenav.component.html` to move profile/login/logout logic to the header `mat-toolbar`.
  - Add `mat-menu` for Profile and Settings.
  - Implement a dialog or expansion panel for the "Help" content.
- **Styling**: Ensure the header elements are correctly aligned and responsive.

## 4. Verification Plan
- **Manual Test**: Verify the menus appear correctly for both logged-in and logged-out users.
- **Backend Test**: Verify the `/api/system/info` endpoint returns the correct version and profile.
- **Component Test**: Update `sidenav.component.spec.ts` if necessary.
