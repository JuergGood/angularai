import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './register.component.html',
  styles: [`
    .register-page {
      display: flex;
      justify-content: center;
      padding: 20px;
    }
    .register-card {
      width: 100%;
      max-width: 500px;
    }
    mat-form-field {
      width: 100%;
      margin-bottom: 10px;
    }
    .actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 10px;
    }
    .error { color: #f44336; margin-top: 10px; text-align: center; }
    .success { color: #4caf50; margin-top: 10px; text-align: center; }
  `]
})
export class RegisterComponent {
  user: User = {
    firstName: '',
    lastName: '',
    login: '',
    password: '',
    email: '',
    birthDate: '',
    address: ''
  };
  confirmPassword = '';
  error = '';
  message = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  onSubmit() {
    if (this.user.password !== this.confirmPassword) {
      this.error = 'Passwords do not match';
      return;
    }

    const userToRegister = { ...this.user };

    this.authService.register(userToRegister).subscribe({
      next: () => {
        this.message = 'Registration successful! Redirecting to login...';
        this.error = '';
        this.cdr.detectChanges();
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.error = err.error || 'Registration failed. User might already exist.';
        this.message = '';
        this.cdr.detectChanges();
      }
    });
  }
}
