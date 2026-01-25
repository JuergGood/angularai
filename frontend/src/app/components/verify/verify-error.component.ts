import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-verify-error',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TranslateModule
  ],
  template: `
    <div class="verify-page">
      <mat-card class="main-card verify-card">
        <mat-card-content class="verify-content">
          <div class="icon-container error">
            <mat-icon color="warn" class="large-icon">error_outline</mat-icon>
          </div>

          <h1>{{ 'REGISTER.VERIFY_ERROR_TITLE' | translate }}</h1>
          <p class="subtitle">{{ errorTextKey | translate }}</p>

          <div class="actions">
            <button mat-flat-button color="primary" (click)="onGoToLogin()">
              {{ 'NAV.LOGIN' | translate }}
            </button>

            @if (email) {
              <button mat-button class="resend-btn" (click)="onResend()" [disabled]="isResending">
                @if (isResending) {
                  <mat-spinner diameter="20"></mat-spinner>
                } @else {
                  {{ 'REGISTER.RESEND_VERIFICATION' | translate }}
                }
              </button>
            }
          </div>

          @if (resendMessage) {
            <p class="feedback" [class.success]="resendSuccess" [class.error]="!resendSuccess">
              {{ resendMessage | translate }}
            </p>
          }
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
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .actions button {
      width: 100%;
      padding: 8px 0;
    }
    .resend-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }
    .feedback {
      margin-top: 16px;
      font-size: 14px;
      padding: 8px 16px;
      border-radius: 4px;
    }
    .feedback.success {
      background: rgba(76, 175, 80, 0.1);
      color: #4caf50;
    }
    .feedback.error {
      background: rgba(244, 67, 54, 0.1);
      color: #f44336;
    }
  `]
})
export class VerifyErrorComponent implements OnInit {
  errorTextKey = 'REGISTER.VERIFY_ERROR_TEXT_INVALID';
  email: string | null = null;
  isResending = false;
  resendMessage = '';
  resendSuccess = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const reason = this.route.snapshot.queryParamMap.get('reason');
    this.email = this.route.snapshot.queryParamMap.get('email');

    if (reason === 'expired') {
      this.errorTextKey = 'REGISTER.VERIFY_ERROR_TEXT_EXPIRED';
    } else {
      this.errorTextKey = 'REGISTER.VERIFY_ERROR_TEXT_INVALID';
    }
  }

  onGoToLogin() {
    this.router.navigate(['/login']);
  }

  onResend() {
    if (!this.email || this.isResending) return;

    this.isResending = true;
    this.resendMessage = '';

    this.authService.resendVerification(this.email).subscribe({
      next: () => {
        this.isResending = false;
        this.resendSuccess = true;
        this.resendMessage = 'REGISTER.RESEND_SUCCESS';
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to resend verification:', err);
        this.isResending = false;
        this.resendSuccess = false;
        this.resendMessage = 'REGISTER.RESEND_ERROR';
        this.cdr.detectChanges();
      }
    });
  }
}
