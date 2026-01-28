### Initial Implementation Summary
The first iteration successfully established the core foundation for the user administration system:
- **Backend (Spring Boot 3.4.1)**:
  - `User` entity and `UserDTO` implemented with `firstName`, `lastName`, `login`, `password`, `email`, `birthDate`, and `address`.
  - `UserController` provides secure REST endpoints (`/api/users/me`) for profile retrieval and updates.
  - `AuthController` handles authentication and session management.
  - Comprehensive unit testing with `MockMvc` and `MockitoBean`, covering controllers and repositories.
- **Frontend (Angular 19)**:
  - `LoginComponent` and `ProfileComponent` implemented as standalone components with Angular Material.
  - `AuthService` and `UserService` manage API interactions and reactive state using Signals.
  - Modern control flow (`@if`, `@for`) and `ngx-translate` for internationalization are integrated.
- **Verification**: All backend tests are green, and the UI correctly handles profile modifications and persistence.