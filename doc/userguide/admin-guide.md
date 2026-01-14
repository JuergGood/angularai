# Admin Guide

This guide is intended for system administrators of the AngularAI application. Administrators have access to additional features for user management and system monitoring.

## Table of Contents
1. [User Administration](#user-administration)
2. [System Logs](#system-logs)
3. [Roles and Permissions](#roles-and-permissions)

## User Administration
The User Administration page allows you to manage all users in the system:
- **List Users**: View a list of all registered users, including their login name, full name, email, and role.
- **Add User**: Create a new user account manually by providing their personal details and assigning a role.
- **Edit User**: Modify the details of an existing user. Note that you cannot change the login name once a user is created.
- **Delete User**: Remove a user from the system. You cannot delete your own account.
- **View User (Read-only)**: Users with the `ROLE_ADMIN_READ` role can view user details but cannot make changes.

## System Logs
The System Logs page provides an audit trail of actions performed within the application:
- **Audit Trail**: View logs including timestamp, user login, action performed, and additional details.
- **Filtering**: Filter logs by action type (Login, Tasks, User Admin) and date range.
- **Sorting**: Sort logs by timestamp.
- **Paging**: Navigate through large sets of logs using the paginator.
- **Clear Logs**: Administrators with full write access can clear all logs using the "Clear All Logs" button.

## Roles and Permissions
The application uses the following roles to control access:
- **ROLE_USER**: Standard user access. Can manage their own tasks and view their profile.
- **ROLE_ADMIN**: Full administrative access. Can manage users, view all logs, and perform system-wide actions.
- **ROLE_ADMIN_READ**: Read-only administrative access. Can view user lists and logs but cannot perform modifications or deletions.
