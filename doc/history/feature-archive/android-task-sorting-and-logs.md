### Proposal: Android Task Sorting and Log Menu

#### 1. Overview
This proposal outlines the implementation of the "Log" menu item and enhancements to the "Tasks" menu in the Android frontend, mirroring the recent changes in the Angular frontend.

#### 2. Log Menu Implementation

##### 2.1. Data Layer
- **`ActionLogDTO`**: Create `ch.goodone.angularai.android.data.remote.dto.ActionLogDTO` with fields: `id`, `timestamp`, `login`, `action`, `details`.
- **`LogApi`**: Create `ch.goodone.angularai.android.data.remote.LogApi` with:
    - `GET api/admin/logs`: returns paged results (need a `LogResponseDTO` similar to Angular's `ActionLogResponse`).
    - `DELETE api/admin/logs`: clears logs.
- **`LogRepository`**: Create `ch.goodone.angularai.android.data.repository.LogRepository` to handle API calls.

##### 2.2. UI Layer
- **`LogViewModel`**: Manage state for logs, filtering (type, date range), and paging.
- **`LogScreen`**: New Compose screen displaying a list (or table) of logs.
    - Action type filter (All, Login, Task, User Admin).
    - Date range selection (using Material3 Date Range Picker).
    - "Clear Filter" button.
    - "Clear Log" button with confirmation dialog.
- **`MainActivity`**: 
    - Add "Logs" to the `ModalNavigationDrawer` (Admin only).
    - Add "Logs" route to `NavHost`.

#### 3. Task Sorting and Filtering Enhancements

##### 3.1. Data Layer Updates
- **`TaskStatus` Enum**: Add `OPEN`, `IN_PROGRESS`, `CLOSED`.
- **`Task` & `TaskDTO`**: Add `status` (String/Enum) and `position` (Int).
- **`TaskApi`**: Add `PUT api/tasks/reorder` accepting a list of task IDs.
- **`TaskRepository`**: Update to support reordering and the new fields.

##### 3.2. UI Layer Updates
- **`TaskViewModel`**: 
    - Add `onReorderTasks(taskIds: List<Long>)`.
    - Add `resetSorting()` (calls backend to reorder by priority, then refreshes).
- **`TaskListScreen`**:
    - **Status Filter**: Add a dropdown or scrollable chips to filter tasks by status.
    - **Relative Sorting**: Implement drag-and-drop reordering. Since Compose `LazyColumn` doesn't have native drag-drop, we will use a library or custom implementation (e.g., `shreyash-p/compose-reorderable` or similar logic).
    - **Reset Sorting Button**: Add a button to reset sorting by priority (High > Medium > Low).
- **`TaskEditScreen`**: Add status selection to the task creation/editing form.

#### 4. Button Naming and Visibility
- **Clear Filter**: Following the Angular implementation, the "Clear Filter" button will be removed/hidden from the Task menu as it is no longer required (filtering is handled by the status selector).
- **Reset Sorting**: A "Reset Sorting" button will be added/maintained to revert to priority-based sorting.

#### 5. Security
- "Logs" menu item and screen will be restricted to users with `ROLE_ADMIN`, similar to "User Admin".
- API calls to `/api/admin/**` will include the necessary Basic Auth headers.
