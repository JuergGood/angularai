### Initial Project Plan and Implementation Archive

#### 1. Objectives
- Create a Spring Boot backend.
- Create an Angular frontend.
- Implement a User administration system with:
    - Firstname, lastname, login, password, birthdate, address.
    - Login and profile editing pages.

#### 2. Initial Plan
1. Initialize Spring Boot project in `backend` folder.
2. Initialize Angular project in `frontend` folder.
3. Define User entity and repository in Backend.
4. Implement Backend REST API for User administration (CRUD + Login).
5. Implement Frontend services for API interaction.
6. Create Login page in Frontend.
7. Create User Profile Editing page in Frontend.
8. Verify the application functionality.

#### 3. Steps Taken
- Created `backend` and `frontend` directories.
- Attempted to initialize Spring Boot using `curl` from `start.spring.io` (Encountered some issues with PowerShell syntax and parameters).
- Attempted to initialize Angular using `ng new` (Angular CLI not found in environment).

#### 4. Current Status
- Directories created.
- Project structure initialized for both Backend (Spring Boot) and Frontend (Angular).
- Core User administration system implemented:
    - `User` entity and `UserDTO` defined with requested attributes.
    - Backend REST API and Security configured.
    - Frontend `ProfileComponent` and `LoginComponent` implemented.
    - Frontend `UserService` and `AuthService` implemented.
