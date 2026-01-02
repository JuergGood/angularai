import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth.service';

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
    MatIconModule
  ],
  templateUrl: './sidenav.component.html',
  styles: [`
    .sidenav-container {
      height: 100vh;
    }
    .sidenav {
      width: 250px;
      border-right: 1px solid rgba(0, 0, 0, 0.12);
      background-color: #ffffff;
    }
    .sidenav .mat-toolbar {
      background: inherit;
      font-size: 18px;
      font-weight: 500;
      display: flex;
      align-items: center;
      justify-content: center;
      height: 80px;
    }
    .logo-container {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 16px 0;
    }
    .logo-text {
      font-size: 24px;
      font-weight: 700;
      letter-spacing: -1px;
      font-family: 'Inter Tight', sans-serif;
    }
    .logo-good {
      color: #3f51b5;
    }
    .logo-one {
      color: #ff4081;
    }
    .mat-toolbar.mat-primary {
      position: sticky;
      top: 0;
      z-index: 1;
      box-shadow: 0 2px 4px rgba(0,0,0,.1);
    }
    .mat-sidenav-content {
      background-color: #fafafa;
    }
    main {
      padding: 24px;
      min-height: calc(100vh - 64px);
    }
    .active-link {
      background: rgba(63, 81, 181, 0.1) !important;
      color: #3f51b5 !important;
    }
    .active-link mat-icon {
      color: #3f51b5 !important;
    }
    mat-nav-list a {
      margin: 4px 8px;
      border-radius: 4px;
      width: auto !important;
    }
  `]
})
export class SidenavComponent {
  constructor(public authService: AuthService, private router: Router) {}

  onLogout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
