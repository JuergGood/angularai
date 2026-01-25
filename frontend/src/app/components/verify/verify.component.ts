import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-verify',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatProgressSpinnerModule, TranslateModule],
  template: `
    <div class="verify-page">
      <mat-card class="main-card verify-card">
        <mat-card-content class="verify-content">
          <div class="loading-container">
            <mat-spinner diameter="50"></mat-spinner>
            <p>{{ 'REGISTER.VERIFYING_ACCOUNT' | translate }}</p>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .verify-page {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
      padding: 20px;
    }
    .verify-card {
      width: 100%;
      max-width: 500px;
    }
    .verify-content {
      padding: 40px !important;
      text-align: center;
    }
    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 20px;
    }
    p {
      font-size: 1.1rem;
      color: var(--text-muted);
    }
  `]
})
export class VerifyComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.router.navigate(['/verify/error'], { queryParams: { reason: 'invalid' } });
      return;
    }

    this.http.get(`/api/auth/verify?token=${token}`, { observe: 'response' }).subscribe({
      next: (response) => {
        this.router.navigate(['/verify/success']);
      },
      error: (err) => {
        const reason = err.error?.reason || 'invalid';
        const email = err.error?.email || '';
        this.router.navigate(['/verify/error'], { queryParams: { reason, email } });
      }
    });
  }
}
