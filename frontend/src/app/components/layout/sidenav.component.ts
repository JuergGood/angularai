import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { AuthService } from '../../services/auth.service';
import { SystemService, SystemInfo } from '../../services/system.service';

@Component({
  selector: 'app-sidenav',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatListModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './sidenav.component.html',
  styles: [`
    .sidenav-container {
      height: 100vh;
    }
    .sidenav {
      width: 250px;
      border-right: 1px solid rgba(255, 255, 255, 0.12);
      background-color: #1a237e; /* Darker Indigo for better contrast */
      color: white;
      transition: width 0.3s ease;
    }
    .sidenav-collapsed {
      width: 70px;
    }
    .sidenav .mat-toolbar {
      background: inherit;
      color: white;
      font-size: 18px;
      font-weight: 500;
      display: flex;
      align-items: center;
      justify-content: center;
      height: 80px;
      border: none;
    }
    .logo-container {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px 0;
    }
    .brand-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #ff4081; /* Accent Pink */
    }
    .logo-text {
      font-size: 24px;
      font-weight: 700;
      letter-spacing: -1px;
      font-family: 'Inter Tight', sans-serif;
      color: white;
    }
    .mat-toolbar.mat-primary {
      position: sticky;
      top: 0;
      z-index: 2;
      box-shadow: 0 2px 4px rgba(0,0,0,.1);
      display: flex;
      justify-content: space-between;
    }
    .header-spacer {
      flex: 1 1 auto;
    }
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-left: 16px;
    }
    .user-name {
      font-size: 14px;
      font-weight: 500;
    }
    .mat-sidenav-content {
      background-color: #fafafa;
    }
    main {
      padding: 24px;
      min-height: calc(100vh - 64px);
    }
    .active-link {
      background: rgba(255, 255, 255, 0.2) !important;
      color: white !important;
      border-left: 4px solid #ff4081;
    }
    .active-link mat-icon {
      color: white !important;
    }
    mat-nav-list a,
    mat-nav-list a mat-icon,
    mat-nav-list a span,
    mat-nav-list a [matListItemTitle],
    mat-nav-list a .mdc-list-item__primary-text,
    mat-nav-list a .mdc-list-item__secondary-text {
      color: white !important;
    }
    mat-nav-list a {
      margin: 4px 8px;
      border-radius: 4px;
      width: auto !important;
    }
    mat-nav-list a:hover {
      background: rgba(255, 255, 255, 0.1);
    }
  `]
})
export class SidenavComponent {
  isMobile = signal(false);
  isHandheld = signal(false);
  systemInfo = signal<SystemInfo | null>(null);

  isCollapsed = computed(() => this.isHandheld() && !this.isMobile());

  constructor(
    public authService: AuthService,
    private systemService: SystemService,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private breakpointObserver: BreakpointObserver
  ) {
    this.systemService.getSystemInfo().subscribe(info => this.systemInfo.set(info));
    this.breakpointObserver.observe([
      Breakpoints.Handset,
      Breakpoints.Tablet
    ]).subscribe(result => {
      this.isHandheld.set(result.matches);
      this.isMobile.set(this.breakpointObserver.isMatched(Breakpoints.HandsetPortrait));
    });
  }

  onLogout() {
    this.authService.logout();
    this.snackBar.open('Logout successful', 'Close', { duration: 3000 });
    this.router.navigate(['/login']);
  }

  showHelp() {
    this.dialog.open(HelpDialogComponent);
  }
}

@Component({
  selector: 'app-help-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Application Help</h2>
    <mat-dialog-content>
      <p>This application allows you to manage tasks and user profiles.</p>
      <ul>
        <li><strong>Tasks:</strong> Create, edit, and delete your tasks.</li>
        <li><strong>Profile:</strong> Manage your personal information.</li>
        <li><strong>Admin:</strong> Users with admin role can manage all users.</li>
      </ul>
      <p><em>Note: This is a test application for AI code generation.</em></p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `
})
class HelpDialogComponent {}
