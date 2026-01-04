I have addressed all the identified defects and implemented the requested change requests for the Android application.

### Improvements & Fixes

#### 1. Task & User Editing (Defect 1)
*   **Fixed**: When selecting an existing task or user, the UI now correctly displays "Edit Task" (or "Edit User") and populates the fields with existing data.
*   **Implementation**: Updated `TaskEditScreen` and `AdminUserEditScreen` to fetch the entity by its ID using the respective ViewModels if an ID is provided in the navigation route.

#### 2. Authentication & Admin Features (Defects 2 & 3)
*   **Fixed**: The **Admin** menu (now labeled **User Admin**) is now correctly displayed in the navigation drawer only for users with the `ROLE_ADMIN` role.
*   **Logout**: The logout feature in the navigation drawer now correctly calls the backend logout endpoint and clears the local session.
*   **Profile**: Added a "Profile" item to the navigation drawer for easy access to user details.

#### 3. Branding & Header Styling (CR 1 & 3)
*   **Title**: Changed the application title from "AngularAI" to **"GoodOne"**.
*   **Color Scheme**: Updated the `TopAppBar` (header) and the Navigation Drawer header to use a vibrant **Indigo Blue** (`#1A237E`) background with white text.
*   **Iconography**: Added the **"1" logo** (`filter_1` icon in pink) next to the "GoodOne" title in the navigation drawer header, matching the Angular UI's branding.

#### 4. Settings Menu (CR 2)
*   **New Feature**: Added a settings icon (⚙️) in the top-right corner of the header.
*   **Menu Items**:
    *   **Version**: Displays the current backend version (fetched from the API).
    *   **Mode**: Displays the active deployment mode (H2/Postgres/Default).
    *   **Help**: Opens an interactive dialog summarizing the application's features and its purpose as an AI code generation testbed.

### Technical Changes
*   **AuthRepository & ViewModel**: Improved state management to expose the `currentUser` (and their role) as a reactive stream, ensuring the UI updates immediately upon login/logout.
*   **System API Integration**: Created `SystemApi`, `SystemRepository`, and `SystemInfoDTO` on the Android side to communicate with the backend's system info endpoint.
*   **Navigation**: Refined navigation routes to support passing and retrieving numeric IDs for editing operations.

Please rebuild the project in Android Studio to see these changes in action!