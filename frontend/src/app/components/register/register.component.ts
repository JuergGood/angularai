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
    MatDatepickerModule,
    TranslateModule
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
    birthDate: null,
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
      this.error = 'ADMIN.ERROR_PASSWORD_MATCH';
      return;
    }

    const userToRegister = { ...this.user };
    if (userToRegister.birthDate && (userToRegister.birthDate as any) instanceof Date) {
      const date = userToRegister.birthDate as unknown as Date;
      // Ensure local date is used to avoid timezone shift to previous day
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      (userToRegister as any).birthDate = `${year}-${month}-${day}`;
    }

    this.authService.register(userToRegister).subscribe({
      next: () => {
        this.message = 'COMMON.SUCCESS';
        this.error = '';
        this.cdr.detectChanges();
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        if (err.status === 400 && typeof err.error === 'string') {
          // Backend returns specific error messages as strings
          this.error = err.error;
          // If the backend error message matches a known key or is specific,
          // we might need to handle it. For now, we display what backend says if it's not a translation key.
          // But to be safe with i18n, we should probably map common ones.
          if (this.error === 'User already exists') {
            // We could add a translation key for this too
            this.error = 'ADMIN.ERROR_USER_EXISTS';
          }
        } else {
          this.error = 'COMMON.ERROR';
        }
        this.message = '';
        this.cdr.detectChanges();
      }
    });
  }
}
