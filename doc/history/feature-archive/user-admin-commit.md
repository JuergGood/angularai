Here is a suggested commit message based on the implementation of the 'User Admin' feature:

```text
feat: implement User Admin page and role-based access control

Implemented a comprehensive User Administration system including backend security, REST endpoints, and a frontend management dashboard.

Backend Changes:
- Introduced `Role` enum (`ROLE_USER`, `ROLE_ADMIN`).
- Updated `User` entity and `UserDTO` with `role` field and unique constraints for `login` and `email`.
- Configured `SecurityConfig` to enforce role-based authorization for `/api/admin/**`.
- Created `AdminUserController` for user CRUD operations with self-protection logic (preventing admins from deleting themselves).
- Updated `DataInitializer` with default admin and user accounts.

Frontend Changes:
- Added `role` to `User` model.
- Enhanced `AuthService` with `isAdmin()` signal-based helper.
- Created `AdminService` for administrative API communication.
- Refactored `UserAdminComponent` into a functional dashboard using `mat-table`.
- Integrated edit/delete functionality with confirmation dialogs and role elevation support.
- Updated side navigation to show 'User Admin' only for administrators.

Verification:
- Added `AdminUserControllerTest` (all passed).
- Added `UserAdminComponent` unit tests (all passed).
- Verified all 19 backend and 37 frontend tests pass successfully.
```