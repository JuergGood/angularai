### Core Foundation: User Admin System

*   **Backend (Spring Boot 3.4.1)**
    *   `User` entity/DTO with core attributes (FirstName, LastName, Login, etc.)
    *   Secure REST endpoints (`/api/users/me`) for profile management
    *   Authentication & session handling via `AuthController`
    *   Unit tests using `MockMvc` & `MockitoBean`

*   **Frontend (Angular 19)**
    *   Standalone `Login` & `Profile` components (Material Design)
    *   `AuthService` & `UserService` with Signal-based state
    *   Modern control flow (`@if`, `@for`) & `ngx-translate` (i18n)

*   **Result**: Secure, tested, and fully functional profile management.
