# Proposal: Android Frontend Implementation (Jetpack Compose)

This document outlines the plan for developing an alternative Android frontend for the AngularAI application using modern Android technologies.

## 1. Objective
Create a native Android application that mirrors the features and UX of the existing Angular web application, communicating with the same Spring Boot REST API.

## 2. Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Modern declarative UI)
- **Design System**: Material UI (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles
- **Asynchronous Work**: Kotlin Coroutines and Flow
- **Networking**: Retrofit 2 with OkHttp
- **Dependency Injection**: Hilt (Dagger-based)
- **Local Caching**: Room Database (SQLite abstraction)
- **Sensitive Data Storage**: EncryptedSharedPreferences or DataStore (for Auth tokens)
- **JSON Parsing**: Kotlinx Serialization or Jackson (to match backend)

## 3. Features & UX Mapping
The Android app will implement all features currently available in the Angular frontend:

### 3.1 Authentication
- Login screen with Basic Auth.
- Registration screen.
- Persistent session using encrypted local storage.
- Logout functionality.

### 3.2 Task Management
- List view of user tasks.
- Create, Edit, and Delete tasks.
- Priority indicators (Material 3 chips or colors).
- Pull-to-refresh to sync with backend.

### 3.3 Profile & Settings
- View and edit user profile (First Name, Last Name, Email, Birth Date, Address).
- Settings screen displaying System Info (Version and Mode).
- Help dialog/screen explaining application features.

### 3.4 Admin Features
- User Management list (only for ROLE_ADMIN).
- Create, Update, and Delete users.
- Role management.

## 4. Local Caching Strategy
To ensure a smooth UX and offline capabilities:
- **Room Database**: Store tasks and user profile data locally.
- **Repository Pattern**: Implement a "Single Source of Truth" strategy.
    - Fetch from API and save to Room.
    - UI observes Room data via Flow.
- **Offline Mode**: Allow viewing cached tasks when no internet is available.
- **Background Sync**: (Optional) Use WorkManager for periodic synchronization.

## 5. Backend API Modifications (If Required)
- **CORS Configuration**: Update `SecurityConfig.java` to allow requests from mobile clients (if testing via physical devices or emulators on different IPs).
- **Authentication**: Ensure Basic Auth is consistently supported for mobile clients (already implemented).
- **API Enhancements**: 
    - Add timestamps to entities (if not present) to help with conflict resolution during sync.
    - Potential pagination for tasks/users if lists grow large.

## 6. Project Structure (Android)
```text
app/
├── data/
│   ├── local/ (Room DB, DAOs, Entities)
│   ├── remote/ (Retrofit API interfaces, DTOs)
│   └── repository/ (Implementation of data sync)
├── di/ (Hilt modules)
├── domain/ (UseCases, Domain Models)
├── ui/
│   ├── auth/ (Login/Register screens)
│   ├── tasks/ (Task list/detail screens)
│   ├── profile/ (Profile screen)
│   ├── admin/ (Admin screens)
│   ├── theme/ (Material 3 configuration)
│   └── components/ (Reusable Compose UI elements)
└── util/ (Helpers)
```

## 7. Implementation Phases
1. **Phase 1: Project Setup**: Initialize Android project, setup Hilt, Retrofit, and Room.
2. **Phase 2: Auth Module**: Implement Login, Registration, and Session management.
3. **Phase 3: Task Module**: Implement Task list (with caching) and CRUD operations.
4. **Phase 4: Profile & Admin**: Implement profile editing and administrative features.
5. **Phase 5: UX Polish**: Apply Material 3 styling, animations, and ensure parity with Angular's look and feel.
6. **Phase 6: Testing**: Unit tests for ViewModels/UseCases and UI tests for core flows.
