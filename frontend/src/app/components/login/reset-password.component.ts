import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, Validators, ReactiveFormsModule, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    TranslateModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule
  ],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css'
})
export class ResetPasswordComponent implements OnInit {
  resetForm: FormGroup;
  token = '';
  loading = false;
  success = false;
  error = '';
  hidePassword = true;
  hideConfirmPassword = true;

  strengthValue = 0;
  strengthColor: 'warn' | 'accent' | 'primary' = 'warn';
  strengthLabel = 'ADMIN.STRENGTH.WEAK';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {
    this.resetForm = this.fb.group({
      password: ['', [Validators.required, this.passwordStrengthValidator()]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!this.token) {
      this.error = 'Invalid or missing token';
    }

    this.resetForm.get('password')?.valueChanges.subscribe(val => this.onPasswordChange(val));
  }

  passwordStrengthValidator() {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      if (!value) return null;
      const hasLetter = /[a-zA-Z]/.test(value);
      const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(value);
      const isLongEnough = value.length >= 8;
      const valid = hasLetter && hasSpecial && isLongEnough;
      return !valid ? { strength: true } : null;
    };
  }

  passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { mismatch: true };
  }

  onPasswordChange(pwd: string) {
    if (!pwd) {
      this.strengthValue = 0;
      this.strengthLabel = 'ADMIN.STRENGTH.WEAK';
      this.strengthColor = 'warn';
      return;
    }

    let strength = 0;
    if (pwd.length >= 8) strength += 33;
    if (/[a-zA-Z]/.test(pwd)) strength += 33;
    if (/[!@#$%^&*(),.?":{}|<>]/.test(pwd)) strength += 34;

    this.strengthValue = strength;
    if (strength <= 33) {
      this.strengthLabel = 'ADMIN.STRENGTH.WEAK';
      this.strengthColor = 'warn';
    } else if (strength <= 66) {
      this.strengthLabel = 'ADMIN.STRENGTH.MEDIUM';
      this.strengthColor = 'accent';
    } else {
      this.strengthLabel = 'ADMIN.STRENGTH.STRONG';
      this.strengthColor = 'primary';
    }
  }

  onSubmit() {
    if (this.resetForm.invalid || !this.token) return;

    this.loading = true;
    this.error = '';

    const payload = {
      token: this.token,
      password: this.resetForm.get('password')?.value
    };

    this.authService.resetPassword(payload).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        if (err.error && err.error.error === 'expired') {
          this.error = 'Link has expired. Please request a new one.';
        } else {
          this.error = 'Invalid token or server error. Please try again.';
        }
      }
    });
  }
}
