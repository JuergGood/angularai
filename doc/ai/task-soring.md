### Proposal: Task State and Relative Sorting

#### 1. Overview
Enhance the Task Management module to support task states ('open', 'in progress', 'closed') and relative sorting. Users will be able to filter tasks by status and manually reorder them via drag-and-drop. A "Reset Sorting" feature will allow reverting to priority-based ordering.

#### 2. Backend Changes (Spring Boot)

##### 2.1. `TaskStatus` Enum
Create a new enum `ch.goodone.angularai.backend.model.TaskStatus`:
- Values: `OPEN`, `IN_PROGRESS`, `CLOSED`.

##### 2.2. `Task` Entity Enhancements
Update `ch.goodone.angularai.backend.model.Task`:
- Add `@Enumerated(EnumType.STRING) TaskStatus status`. Default: `OPEN`.
- Add `Integer position` to store the relative order of tasks for each user.

##### 2.3. `TaskDTO` Enhancements
Update `ch.goodone.angularai.backend.dto.TaskDTO`:
- Add `String status` field.
- Add `Integer position` field.
- Update `fromEntity()` to include these new fields.

##### 2.4. `TaskRepository` Enhancements
Update `ch.goodone.angularai.backend.repository.TaskRepository`:
- Update `findByUser(User user)` to `findByUserOrderByPositionAsc(User user)` to ensure tasks are returned in their custom order.
- Add support for finding tasks by user and status if server-side filtering is preferred, though client-side filtering might be sufficient for small lists.

##### 2.5. `TaskController` Enhancements
- Update `createTask`: Initialize `position` for the new task (e.g., max position + 1).
- Add `PUT /api/tasks/reorder`: Accept a list of task IDs in their new order and update `position` values in the database.
- Update `updateTask`: Allow updating the `status`.

#### 3. Frontend Changes (Angular)

##### 3.1. `Task` Model Enhancements
Update `frontend/src/app/models/task.model.ts`:
- Add `TaskStatus` enum.
- Add `status` and `position` fields to `Task` interface.

##### 3.2. `TaskService` Enhancements
Update `frontend/src/app/services/task.service.ts`:
- Add `reorderTasks(taskIds: number[]): Observable<void>` to sync the new order with the backend.

##### 3.3. `TasksComponent` UI Enhancements
- **Filter Section**:
    - Add a `mat-select` for status filtering (All, Open, In Progress, Closed).
    - Add a "Clear Filter" button.
- **Task List (Drag-and-Drop)**:
    - Use `@angular/cdk/drag-drop` to implement relative sorting.
    - Wrap the task list in a `cdkDropList` and each task in `cdkDrag`.
    - Handle `(cdkDropListDropped)` event to reorder the local array and call `TaskService.reorderTasks()`.
- **Form**:
    - Add a status selector to the Add/Edit task form.
- **Actions**:
    - Add a "Reset Sorting" button. This will:
        1. Sort tasks locally by priority (High > Medium > Low).
        2. Assign new `position` values based on this order.
        3. Sync with the backend.

##### 3.4. Priority-based Sorting Logic
For "Reset Sorting", use a mapping:
- `CRITICAL` (if exists) -> 0
- `HIGH` -> 1
- `MEDIUM` -> 2
- `LOW` -> 3
Tasks with the same priority will retain their relative order or be sorted by due date as a secondary criterion.

#### 4. UI/UX Details
- Use Material Design `mat-chip` or icons to visually represent different task statuses.
- Use a drag handle icon (e.g., `drag_indicator`) to indicate reorderable items.
- Ensure the filter and reset buttons are easily accessible above the task list.

#### 5. Implementation Steps
1. Backend: Add `TaskStatus`, update `Task` entity and `TaskDTO`.
2. Backend: Implement reordering logic in `TaskController`.
3. Frontend: Update models and services.
4. Frontend: Integrate `CdkDragDrop` into `TasksComponent`.
5. Frontend: Add filtering and "Reset Sorting" functionality.
