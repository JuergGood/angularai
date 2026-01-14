# Dashboard Implementation Plan

This document outlines the plan for adding a Dashboard menu to both the Angular and Android frontends, based on the `tmp/Dashboard.png` design.

## 1. Backend Implementation (Spring Boot)

### 1.1. Data Models & DTOs
Create a `DashboardDTO` to encapsulate all data required by the dashboard in a single request.

```java
public class DashboardDTO {
    private SummaryStats summary;
    private List<TaskDTO> priorityTasks;
    private List<ActionLogDTO> recentActivity;
    private List<UserDTO> recentUsers;
    private TaskStatusDistribution taskDistribution;

    // Inner classes for structured data
    public static class SummaryStats {
        private long openTasks;
        private long openTasksDelta;
        private long activeUsers;
        private long activeUsersDelta;
        private long completedTasks;
        private long completedTasksDelta;
        private long todayLogs;
        private long todayLogsDelta;
    }

    public static class TaskStatusDistribution {
        private long open;
        private long inProgress;
        private long completed;
        private long total;
    }
}
```

### 1.2. Service Layer
Create `DashboardService` to aggregate data:
- `getDashboardData()`:
    - Count tasks by status (Open, In Progress, Completed).
    - Calculate deltas for today (tasks created today, users registered today).
    - Fetch the latest 5 `ActionLog` entries.
    - Fetch the latest 5 high-priority `Task` entries.
    - Fetch the latest 5 `User` entries.

### 1.3. Controller Layer
Create `DashboardController` with a GET endpoint `/api/dashboard`.
- Enforce appropriate security (e.g., `ROLE_USER` minimum, but some data like User Admin might be restricted or masked based on role).

## 2. Frontend Implementation (Angular)

### 2.1. Service
- Update `ApiService` or create `DashboardService` to fetch data from `/api/dashboard`.

### 2.2. Components
- Create `DashboardComponent` (standalone).
- Use Angular Material components:
    - `mat-card` for summary stats.
    - `ngx-charts` or similar for the Task Overview donut chart.
    - `mat-table` for Recent Activity and User Admin.
    - `mat-list` for Priority Tasks.
- Layout: Use CSS Grid or Flexbox to match the 2x3 grid layout in the screenshot.

### 2.3. Routing & Navigation
- Add `/dashboard` route to `app.routes.ts`.
- Add "Dashboard" link with icon `dashboard` to the side navigation menu.

## 3. Mobile Implementation (Android/Compose)

### 3.1. Data Layer
- Add `DashboardDTO` to the domain model.
- Add `getDashboard()` to `ApiService`.

### 3.2. UI Layer
- Create `DashboardScreen` using Jetpack Compose.
- Implement reusable cards for:
    - Statistics summary (Horizontal scroll or Grid).
    - Task Distribution chart (Simple Canvas-based donut).
    - Recent Activity list.
    - Priority Tasks list.
- Add `DashboardViewModel` to handle data fetching and state.

### 3.3. Navigation
- Add `Dashboard` to the bottom navigation or side drawer.
- Set `Dashboard` as the start destination for logged-in users.

## 4. Testing Strategy
- **Backend**: JUnit tests for `DashboardService` logic and `DashboardController` endpoint.
- **Frontend**: Vitest tests for `DashboardComponent` rendering and data binding.
- **Mobile**: UI tests for `DashboardScreen`.
