# Frontend Style Guideline

This document defines the standard UI patterns and styles for the AngularAI project to ensure consistency across all pages. Junie must follow these guidelines for all UI-related changes.

## 1. Core Principles
- **Consistency**: Use the same components, spacing, and layouts across all pages.
- **Design Tokens**: Always use the CSS variables defined in `src/styles.css` (e.g., `--bg`, `--surface`, `--text`, `--brand`).
- **Responsive**: Use Flexbox and Grid to ensure layouts work on both desktop and mobile.
- **Dark Mode**: All components must look good in both light and dark themes using the provided design tokens.

## 2. Layout & Spacing
- **Page Container**: Use a div with class `page-container` (or specific component container like `dashboard-container`) with `padding: 24px`.
- **Spacing**: Use standard margins/gaps: `12px`, `16px`, `24px`, or `32px`.
- **Grids**: Use `display: grid` with `gap: 24px` for dashboard-like layouts.

## 3. Material Cards
- **Standard Cards**: Use `<mat-card>` with `class="main-card"` for primary content areas.
- **Summary Cards**: For KPIs/stats, use `class="summary-card interactive-card"`:
    - Should have a `border-top: 4px solid var(--brand)`.
    - Use `.stat-value` for large numbers (font-size: 2.8rem, font-weight: 800).
- **Interactive Cards**: Cards that link to other pages should have `class="interactive-card"` for hover effects (translateY and increased shadow).

## 4. Tables (Angular Material)
- **Zebra Striping**: Apply zebra striping to all tables using:
    ```css
    tr:nth-child(even) { background-color: color-mix(in srgb, var(--surface-2) 70%, transparent); }
    ```
- **Hover Effect**: Rows should highlight on hover: `tr:hover { background-color: var(--brand-weak) !important; }`.
- **Headers**: Use uppercase, small font size (11px), bold, and `var(--surface-2)` background.
- **Compact Tables**: Use `class="compact-table"` for dashboard widgets (smaller padding).

## 5. Chips & Semantic Colors
Use semantic chips for status and roles. Do not rely on hardcoded colors; use background-opacity patterns:
- **Primary/Brand**: `background: rgba(63, 81, 181, 0.14); color: var(--brand);`
- **Success/Green**: `background: rgba(76, 175, 80, 0.16); color: #2e7d32;`
- **Warning/Orange**: `background: rgba(255, 152, 0, 0.16); color: #e65100;`
- **Error/Red**: `background: rgba(211, 47, 47, 0.12); color: #c62828;`
- **Neutral**: `background: color-mix(in srgb, var(--surface-2) 70%, transparent); color: var(--text-muted);`

## 6. Forms
- **Appearance**: Always use `appearance="outline"` for `mat-form-field`.
- **Layout**: Use a grid (e.g., `class="form-grid"`) for multiple fields.
- **Actions**: Place action buttons at the bottom in a `class="form-actions"` div.

## 7. Icons
- Use Material Icons (`<mat-icon>`).
- Use consistent icons for actions: `edit` (Edit), `delete` (Delete), `visibility` (View), `person_add` (Add User), `search` (Search/Filter).

## 8. Typography
- **Page Titles**: Use `<h2>` with `class="page-title"` in a `page-toolbar`.
- **Muted Text**: Use `class="muted"` for secondary information (uses `--text-muted`).
- **Weights**: Use `font-weight: 500` for emphasis in tables, `600` for card titles.
