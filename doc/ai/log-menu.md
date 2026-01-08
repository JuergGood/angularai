### Proposal: Log Menu Item Implementation

#### 1. Overview
The goal is to add a new "Log" menu item to the Angular frontend, accessible only to administrators. This page will display a table of `ActionLog` entries with filtering, sorting, and paging capabilities.

#### 2. Backend Changes (Spring Boot)

##### 2.1. `ActionLogDTO`
Create a new DTO for sending log entries to the frontend.
- Fields: `id`, `timestamp` (formatted), `login`, `action`, `details`.
- Static `fromEntity(ActionLog log)` method.

##### 2.2. `ActionLogRepository` enhancements
Extend `ActionLogRepository` to support filtering and paging.
- Add methods or use `JpaSpecificationExecutor` for flexible filtering.
- Filter criteria:
    - Action type (all, login, task, user admin).
        - **Login**: includes `USER_LOGIN`, `USER_LOGOUT`, and `USER_REGISTERED`.
        - **Task**: includes `TASK_%`.
        - **User Admin**: includes `USER_CREATED`, `USER_MODIFIED`, and `USER_DELETED`.
    - Date range (from/to `LocalDateTime`).

##### 2.3. `ActionLogController`
Create `ch.goodone.angularai.backend.controller.ActionLogController` with the following endpoints:
- `GET /api/admin/logs`: Returns a paged and filtered list of `ActionLogDTO`.
    - Parameters: `page`, `size`, `sort`, `type`, `startDate`, `endDate`.
- `DELETE /api/admin/logs`: Deletes all log entries (requires admin confirmation from UI).
- Annotate with `@RestController`, `@RequestMapping("/api/admin/logs")`.
- Secure endpoints to `ROLE_ADMIN` in `SecurityConfig`.

##### 2.4. `ActionLogService` enhancements
- `getLogs(Pageable pageable, String type, LocalDateTime start, LocalDateTime end)`: Logic for fetching filtered logs.
- `clearLogs()`: Deletes all entries from the repository.

#### 3. Frontend Changes (Angular)

##### 3.1. `ActionLog` Model
Create `frontend/src/app/models/action-log.model.ts`.

##### 3.2. `LogService`
Create `frontend/src/app/services/log.service.ts`.
- `getLogs(params)`: Fetch logs from `/api/admin/logs`.
- `clearLogs()`: Call `DELETE /api/admin/logs`.

##### 3.3. `LogComponent`
Create a new standalone component `frontend/src/app/components/log/log.component`.
- **Template**:
    - Filter section:
        - `mat-select` for Action Type (All, Login, Task, User Admin).
        - `mat-date-range-input` with `mat-datepicker` for day-range.
        - "Clear Filter" button.
    - Action section:
        - "Clear Log" button (triggers `mat-dialog` confirmation).
    - Table:
        - `mat-table` with columns: Timestamp, Login, Action, Details.
        - `matSort` for sorting by timestamp.
    - Paging:
        - `mat-paginator` for handling large amounts of logs.
- **Security Enhancements**:
    - Backend: Configure `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` to avoid browser basic auth dialogs.
    - Frontend: Added `isInitializing` state to `AuthService` to prevent premature menu display during session restoration.
- **Logic**:
    - Use `Signals` for state management.
    - Implement filtering and paging logic calling `LogService`.

##### 3.4. Routing
Update `frontend/src/app/app.routes.ts`:
- Add `{ path: 'logs', component: LogComponent, canActivate: [authGuard, adminGuard] }`.

##### 3.5. Navigation
Update `frontend/src/app/components/layout/sidenav.component.html`:
- Add "Log" item within the `authService.isAdmin()` block.
- Use `history` or `receipt` icon.

#### 4. UI/UX Details
- **Theme**: Follow existing `indigo-pink` theme using Angular Material.
- **Responsiveness**: Ensure the table is readable on mobile or hide less critical columns.
- **Dialog**: Use a Material dialog for the "Clear Log" confirmation to prevent accidental data loss.

#### 5. Security
- Backend: Endpoints are prefixed with `/api/admin/` and secured via Spring Security configuration.
- Frontend: Route protected by `adminGuard`, and menu item only visible if `authService.isAdmin()` is true.
