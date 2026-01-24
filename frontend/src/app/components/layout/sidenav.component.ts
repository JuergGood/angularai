import { Component, computed, signal, effect, OnDestroy } from '@angular/core';
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
    body.theme-dark .header-toolbar {
      background: linear-gradient(90deg, #1e293b 0%, #0f172a 100%) !important;
      border-bottom: 1px solid var(--border);
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
      transition: all 0.3s ease;
      position: relative;
    }
    mat-nav-list a:hover {
      background-color: rgba(63, 81, 181, 0.06) !important;
      color: var(--brand) !important;
    }
    mat-nav-list a mat-icon {
      margin-right: 16px;
      transition: color 0.3s ease;
    }
    mat-nav-list a [matListItemTitle] {
      font-weight: 500;
      font-size: 14px;
    }
    .landing-info-banner {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px 24px;
      background: linear-gradient(90deg, #e3f2fd 0%, #bbdefb 100%);
      border-bottom: 1px solid #90caf9;
      color: #0d47a1;
      font-weight: 500;
      animation: slideDown 0.5s ease-out;
    }
    body.theme-dark .landing-info-banner {
      background: linear-gradient(90deg, #1e293b 0%, #334155 100%);
      border-bottom: 1px solid #475569;
      color: #e2e8f0;
    }
    .landing-info-banner mat-icon {
      color: #1976d2;
    }
    body.theme-dark .landing-info-banner mat-icon {
      color: #38bdf8;
    }
    @keyframes slideDown {
      from { transform: translateY(-100%); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
    .landing-info-banner.hiding {
      animation: slideUp 0.5s ease-in forwards;
    }
    @keyframes slideUp {
      from { transform: translateY(0); opacity: 1; }
      to { transform: translateY(-100%); opacity: 0; }
    }
    .banner-close {
      margin-left: auto;
      color: inherit;
      opacity: 0.7;
    }
    .banner-close:hover {
      opacity: 1;
    }
  `]
})
export class SidenavComponent implements OnDestroy {
  isMobile = signal(false);
  isHandheld = signal(false);
  systemInfo = signal<SystemInfo | null>(null);
  geolocationEnabled = signal(false);
  recaptchaConfigIndex = signal(1);
  showBanner = signal(true);
  isHiding = signal(false);
  private actionCount = 0;
  private autoHideTimer: any;
  private routerSubscription: any;

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
    this.systemService.getSystemInfo().subscribe(info => {
      this.systemInfo.set(info);
      if (info.landingMessage) {
        this.startBannerTimers();
      }
    });

    // Reactively fetch geolocation and recaptcha settings when admin status changes
    effect(() => {
      if (this.authService.isAdmin()) {
        this.systemService.getGeolocationEnabled().subscribe({
          next: res => this.geolocationEnabled.set(res.enabled),
          error: err => console.error('Failed to fetch geolocation status', err)
        });
        this.systemService.getRecaptchaConfigIndex().subscribe({
          next: res => this.recaptchaConfigIndex.set(res.index),
          error: err => console.error('Failed to fetch reCAPTCHA config index', err)
        });
      }
    });

    this.breakpointObserver.observe([
      Breakpoints.Handset,
      Breakpoints.Tablet
    ]).subscribe(result => {
      this.isHandheld.set(result.matches);
      this.isMobile.set(this.breakpointObserver.isMatched(Breakpoints.HandsetPortrait));
    });

    // Track user actions (navigation)
    this.routerSubscription = this.router.events.subscribe(event => {
      if (event.constructor.name === 'NavigationEnd') {
        this.actionCount++;
        if (this.actionCount >= 3) {
          this.hideBanner();
        }
      }
    });
  }

  private startBannerTimers() {
    // Auto-hide after 20 seconds
    this.autoHideTimer = setTimeout(() => {
      this.hideBanner();
    }, 20000);
  }

  hideBanner() {
    if (this.isHiding()) return;
    this.isHiding.set(true);
    setTimeout(() => {
      this.showBanner.set(false);
      if (this.autoHideTimer) clearTimeout(this.autoHideTimer);
    }, 500); // Match animation duration
  }

  ngOnDestroy() {
    if (this.autoHideTimer) clearTimeout(this.autoHideTimer);
    if (this.routerSubscription) this.routerSubscription.unsubscribe();
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

  toggleGeolocation() {
    const newState = !this.geolocationEnabled();
    this.systemService.setGeolocationEnabled(newState).subscribe({
      next: () => {
        this.geolocationEnabled.set(newState);
        this.snackBar.open(`Geolocation ${newState ? 'enabled' : 'disabled'}`, 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.snackBar.open('Failed to update geolocation setting', 'Close', { duration: 3000 });
        console.error('Error toggling geolocation', err);
      }
    });
  }

  setRecaptchaConfig(index: number) {
    this.systemService.setRecaptchaConfigIndex(index).subscribe({
      next: () => {
        this.recaptchaConfigIndex.set(index);
        this.snackBar.open(`reCAPTCHA config ${index} selected. Please refresh the registration page to apply.`, 'Close', { duration: 5000 });
      },
      error: (err) => {
        this.snackBar.open('Failed to update reCAPTCHA setting', 'Close', { duration: 3000 });
        console.error('Error setting reCAPTCHA config', err);
      }
    });
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
