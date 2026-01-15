### Specification: User Admin Page

This proposal outlines the implementation of the 'User Admin' page, including necessary backend and frontend changes to support role-based access control and user management.

#### 1. Backend Changes

**1.1 Data Model (`User` Entity)**
- Add a `role` field to the `User` entity.
- Introduce a `Role` enum with values: `ROLE_USER`, `ROLE_ADMIN`.
- Update the `email` column to have a `unique = true` constraint.

**1.2 Security Configuration (`SecurityConfig`)**
- Update `UserDetailsService` to load the role from the `User` entity instead of hardcoding `ROLE_USER`.
- Update `SecurityFilterChain` to restrict access to `/api/admin/**` endpoints to users with `ROLE_ADMIN`.
- Ensure `/api/auth/register` assigns `ROLE_USER` by default.

**1.3 Admin Controller (`AdminUserController`)**
- Create a new REST controller at `/api/admin/users`.
- **Endpoints**:
    - `GET /api/admin/users`: Retrieve a list of all users.
    - `PUT /api/admin/users/{id}`: Update any user's details, including their role (elevation to Admin).
    - `DELETE /api/admin/users/{id}`: Delete a user by ID.

#### 2. Frontend Changes

**2.1 User Model (`user.model.ts`)**
- Add the `role` field to the `User` interface.

**2.2 Authentication Service (`auth.service.ts`)**
- Update `AuthService` to include a helper method `isAdmin()` based on the `currentUser` signal.

**2.3 User Admin Component (`user-admin.component.ts`)**
- Implement a table view using `mat-table` to display all users.
- **Features**:
    - **Edit**: Open a dialog or inline form to modify user details (First Name, Last Name, Email, Role).
    - **Delete**: Display a confirmation dialog before deleting a user.
    - **Role Elevation**: A toggle or select menu within the edit form to change a user's role between 'User' and 'Admin'.
- **Access Control**: Use an `@if (authService.isAdmin())` guard in the template and a functional router guard to prevent unauthorized access to the `/user-admin` route.

#### 3. Data Integrity & Validation
- **Unique Email**: The backend will return a `400 Bad Request` if an administrator attempts to update a user's email to one that already exists in the system.
- **Self-Protection**: Prevent an admin from deleting their own account or removing their own admin privileges to ensure at least one administrator remains.

#### 4. Sample Data (`DataInitializer`)
- Update the default 'admin' user to have `ROLE_ADMIN`.
- Create several dummy users with `ROLE_USER` for testing purposes.