import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './login.component.html',
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
      justify-content: space-between;
      align-items: center;
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
  hidePassword = true;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  onSubmit() {
    this.authService.login(this.login, this.password).subscribe({
      next: () => {
        this.router.navigate(['/tasks']);
      },
      error: (err) => {
        if (err.status === 401) {
          this.error = 'Invalid login or password';
        } else {
          this.error = `An error occurred: ${err.statusText || 'Server unreachable'}`;
        }
        this.cdr.detectChanges();
      }
    });
  }
}
