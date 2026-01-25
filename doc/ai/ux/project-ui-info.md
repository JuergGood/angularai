### Design System & UI Architecture

This document provides a technical overview of the UI framework and design system used in the AngularAI project.

#### 1. UI Framework: Angular Material
- **Version**: **21.1.x** (latest stable).
- **Material Design Version**: **Material 3** (default for Angular Material 18+).
- **Core Principles**:
    - **Standalone Components**: All UI components are `standalone: true`.
    - **Modern Control Flow**: Uses `@if`, `@for`, `@empty` instead of `*ngIf`/`*ngFor`.
    - **Signals**: Used for reactive state management (e.g., `AuthService.currentUser`).

#### 2. Design System & Theming
- **Base Theme**: `indigo-pink` prebuilt Material theme.
- **Custom Theme**: Extensively customized via CSS variables in `src/styles.css` for both **Light** and **Dark** modes.
- **Color Variables (Design Tokens)**:
    - `--bg`: Main background color.
    - `--surface`: Component background (cards, dialogs).
    - `--surface-2`: Secondary surface color (headers, zebra striping).
    - `--text`: Primary text color.
    - `--text-muted`: Secondary/hint text.
    - `--brand`: Primary action color (#3f51b5).
    - `--border`: Standard border/divider color.
- **Utility Tokens**:
    - `--r`: Standard border-radius (12px).
    - `--shadow-1`, `--shadow-2`: Standardized elevation shadows.

#### 3. Navigation & Routing
- **Main Routing**: Defined in `src/app/app.routes.ts`.
- **Default Path**: Redirects to `/tasks`.
- **Protected Routes**: Most features (Dashboard, Tasks, Profile) are protected by `authGuard`. Admin features are protected by `adminGuard`.
- **Landing Logic**: 
    - Unauthenticated users → `/login`.
    - Authenticated users → `/tasks`.
- **Sidenav Navigation**: `SidenavComponent` handles the main layout and sidebar navigation.

#### 4. Reusable UI Components
The project follows a modular structure where reusable logic is extracted into sub-components.

- **Layout Container**: `app-root` -> `app-sidenav` (Wraps all content in a responsive sidebar + toolbar).
- **Task Management**:
    - `app-quick-add-task`: Inline task creation with natural language parsing.
    - `app-task-filter-chips`: Filter toolbar for status/priority.
    - `app-completed-tasks-section`: Collapsible section for historical tasks.
- **Common Dialogs**:
    - `ConfirmDialogComponent`: Generic confirmation popup.
- **Material Components**: Extensively uses `mat-card`, `mat-form-field` (outline appearance), `mat-button` (flat/stroked), `mat-menu`, and `mat-checkbox`.

#### 5. Style Rules & UX
- **Spacing**: Follows an 8px grid system (margins/padding: 8px, 16px, 24px, 32px).
- **Typography**: Uses Roboto (default Material font). Headlines use 700 weight; body text uses 400.
- **Form Design**: 
    - Always use `appearance="outline"` for `mat-form-field`.
    - Extract templates and styles for components unless they are extremely small (<60 lines).
- **Accessibility**: ARIA labels and appropriate Material component roles are enforced.
- **Translations**: Integrated with `ngx-translate`. Supports English (`en`) and Swiss German (`de-ch`). Note: 'ss' is used instead of 'ß' for Swiss German consistency.

#### 6. Key Files
- **Global Styles**: `frontend/src/styles.css`
- **Routing**: `frontend/src/app/app.routes.ts`
- **Root Component**: `frontend/src/app/app.ts` / `app.component.html`
- **Main Layout**: `frontend/src/app/components/layout/sidenav.component.ts`
