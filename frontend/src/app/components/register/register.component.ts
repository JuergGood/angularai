import { Component, ChangeDetectorRef, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SystemService } from '../../services/system.service';
import { TranslateModule } from '@ngx-translate/core';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
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
    TranslateModule
  ],
  templateUrl: './register.component.html',
  styles: [`
    .register-page {
      display: flex;
      justify-content: center;
      padding: 24px;
    }
    .register-card {
      width: 100%;
      max-width: 650px;
    }
    .form-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
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
    .error { color: #f44336; margin-top: 16px; text-align: center; }
    .success { color: #4caf50; margin-top: 16px; text-align: center; }
    .recaptcha-wrapper {
      margin-bottom: 24px;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    .recaptcha-wrapper mat-error {
      font-size: 12px;
      margin-top: 4px;
    }
  `]
})
export class RegisterComponent implements OnInit, AfterViewInit {
  user: User = {
    firstName: '',
    lastName: '',
    login: '',
    password: '',
    email: '',
    address: '',
    recaptchaToken: ''
  };
  confirmPassword = '';
  error = '';
  message = '';
  recaptchaSiteKey = '';
  private scriptLoaded = false;

  constructor(
    private authService: AuthService,
    private systemService: SystemService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.systemService.getRecaptchaSiteKey().subscribe(key => {
      this.recaptchaSiteKey = key;
      if (this.recaptchaSiteKey && this.recaptchaSiteKey !== 'disabled') {
        this.setupRecaptcha();
      }
    });
  }

  ngAfterViewInit() {
    // If key is already loaded (e.g. back navigation), try to render
    if (this.recaptchaSiteKey && this.recaptchaSiteKey !== 'disabled') {
      this.renderRecaptcha();
    }
  }

  setupRecaptcha() {
    if (document.getElementById('recaptcha-script')) {
      this.scriptLoaded = true;
      this.renderRecaptcha();
      return;
    }
    const script = document.createElement('script');
    script.id = 'recaptcha-script';
    script.src = `https://www.google.com/recaptcha/enterprise.js?onload=onRecaptchaLoad&render=explicit`;
    script.async = true;
    script.defer = true;
    document.head.appendChild(script);

    (window as any).onRecaptchaLoad = () => {
      this.scriptLoaded = true;
      this.renderRecaptcha();
    };
  }

  renderRecaptcha() {
    if (!this.recaptchaSiteKey || this.recaptchaSiteKey === 'disabled' || !this.scriptLoaded) {
      return;
    }

    const container = document.getElementById('recaptcha-container');
    if (!container) {
      return;
    }

    // Key starting with 6Lfik... is score-based according to user docs
    if (this.recaptchaSiteKey.startsWith('6Lfik')) {
      console.log('Detected Score-based reCAPTCHA Enterprise key - no render needed');
      return;
    }

    // Check if already rendered to avoid duplicates
    if (container.hasChildNodes()) {
      return;
    }

    try {
      (window as any).grecaptcha.enterprise.render('recaptcha-container', {
        'sitekey': this.recaptchaSiteKey,
        'callback': (token: string) => {
          this.user.recaptchaToken = token;
          this.cdr.detectChanges();
        },
        'expired-callback': () => {
          this.user.recaptchaToken = '';
          this.cdr.detectChanges();
        }
      });
    } catch (err) {
      console.warn('reCAPTCHA render failed (might be already rendering)', err);
    }
  }

  onSubmit() {
    if (this.user.password !== this.confirmPassword) {
      this.error = 'ADMIN.ERROR_PASSWORD_MATCH';
      return;
    }

    // Handle Score-based (Invisible) reCAPTCHA Enterprise
    if (this.recaptchaSiteKey && this.recaptchaSiteKey !== 'disabled' && this.recaptchaSiteKey.startsWith('6Lfik')) {
      (window as any).grecaptcha.enterprise.ready(async () => {
        try {
          const token = await (window as any).grecaptcha.enterprise.execute(this.recaptchaSiteKey, { action: 'REGISTER' });
          this.user.recaptchaToken = token;
          this.executeRegistration();
        } catch (err) {
          console.error('reCAPTCHA execution failed', err);
          this.error = 'ADMIN.ERROR_RECAPTCHA';
          this.cdr.detectChanges();
        }
      });
      return;
    }

    if (this.recaptchaSiteKey && this.recaptchaSiteKey !== 'disabled' && !this.user.recaptchaToken) {
      this.error = 'ADMIN.ERROR_RECAPTCHA';
      return;
    }

    this.executeRegistration();
  }

  executeRegistration() {
    const userToRegister = { ...this.user };
    if (userToRegister.birthDate && (userToRegister.birthDate as any) instanceof Date) {
      const date = userToRegister.birthDate as unknown as Date;
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
          this.error = err.error;
          if (this.error === 'User already exists') {
            this.error = 'ADMIN.ERROR_USER_EXISTS';
          } else if (this.error === 'reCAPTCHA verification failed') {
            this.error = 'ADMIN.ERROR_RECAPTCHA';
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
