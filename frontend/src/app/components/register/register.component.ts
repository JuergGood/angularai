import { Component, ChangeDetectorRef, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SystemService } from '../../services/system.service';
import { TranslateModule } from '@ngx-translate/core';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    TranslateModule
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit, AfterViewInit {
  // CR-REG-02: reactive form + validators
  registerForm: FormGroup;

  // CR-REG-03: password UX visibility
  passwordVisible = false;
  confirmPasswordVisible = false;

  // CR-REG-04: reCAPTCHA state machine
  recaptchaMode: 'disabled' | 'score' | 'visible' = 'disabled';
  recaptchaStatus: 'idle' | 'verifying' | 'verified' | 'expired' | 'error' = 'idle';

  // CR-REG-06: submit/loading state
  isSubmitting = false;
  submitted = false;

  error = '';
  recaptchaSiteKey = '';
  passwordStrength: 'weak' | 'medium' | 'strong' | '' = '';
  private scriptLoaded = false;

  showError(controlName: string): boolean {
    const control = this.registerForm.get(controlName);
    const isMismatch = controlName === 'confirmPassword' &&
      (this.registerForm.errors?.['passwordMismatch'] || control?.hasError('passwordMismatch'));

    return !!(control && (control.invalid || isMismatch) && (control.touched || this.submitted));
  }

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private systemService: SystemService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {
    this.registerForm = this.fb.group({
      fullName: ['', { validators: [Validators.required, this.nameFormatValidator()], updateOn: 'blur' }],
      login: ['', { validators: [Validators.required, this.noSpacesValidator()], updateOn: 'blur' }],
      email: ['', { validators: [Validators.required, Validators.email], updateOn: 'blur' }],
      password: ['', { validators: [Validators.required, this.passwordStrengthValidator()], updateOn: 'blur' }],
      confirmPassword: ['', { validators: [Validators.required], updateOn: 'blur' }],
      address: ['']
    }, { validators: this.passwordMatchValidator });
  }

  noSpacesValidator() {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      const hasSpaces = /\s/.test(control.value);
      return hasSpaces ? { noSpaces: true } : null;
    };
  }

  passwordStrengthValidator() {
    // CR-REG-02: centralize validators
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      if (!value) return null;
      const passwordRegex = /^(?=.*[A-Za-z])(?=.*[^A-Za-z0-9]).{8,}$/;
      const isInvalid = !passwordRegex.test(value);

      // Update strength immediately on typing via template (input) event
      // This validator runs on blur due to updateOn: 'blur'
      return isInvalid ? { passwordStrength: true } : null;
    };
  }

  nameFormatValidator() {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      const name = control.value.trim();
      const parts = name.split(/\s+/);
      return parts.length >= 2 ? null : { nameFormat: true };
    };
  }

  passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
    // CR-REG-02: confirm match as form error
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    const confirmControl = group.get('confirmPassword');

    // Logic: mismatch shows only after confirm has value + blur, or after submit
    // updateOn: 'blur' on confirmControl handles the blur part.
    if (!confirmPassword) {
      if (confirmControl) {
         const currentErrors = confirmControl.errors;
         if (currentErrors) {
           delete currentErrors['passwordMismatch'];
           confirmControl.setErrors(Object.keys(currentErrors).length > 0 ? currentErrors : null);
         }
      }
      return null;
    }

    const mismatch = password === confirmPassword ? null : { passwordMismatch: true };

    // Also set error on the confirmPassword control so mat-form-field marks it as invalid
    if (confirmControl) {
      const currentErrors = confirmControl.errors;
      if (mismatch) {
        confirmControl.setErrors({ ...currentErrors, passwordMismatch: true });
      } else if (currentErrors) {
        delete currentErrors['passwordMismatch'];
        confirmControl.setErrors(Object.keys(currentErrors).length > 0 ? currentErrors : null);
      }
    }

    return mismatch;
  }

  ngOnInit() {
    this.registerForm.reset(); // Clear any pre-filled state on navigation
    this.systemService.getRecaptchaSiteKey().subscribe(key => {
      this.recaptchaSiteKey = key;
      if (!key || key === 'disabled') {
        this.recaptchaMode = 'disabled';
      } else if (key.startsWith('6Lfik')) {
        this.recaptchaMode = 'score';
      } else {
        this.recaptchaMode = 'visible';
      }

      if (this.recaptchaMode !== 'disabled') {
        this.setupRecaptcha();
      }
    });

    // CR-REG-03: strength indicator logic
    this.registerForm.get('password')?.valueChanges.subscribe(pwd => {
      this.onPasswordChange(pwd);
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
    if (this.recaptchaMode !== 'visible' || !this.scriptLoaded) {
      return;
    }

    const container = document.getElementById('recaptcha-container');
    if (!container || container.hasChildNodes()) {
      return;
    }

    try {
      (window as any).grecaptcha.enterprise.render('recaptcha-container', {
        'sitekey': this.recaptchaSiteKey,
        'callback': (token: string) => {
          this.recaptchaStatus = 'verified';
          this.registerForm.patchValue({ recaptchaToken: token });
          this.cdr.detectChanges();
        },
        'expired-callback': () => {
          this.recaptchaStatus = 'expired';
          this.registerForm.patchValue({ recaptchaToken: '' });
          this.cdr.detectChanges();
        },
        'error-callback': () => {
          this.recaptchaStatus = 'error';
          this.cdr.detectChanges();
        }
      });
    } catch (err) {
      console.warn('reCAPTCHA render failed (might be already rendering)', err);
    }
  }

  onPasswordChange(pwd: string) {
    if (!pwd) {
      this.passwordStrength = '';
      return;
    }

    let score = 0;
    if (pwd.length >= 8) score++;
    if (pwd.length >= 12) score++;
    if (/[A-Z]/.test(pwd)) score++;
    if (/[a-z]/.test(pwd)) score++;
    if (/[0-9]/.test(pwd)) score++;
    if (/[^A-Za-z0-9]/.test(pwd)) score++;

    if (score <= 3) {
      this.passwordStrength = 'weak';
    } else if (score <= 5) {
      this.passwordStrength = 'medium';
    } else {
      this.passwordStrength = 'strong';
    }
  }

  onSubmit() {
    this.submitted = true;
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    if (this.isSubmitting) {
      return;
    }

    // Double-submit prevention: immediately set isSubmitting to true
    this.isSubmitting = true;
    this.error = '';

    // Handle Score-based (Invisible) reCAPTCHA Enterprise
    if (this.recaptchaMode === 'score' && !(window as any).BYPASS_RECAPTCHA) {
      this.recaptchaStatus = 'verifying';
      (window as any).grecaptcha.enterprise.ready(async () => {
        try {
          const token = await (window as any).grecaptcha.enterprise.execute(this.recaptchaSiteKey, { action: 'REGISTER' });
          this.recaptchaStatus = 'verified';
          this.executeRegistration(token);
        } catch (err) {
          console.error('reCAPTCHA execution failed', err);
          this.recaptchaStatus = 'error';
          this.error = 'REGISTER.RECAPTCHA_FAILED';
          this.isSubmitting = false;
          this.cdr.detectChanges();
        }
      });
      return;
    }

    let token = '';
    if ((window as any).BYPASS_RECAPTCHA) {
      token = 'bypass-token';
    } else if (this.recaptchaMode === 'visible') {
      try {
        token = (window as any).grecaptcha?.enterprise?.getResponse();
      } catch (err) {
        console.warn('Failed to get reCAPTCHA response', err);
      }

      if (!token) {
        this.error = 'ADMIN.ERROR_RECAPTCHA';
        this.isSubmitting = false; // Reset if token is missing
        return;
      }
    }

    this.executeRegistration(token);
  }

  executeRegistration(token: string) {
    // isSubmitting is already true from onSubmit()

    // Parse fullName into firstName and lastName
    const fullNameValue = (this.registerForm.value.fullName || '').trim();
    const parts = fullNameValue.split(/\s+/);
    let firstName = '';
    let lastName = '';

    if (parts.length > 1) {
      lastName = parts.pop() || '';
      firstName = parts.join(' ');
    } else {
      firstName = parts[0] || '';
      lastName = '';
    }

    const { fullName: _, ...otherValues } = this.registerForm.value;
    const userToRegister = {
      ...otherValues,
      firstName,
      lastName,
      recaptchaToken: token
    };

    this.authService.register(userToRegister).subscribe({
      next: () => {
        this.isSubmitting = false;
        // CR-REG-01: navigate to success
        this.router.navigate(['/register/success'], { state: { email: this.registerForm.value.email } });
      },
      error: (err) => {
        this.isSubmitting = false;
        console.error('Registration failed:', err);

        if (err.status === 400 && typeof err.error === 'string') {
          this.error = err.error;
          if (this.error === 'User already exists') {
            this.error = 'ADMIN.ERROR_USER_EXISTS';
          } else if (this.error === 'Email already exists') {
            this.error = 'ADMIN.ERROR_EMAIL_EXISTS';
          } else if (this.error === 'reCAPTCHA verification failed') {
            this.error = 'ADMIN.ERROR_RECAPTCHA';
          }
        } else {
          this.error = 'COMMON.ERROR';
        }
        this.cdr.detectChanges();
      }
    });
  }
}
