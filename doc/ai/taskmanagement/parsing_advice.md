# Advice on Task Parsing Architecture

## Current State: Frontend Parsing
The current implementation of the "Quick Add Task" feature uses frontend-based parsing (TypeScript) to interpret user input. It supports multiple separators (`|`, `;`, `,`) and a heuristic-based space parsing for simple natural language inputs (e.g., "Buy milk tomorrow HIGH").

### Pros:
- **Instant Feedback**: Validation errors (invalid date, priority, etc.) are shown immediately without a network round-trip.
- **Low Latency**: Simple tasks are processed entirely on the client side before being sent to the API.

### Cons:
- **Limited Complexity**: Heuristic parsing is brittle. As the syntax becomes more complex (e.g., multi-word titles with space separators), the logic becomes harder to maintain and more error-prone.
- **Maintenance Overhead**: Parsing logic must be duplicated if other clients (e.g., Mobile Android app) want the same "Quick Add" experience.
- **Internationalization**: Parsing natural language dates ("today", "morgen") requires frontend awareness of all supported locales.

---

## Evolution: Unified Backend Parsing (Implemented)

The parsing logic has been moved to the backend to ensure consistency across all clients and to enable "parse-as-you-type" functionality.

### 1. Unified Logic in Backend
The `TaskParserService` in the Spring Boot backend now handles both heuristic (space-based) and structured (separator-based) parsing.
- **Location**: `ch.goodone.angularai.backend.service.TaskParserService`
- **Supported Formats**:
    - `Title | Description | Due Date | Priority | Status` (Separators: `|`, `;`, `,`)
    - `Title [Date] [Priority] [Status]` (Heuristic space-based)

### 2. "Parse-as-you-type" API
A new analysis endpoint has been implemented:
- **Endpoint**: `POST /api/tasks/analyze`
- **Request Body**: Raw text string
- **Response**: `TaskDTO` representing the interpreted task

The frontend calls this API as the user types (with a 300ms debounce), allowing the UI to show real-time feedback of what has been recognized.

### 3. Benefits
- **Consistency**: The Android app, Web app, and future integrations will all interpret input the same way.
- **Improved UX**: Users see chips for recognized dates, priorities, and statuses *before* submitting, reducing errors.
- **Maintainability**: Complex parsing logic is centralized in one place.
