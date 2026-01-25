import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-verify-success',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    TranslateModule
  ],
  template: `
    <div class="verify-page">
      <mat-card class="main-card verify-card">
        <mat-card-content class="verify-content">
          <div class="icon-container success">
            <mat-icon color="primary" class="large-icon">check_circle</mat-icon>
          </div>

          <h1>{{ 'REGISTER.VERIFY_SUCCESS_TITLE' | translate }}</h1>
          <p class="subtitle">{{ 'REGISTER.VERIFY_SUCCESS_TEXT' | translate }}</p>

          <div class="actions">
            <button mat-flat-button color="primary" (click)="onGoToLogin()">
              {{ 'NAV.LOGIN' | translate }}
            </button>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .verify-page {
      display: flex;
      justify-content: center;
      padding: 40px 24px;
      background: var(--bg);
      min-height: calc(100vh - 64px);
    }
    .verify-card {
      width: 100%;
      max-width: 500px;
      height: fit-content;
      text-align: center;
      padding: 32px 16px;
    }
    .verify-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
    }
    .icon-container {
      margin-bottom: 8px;
    }
    .large-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
    }
    h1 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
      color: var(--text);
    }
    .subtitle {
      color: var(--text-muted);
      line-height: 1.6;
      margin: 0;
      max-width: 400px;
    }
    .actions {
      margin-top: 24px;
      width: 100%;
    }
    .actions button {
      width: 100%;
      padding: 8px 0;
    }
  `]
})
export class VerifySuccessComponent {
  constructor(private router: Router) {}

  onGoToLogin() {
    this.router.navigate(['/login']);
  }
}
