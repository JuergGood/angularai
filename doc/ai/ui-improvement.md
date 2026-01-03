# UI Improvement Plan

This document outlines the planned UI enhancements for the GoodOne application to improve aesthetics, branding, and responsiveness.

## 1. Branding and Header
- **Rename Header**: The main application title in the top app bar will be renamed from "User Management System" to **"GoodOne"**.
- **Top App Bar**: The header will be implemented using a dedicated `mat-toolbar` with `position: sticky` to ensure it remains visible while scrolling.

## 2. Navigation Sidebar (Sidenav)
- **Background Color**: The current white background (`#ffffff`) will be replaced with a more modern and appealing color, such as a deep indigo (`#303f9f`) or a subtle slate gray, with high-contrast text and icons.
- **Conditional Visibility**: The sidebar will only be rendered and accessible if the user is successfully logged in.
- **Logo Icon**: The current placeholder text/icon will be replaced with a graphical icon that matches the branding in `doc/img/GoodOne.jpg` (e.g., using `mat-icon` with `stars` or a custom SVG).

## 3. Responsive Design
To ensure a great experience on all devices, the sidenav will implement the following responsive states using Angular's `BreakpointObserver`:

| Device Type | Sidenav State | Behavior |
| :--- | :--- | :--- |
| **Desktop** | Expanded | Full menu with icons and labels visible. |
| **Tablet / Handheld** | Mini-Variant | Collapsed sidebar showing only menu icons to save screen space. |
| **Mobile (Very Small)** | Over / Hidden | Sidenav is completely hidden and toggled via a hamburger menu icon in the top app bar. |

## 4. Implementation Details (Code Snippets)

### Template Changes (`sidenav.component.html`)
```html
<!-- Main Header -->
<mat-toolbar color="primary">
  @if (isMobile) {
    <button mat-icon-button (click)="sidenav.toggle()">
      <mat-icon>menu</mat-icon>
    </button>
  }
  <span>GoodOne</span>
</mat-toolbar>

<!-- Sidenav Container -->
<mat-sidenav-container>
  <mat-sidenav #sidenav [mode]="isMobile ? 'over' : 'side'" [opened]="!isMobile && authService.isLoggedIn()">
    <!-- Styled Logo Section -->
    <div class="logo-section">
      <mat-icon class="brand-icon">rocket_launch</mat-icon>
      @if (!isCollapsed) { <span class="brand-name">GoodOne</span> }
    </div>
    
    <mat-nav-list>
      <!-- Menu Items -->
    </mat-nav-list>
  </mat-sidenav>
</mat-sidenav-container>
```

### Styling Changes (`sidenav.component.ts`)
```css
.sidenav {
  background-color: #3f51b5; /* Primary Indigo */
  color: white;
  transition: width 0.3s ease;
}

.active-link {
  background: rgba(255, 255, 255, 0.15) !important;
  border-left: 4px solid #ff4081; /* Accent Pink */
}
```
