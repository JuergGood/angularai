# Task Management Implementation Plan - COMPLETED

This document outlines the execution strategy for the proposed Task Management improvements, as specified in `task_management_proposed_improvements.md`.

## 1. Feasibility and Effort Assessment

| Feature | Effort | Feasibility | Status |
| :--- | :--- | :--- | :--- |
| **Quick Add Task** | Medium | High | ✓ Done |
| **Inline Editing** | Medium-High | High | ✓ Done |
| **Extended Statuses** | Low | High | ✓ Done |
| **Visual Status Encoding** | Low | High | ✓ Done |
| **Relative Due Dates** | Low | High | ✓ Done |
| **Smart Filter Chips** | Medium | High | ✓ Done |
| **Persist Preferences** | Low-Medium | High | ✓ Done (FE state/Signals) |
| **Completion Animation** | Low | High | ✓ Done (CSS transitions) |
| **Completed Section** | Medium | High | ✓ Done |
| **View Density Toggle** | Low | High | ✓ Done |
| **Bulk Actions** | Medium-High | High | ✓ Done |
| **Tags** | Medium | High | ✓ Done (BE + DTO support) |

---

## 2. Phase 1: MVP (Focus on Core UX & Speed)

Goal: Transform the current CRUD interface into a fast, daily-use ToDo list.

### 2.1 Backend Changes
1.  **Enums Update**: 
    - Update `TaskStatus` to include `IN_PROGRESS` and `ARCHIVED`. Rename `COMPLETED` to `DONE` for consistency with spec (or keep `COMPLETED` and map it).
2.  **Entity Update**:
    - Add `completedAt` (Instant) to `Task` entity.
    - Ensure `updatedAt` is tracked.
3.  **Controller Enhancements**:
    - Implement `PATCH /api/tasks/{id}` for partial updates (important for inline editing).
    - Update `POST /api/tasks` to handle minimal "Quick Add" payload (only title).
    - Add filtering parameters to `GET /api/tasks` (status, smartFilter).
4.  **Repository**:
    - Add query methods for smart filters (Today, Overdue, etc.).

### 2.2 Frontend Changes
1.  **Models**: Update `task.model.ts` with new statuses and fields.
2.  **TaskService**:
    - Add `patchTask()` method.
    - Add `quickAdd()` method.
    - Implement state management using `Signals` or `BehaviorSubject` for immediate UI updates.
3.  **Components**:
    - **QuickAddTaskComponent**: New standalone component for the top input field.
    - **TaskListComponent**: Update to integrate filter chips.
    - **TaskRowComponent**: Implement inline editing (click-to-edit title, status cycling).
4.  **Utilities**:
    - Create `date-utils.ts` for relative date formatting and overdue logic.

### 2.3 Data Migration
- SQL script to add `completed_at` column.
- Update existing `COMPLETED` tasks to `DONE` status (if renamed).

---

## 3. Phase 2: Enhancements (Organization & Feedback)

Goal: Add power-user features and polished feedback loops.

### 3.1 Backend Changes
1.  **Bulk API**: `PATCH /api/tasks/bulk` for multi-select actions.
2.  **Tags Support**: Implement `tags` (List of Strings) in `Task` entity.
3.  **User Settings**: (Optional) Add `UserSettings` entity to persist view density/filters on backend.

### 3.2 Frontend Changes
1.  **CompletedTasksSectionComponent**: Collapsible section grouping tasks by `completedAt`.
2.  **Bulk Actions UI**: Multi-select checkboxes and action bar.
3.  **Tags UI**: Small pills and tag selection in edit mode.
4.  **Animations**: Angular animations for task completion and list reordering.
5.  **View Toggle**: Add "Compact" vs "Comfortable" button.

---

## 4. API Contract Summary

| Endpoint | Method | Payload | Description |
| :--- | :--- | :--- | :--- |
| `/api/tasks` | `POST` | `{ "title": "..." }` | Quick Add |
| `/api/tasks/{id}` | `PATCH` | `{ "status": "...", "title": "..." }` | Partial Update |
| `/api/tasks/bulk` | `PATCH` | `{ "ids": [...], "patch": {...} }` | Bulk Update |
| `/api/tasks` | `GET` | `?smartFilter=TODAY&sort=DUE_ASC` | Filtered List |

---

## 5. Risks & Considerations
- **Concurrency**: Inline editing and rapid status changes might lead to race conditions if not handled carefully (optimistic UI vs server state).
- **Date Consistency**: Ensure "Today" filter uses the user's local timezone correctly when communicating with the backend.
- **Mobile UX**: Inline editing must be touch-friendly.
