import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <div class="login-page">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>Login</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form (ngSubmit)="onSubmit()">
            <mat-form-field appearance="fill">
              <mat-label>Login</mat-label>
              <input matInput type="text" name="login" [(ngModel)]="login" required>
            </mat-form-field>

            <mat-form-field appearance="fill">
              <mat-label>Password</mat-label>
              <input matInput type="password" name="password" [(ngModel)]="password" required>
            </mat-form-field>

            <div class="actions">
              <button mat-raised-button color="primary" type="submit">Login</button>
            </div>

            @if (error) {
              <p class="error">{{ error }}</p>
            }
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-page {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 80vh;
    }
    .login-card {
      width: 400px;
    }
    mat-form-field {
      width: 100%;
      margin-bottom: 10px;
    }
    .actions {
      display: flex;
      justify-content: flex-end;
    }
    .error {
      color: #f44336;
      margin-top: 10px;
      text-align: center;
    }
  `]
})
export class LoginComponent {
  login = '';
  password = '';
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    this.authService.login(this.login, this.password).subscribe({
      next: () => this.router.navigate(['/profile']),
      error: () => this.error = 'Invalid login or password'
    });
  }
}
