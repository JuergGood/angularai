import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { TranslateModule } from '@ngx-translate/core';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, TranslateModule],
  templateUrl: './login.component.html',
  styles: [`
    .login-page {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 85vh;
      padding: 24px;
    }
    .login-card {
      width: 100%;
      max-width: 420px;
    }
    mat-form-field {
      width: 100%;
    }
    .form-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid var(--border);
    }
    .links-container {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
    }
    .forgot-link {
      font-size: 0.85em;
      margin-top: -8px;
    }
    .error {
      color: #f44336;
      margin-top: 16px;
      text-align: center;
    }
  `]
})
export class LoginComponent {
  login = '';
  password = '';
  error = '';
  hidePassword = true;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  onSubmit() {
    this.authService.login(this.login, this.password).subscribe({
      next: (user) => {
        if (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_ADMIN_READ') {
          this.router.navigate(['/dashboard']);
        } else {
          this.router.navigate(['/tasks']);
        }
      },
      error: (err) => {
        console.error('Login error:', err);
        if (err.status === 401) {
          this.error = 'COMMON.ERROR_LOGIN_FAILED';
        } else if (err.status === 403) {
          this.error = 'ADMIN.ERROR_USER_NOT_ACTIVE';
        } else {
          this.error = 'COMMON.ERROR';
        }
        this.cdr.detectChanges();
      }
    });
  }
}
