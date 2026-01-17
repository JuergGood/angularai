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
import { MatDividerModule } from '@angular/material/divider';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { AuthService } from '../../services/auth.service';
import { SystemService, SystemInfo } from '../../services/system.service';
import { I18nService, Language } from '../../services/i18n.service';
import { ThemeService } from '../../services/theme.service';
import { TranslateModule } from '@ngx-translate/core';

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
    MatDialogModule,
    MatDividerModule,
    TranslateModule
  ],
  templateUrl: './sidenav.component.html',
  styles: [`
    .sidenav-container {
      height: 100vh;
    }
    .sidenav {
      width: 260px;
      border-right: 1px solid var(--border);
      background-color: var(--surface);
      color: var(--text);
      transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .sidenav-collapsed {
      width: 76px;
    }
    .logo-container {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px 20px;
      height: 64px;
      box-sizing: border-box;
    }
    .sidenav-logo {
      margin-bottom: 8px;
    }
    .header-logo {
      padding: 0;
      margin-right: 24px;
    }
    .brand-box {
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: 800;
      font-size: 20px;
      box-shadow: 0 4px 8px rgba(102, 126, 234, 0.25);
      flex-shrink: 0;
      letter-spacing: -1px;
      transition: transform 0.2s ease;
    }
    .brand-box:hover {
      transform: scale(1.05);
    }
    .brand-g {
      margin-right: -1px;
    }
    .brand-1 {
      font-size: 16px;
      margin-top: 4px;
      opacity: 0.9;
    }
    .logo-text {
      font-size: 22px;
      font-weight: 600;
      letter-spacing: -0.5px;
      font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
      color: var(--text);
    }
    .header-logo-text {
      color: white;
    }
    @media (max-width: 480px) {
      .header-logo-text {
        display: none;
      }
    }
    .header-toolbar {
      background: linear-gradient(90deg, #4b6cb7 0%, #182848 100%) !important;
      color: white;
      height: 64px;
      padding: 0 24px;
      display: flex;
      align-items: center;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
    .header-spacer {
      flex: 1 1 auto;
    }
    .user-info-group {
      display: flex;
      align-items: center;
      gap: 12px;
      background: rgba(255, 255, 255, 0.1);
      padding: 4px 4px 4px 12px;
      border-radius: 32px;
      border: 1px solid rgba(255, 255, 255, 0.2);
    }
    .user-profile-button {
      display: flex;
      align-items: center;
      gap: 4px;
      cursor: pointer;
      padding: 4px;
      border-radius: 24px;
      transition: background 0.2s;
    }
    .user-profile-button:hover {
      background: rgba(255, 255, 255, 0.1);
    }
    .user-avatar {
      width: 32px;
      height: 32px;
      background: #764ba2;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2px solid rgba(255,255,255,0.8);
    }
    .user-avatar mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }
    .dropdown-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
      opacity: 0.7;
    }
    .user-menu-header {
      padding: 16px;
      display: flex;
      flex-direction: column;
      pointer-events: none;
      background: var(--brand-weak);
      border-bottom: 1px solid var(--border);
      margin-bottom: 8px;
    }
    .user-menu-name {
      font-weight: 700;
      font-size: 16px;
      color: var(--brand);
      margin-bottom: 2px;
    }
    .user-menu-role {
      font-size: 12px;
      font-weight: 500;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .info-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 16px;
      opacity: 0.8;
      cursor: default;
    }
    .info-item mat-icon {
      color: var(--text-muted);
      font-size: 20px;
      width: 20px;
      height: 20px;
    }
    .info-label {
      font-size: 11px;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      display: block;
      line-height: 1;
      margin-bottom: 2px;
    }
    .info-value {
      font-size: 14px;
      font-weight: 500;
      color: var(--text);
      display: block;
    }
    .settings-button {
      background: rgba(255, 255, 255, 0.15) !important;
      color: white !important;
    }
    .theme-toggle-button {
      background: rgba(255, 255, 255, 0.15) !important;
      color: white !important;
    }
    .theme-toggle-button:hover {
      background: rgba(255, 255, 255, 0.22) !important;
    }
    .mat-sidenav-content {
      background-color: var(--bg);
    }
    main {
      padding: 32px;
      min-height: calc(100vh - 64px);
    }
    .active-link {
      background: linear-gradient(90deg, var(--brand-weak) 0%, rgba(255, 255, 255, 0) 100%) !important;
      color: var(--brand) !important;
      border-left: 4px solid var(--brand);
      font-weight: 600;
    }
    .active-link mat-icon {
      color: var(--brand) !important;
    }
    mat-nav-list a {
      margin: 4px 12px;
      border-radius: 0 8px 8px 0;
      height: 48px !important;
      color: var(--text-muted) !important;
      transition: all 0.3s ease;
      position: relative;
    }
    mat-nav-list a:hover {
      background-color: rgba(63, 81, 181, 0.06) !important;
      color: var(--brand) !important;
    }
    mat-nav-list a mat-icon {
      color: var(--text-muted) !important;
      margin-right: 16px;
      transition: color 0.3s ease;
    }
    mat-nav-list a [matListItemTitle] {
      font-weight: 500;
      font-size: 14px;
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
    public i18nService: I18nService,
    public theme: ThemeService,
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

  setLanguage(lang: string) {
    this.i18nService.setLanguage(lang as Language);
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
