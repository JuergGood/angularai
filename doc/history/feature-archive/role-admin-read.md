# Plan for ROLE_ADMIN_READ Implementation

This document outlines the changes required to add a new role `ROLE_ADMIN_READ` to the AngularAI project.
`ROLE_ADMIN_READ` will have the same view rights as `ROLE_ADMIN` but no update rights.

## 1. Backend Changes (Spring Boot)

### 1.1. Model Update
- **File**: `backend/src/main/java/ch/goodone/angularai/backend/model/Role.java`
- **Change**: Add `ROLE_ADMIN_READ` to the `Role` enum.

### 1.2. Security Configuration
- **File**: `backend/src/main/java/ch/goodone/angularai/backend/config/SecurityConfig.java`
- **Change**: Update the `SecurityFilterChain` to allow both `ADMIN` and `ADMIN_READ` to access administrative endpoints for read operations, but restrict write operations to `ADMIN` only.
```java
// Current
.requestMatchers("/api/admin/**").hasRole("ADMIN")

// New
.requestMatchers(HttpMethod.GET, "/api/admin/**").hasAnyRole("ADMIN", "ADMIN_READ")
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

### 1.3. Controller Refinement (Optional but recommended)
- **Files**: `AdminUserController.java`, `ActionLogController.java`
- **Change**: Although `SecurityConfig` handles the main protection, adding `@PreAuthorize` or explicit checks within methods provides defense in depth.

## 2. Frontend Changes (Angular)

### 2.1. Authentication Service
- **File**: `frontend/src/app/services/auth.service.ts`
- **Change**: Update `isAdmin()` to include `ROLE_ADMIN_READ` for general admin UI visibility, and add a new `canEditAdmin()` or similar method.
```typescript
isAdmin(): boolean {
  const role = this.currentUser()?.role;
  return role === 'ROLE_ADMIN' || role === 'ROLE_ADMIN_READ';
}

hasAdminWriteAccess(): boolean {
  return this.currentUser()?.role === 'ROLE_ADMIN';
}
```

### 2.2. User Administration UI
- **File**: `frontend/src/app/components/user-admin/user-admin.component.ts` & `user-admin.component.html`
- **Change**: 
    - Disable or hide "Add User", "Edit", and "Delete" buttons for `ROLE_ADMIN_READ`.
    - Show user details in read-only mode if the edit button is clicked, or simply disable the edit button.
    - Add `ROLE_ADMIN_READ` to the role selection dropdown (only visible/editable for full `ROLE_ADMIN`).

### 2.3. Action Logs UI
- **File**: `frontend/src/app/components/log/log.component.ts` & `log.component.html`
- **Change**: Hide the "Clear All Logs" button for `ROLE_ADMIN_READ`.

## 3. Android App Changes

### 3.1. Main Navigation
- **File**: `android/app/src/main/java/ch/goodone/angularai/android/MainActivity.kt`
- **Change**: Update `isAdmin` logic to include `ROLE_ADMIN_READ` so the "User Admin" and "Logs" menu items are visible.
```kotlin
val isAdmin = currentUser?.role == "ROLE_ADMIN" || currentUser?.role == "ROLE_ADMIN_READ"
val canEditAdmin = currentUser?.role == "ROLE_ADMIN"
```

### 3.2. User List Screen
- **File**: `android/app/src/main/java/ch/goodone/angularai/android/ui/admin/AdminUserListScreen.kt`
- **Change**: 
    - Hide the Floating Action Button (FAB) for adding users if the user is `ROLE_ADMIN_READ`.
    - Hide the Delete icon in `UserItem` if the user is `ROLE_ADMIN_READ`.

### 3.3. User Edit Screen
- **File**: `android/app/src/main/java/ch/goodone/angularai/android/ui/admin/AdminUserEditScreen.kt`
- **Change**: 
    - Add `ROLE_ADMIN_READ` to the role selection list.
    - If the user has `ROLE_ADMIN_READ`, disable all input fields and hide the "Save" button.

## 4. Documentation & Verification

- **API Documentation**: Update Swagger annotations if necessary.
- **Tests**:
    - Add backend tests for `ROLE_ADMIN_READ` to verify they can GET `/api/admin/users` but get 403 on POST/PUT/DELETE.
    - Add frontend/android unit tests to verify UI elements are correctly hidden/disabled.
