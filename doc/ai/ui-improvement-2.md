# UI Improvement Plan - Phase 2

This document outlines the second phase of UI enhancements for the GoodOne application, focusing on navigation logic, accessibility, branding, and mobile responsiveness.

## 1. Navigation Logic & Accessibility

### Restrictive Menu Items
- **Goal**: Ensure that operational menu items (Profile, Tasks, Logout, User Admin) are only visible when a user is authenticated.
- **Implementation**: Wrap these items in an `@if (authService.isLoggedIn())` block within the `SidenavComponent` template.

### New 'Login' Menu Item
- **Goal**: Provide a clear entry point for unauthenticated users.
- **Implementation**: Add a 'Login' item using the `login` Material icon, visible only when `@if (!authService.isLoggedIn())`.

### Accessibility & Color Contrast
- **Goal**: Improve the visibility of the sidenav menu text and icons.
- **Current Issue**: Black font on a deep blue background is difficult to read.
- **Correction**: Standardize on high-contrast white (`#ffffff`) or near-white (`rgba(255, 255, 255, 0.87)`) for all text and icons within the indigo sidenav. Use the `mat-primary-contrast` or explicit hex values to ensure WCAG compliance.

## 2. Branding Update

### New Logo Icon
- **Goal**: Replace the `stars` icon with a more custom "GoodOne" identity.
- **Proposal**: 
    - Use a layered approach: A circular background (`mat-icon` with a circle shape or a CSS-styled div) with the number **1** centered over it.
    - Alternatively, use the `filter_1` Material icon styled with the brand's primary and accent colors to represent "GoodOne".

## 3. Responsive User Administration

### Problem: Table Overflow on Mobile
The `mat-table` in the User Administration page contains too many columns to display comfortably on small handheld devices, causing content to spill over and the "Actions" buttons to be hidden.

### Proposed Solution: Card-Based Mobile Layout
On small screens (detected via `BreakpointObserver`), we will switch from a tabular view to a card-based list view.

**Desktop View (Current)**:
Standard `mat-table` with all columns (Login, Name, Email, Role, Actions).

**Mobile View (Handset Portrait)**:
- Hide the `mat-table`.
- Display a list of `mat-card` elements, one per user.
- Each card will show:
    - **Header**: User Login & Role (as a chip or badge).
    - **Content**: Full Name and Email.
    - **Actions**: Edit and Delete buttons clearly visible at the bottom of the card.

**Technical Implementation**:
```html
@if (!isMobile) {
  <!-- Existing mat-table for desktop -->
  <table mat-table ...> ... </table>
} @else {
  <!-- Mobile Card List -->
  <div class="mobile-user-list">
    @for (user of users; track user.id) {
      <mat-card class="user-mobile-card">
        <mat-card-header>
          <mat-card-title>{{ user.login }}</mat-card-title>
          <mat-card-subtitle>{{ user.role }}</mat-card-subtitle>
        </mat-card-header>
        <mat-card-content>
          <p><strong>Name:</strong> {{ user.firstName }} {{ user.lastName }}</p>
          <p><strong>Email:</strong> {{ user.email }}</p>
        </mat-card-content>
        <mat-card-actions align="end">
          <button mat-button color="primary" (click)="editUser(user)">Edit</button>
          <button mat-button color="warn" (click)="deleteUser(user)">Delete</button>
        </mat-card-actions>
      </mat-card>
    }
  </div>
}
```

### CSS for Mobile Cards:
```css
.user-mobile-card {
  margin-bottom: 16px;
  border-left: 4px solid #303f9f;
}
```
