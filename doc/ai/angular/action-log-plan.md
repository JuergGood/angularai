### Proposal: Action Logging to Database

This proposal outlines the implementation of a logging system to record important user and administrative actions into the database.

#### 1. Database Schema

A new table `action_log` will be created to store the following information:

- `id` (Primary Key, Auto-increment)
- `timestamp` (LocalDateTime, when the action occurred)
- `login` (String, the login name of the user who performed the action or is related to the action)
- `action` (String, description of the action, e.g., "USER_LOGIN", "TASK_CREATED")
- `details` (String, optional additional information about the action)

#### 2. Logged Actions

The following actions will be logged:

##### 2.1 User Authentication
- **User Login**: Logged when a user successfully logs in.
- **User Logout**: Logged when a user logs out.

##### 2.2 User Management (Admin & Self)
- **User Registered**: Logged when a new user registers.
- **User Created**: Logged when an admin creates a new user.
- **User Deleted**: Logged when an admin deletes a user.
- **User Modified**: Logged when a user's profile is updated (email, password, etc.).
- **Password Changed**: Specifically log when a password is updated.

##### 2.3 Task Management
- **Task Added**: Logged when a user creates a new task.
- **Task Updated**: Logged when a user modifies an existing task.
- **Task Removed**: Logged when a user deletes a task.

#### 3. Implementation Plan

- **Backend**:
  - Create `ActionLog` entity and `ActionLogRepository`.
  - Create `ActionLogService` to provide a centralized way to record logs.
  - Integrate `ActionLogService` into `AuthController`, `UserController`, `AdminUserController`, and `TaskController`.
  - Add a logout endpoint to `AuthController` to allow logging the logout action before the session is cleared.

- **Frontend**:
  - Update `AuthService.logout()` to call the new backend logout endpoint.
