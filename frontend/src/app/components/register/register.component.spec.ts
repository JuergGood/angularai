import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';
import { SystemService } from '../../services/system.service';
import { Router, provideRouter } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { User } from '../../models/user.model';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { TranslateModule } from '@ngx-translate/core';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';

import { NO_ERRORS_SCHEMA, signal } from '@angular/core';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: any;
  let systemServiceSpy: any;
  let translateServiceSpy: any;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = {
      register: vi.fn().mockReturnValue(of({}))
    };

    systemServiceSpy = {
      getRecaptchaSiteKey: vi.fn().mockReturnValue(of('disabled'))
    };

    translateServiceSpy = {
      get: vi.fn().mockReturnValue(of('translated')),
      onTranslationChange: of({}),
      onLangChange: of({}),
      onDefaultLangChange: of({}),
      instant: vi.fn().mockReturnValue('translated'),
      stream: vi.fn().mockReturnValue(of('translated')),
      get currentLang() { return 'en'; }
    };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, ReactiveFormsModule, TranslateModule.forRoot()],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: SystemService, useValue: systemServiceSpy },
        provideRouter([]),
        provideNoopAnimations()
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should register and navigate to success when recaptcha is disabled', async () => {
    component.registerForm.patchValue({
      fullName: 'John Doe',
      login: 'johndoe',
      password: 'Password@123',
      confirmPassword: 'Password@123',
      email: 'john@example.com',
      address: 'Test Address'
    });
    component.recaptchaMode = 'disabled';

    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalledWith(expect.objectContaining({
      firstName: 'John',
      lastName: 'Doe'
    }));
    expect(router.navigate).toHaveBeenCalledWith(['/register/success'], expect.anything());
  });

  it('should validate full name format only after blur', () => {
    const fullNameControl = component.registerForm.get('fullName');

    // Single word
    fullNameControl?.setValue('John');
    // Validation runs on blur (markAsTouched)
    fullNameControl?.markAsTouched();
    fullNameControl?.updateValueAndValidity();
    expect(fullNameControl?.hasError('nameFormat')).toBe(true);

    // Two words
    fullNameControl?.setValue('John Doe');
    fullNameControl?.updateValueAndValidity();
    expect(fullNameControl?.hasError('nameFormat')).toBe(false);
  });

  it('should parse multiple first names and last name correctly', () => {
    component.registerForm.patchValue({
      fullName: 'Hans Peter   Müller',
      login: 'hpm',
      password: 'Password@123',
      confirmPassword: 'Password@123',
      email: 'hpm@example.com'
    });
    component.recaptchaMode = 'disabled';
    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalledWith(expect.objectContaining({
      firstName: 'Hans Peter',
      lastName: 'Müller'
    }));
  });

  it('should not register if name is only one word', () => {
    component.registerForm.patchValue({
      fullName: 'John',
      login: 'john',
      password: 'Password@123',
      confirmPassword: 'Password@123',
      email: 'john@example.com'
    });
    component.recaptchaMode = 'disabled';
    component.onSubmit();

    expect(authServiceSpy.register).not.toHaveBeenCalled();
    expect(component.registerForm.get('fullName')?.hasError('nameFormat')).toBe(true);
  });

  it('should validate login has no spaces only after blur', () => {
    const loginControl = component.registerForm.get('login');

    // With space
    loginControl?.setValue('john doe');
    // Blur
    loginControl?.markAsTouched();
    loginControl?.updateValueAndValidity();
    expect(loginControl?.hasError('noSpaces')).toBe(true);

    // Without space
    loginControl?.setValue('johndoe');
    loginControl?.updateValueAndValidity();
    expect(loginControl?.hasError('noSpaces')).toBe(false);
  });

  it('should not register if passwords do not match', () => {
    component.registerForm.patchValue({
      password: 'Password@123',
      confirmPassword: 'MismatchPassword'
    });
    component.onSubmit();
    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('should not register if recaptcha is required but not completed', () => {
    component.recaptchaMode = 'visible';
    component.registerForm.patchValue({
      fullName: 'John Doe',
      login: 'johndoe',
      password: 'Password@123',
      confirmPassword: 'Password@123',
      email: 'john@example.com'
    });

    component.onSubmit();

    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('should not show recaptcha error until form is valid', async () => {
    component.recaptchaMode = 'visible';
    component.recaptchaStatus = 'idle';
    component.registerForm.patchValue({
      fullName: '',
      login: '',
      email: '',
      password: 'Password@123',
      confirmPassword: 'Password@123'
    });

    fixture.detectChanges();
    await fixture.whenStable();

    const errorElement = fixture.nativeElement.querySelector('.recaptcha-wrapper mat-error');
    // In reactive forms version, we don't show the same inline error anymore, it's blocked by button disabled
    expect(errorElement).toBeFalsy();
  });

  it('should show error on registration failure', () => {
    authServiceSpy.register.mockReturnValue(throwError(() => ({ status: 400, error: 'User already exists' })));
    component.registerForm.patchValue({
      fullName: 'John Doe',
      login: 'johndoe',
      password: 'Password@123',
      confirmPassword: 'Password@123',
      email: 'john@example.com'
    });
    component.recaptchaMode = 'disabled';

    component.onSubmit();

    expect(component.error).toBe('ADMIN.ERROR_USER_EXISTS');
  });

  it('should show error when email already exists', () => {
    authServiceSpy.register.mockReturnValue(throwError(() => ({ status: 400, error: 'Email already exists' })));
    component.registerForm.patchValue({
      fullName: 'John Doe',
      login: 'johndoe',
      password: 'Password@123',
      confirmPassword: 'Password@123',
      email: 'john@example.com'
    });
    component.recaptchaMode = 'disabled';

    component.onSubmit();

    expect(component.error).toBe('ADMIN.ERROR_EMAIL_EXISTS');
  });

  it('should toggle password visibility', () => {
    expect(component.passwordVisible).toBe(false);
    component.passwordVisible = true;
    expect(component.passwordVisible).toBe(true);
  });

  it('should toggle confirm password visibility', () => {
    expect(component.confirmPasswordVisible).toBe(false);
    component.confirmPasswordVisible = true;
    expect(component.confirmPasswordVisible).toBe(true);
  });

  it('should show error when full name is missing', () => {
    const fullNameControl = component.registerForm.get('fullName');
    fullNameControl?.setValue('');
    fullNameControl?.markAsTouched();
    expect(fullNameControl?.hasError('required')).toBe(true);
  });

  it('should handle reCAPTCHA score mode correctly', async () => {
    component.recaptchaSiteKey = '6Lfik-dummy';
    component.recaptchaMode = 'score';
    component.registerForm.patchValue({
      fullName: 'John Doe',
      login: 'johndoe',
      password: 'Password@123',
      confirmPassword: 'Password@123',
      email: 'john@example.com'
    });

    const executeSpy = vi.fn().mockResolvedValue('score-token');
    (window as any).grecaptcha = {
      enterprise: {
        ready: (cb: any) => cb(),
        execute: executeSpy
      }
    };

    component.onSubmit();

    // Wait for the async reCAPTCHA ready block
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(component.recaptchaStatus).toBe('verified');
    expect(authServiceSpy.register).toHaveBeenCalledWith(expect.objectContaining({
      recaptchaToken: 'score-token'
    }));
  });

  it('should handle reCAPTCHA score mode error', async () => {
    component.recaptchaSiteKey = '6Lfik-dummy';
    component.recaptchaMode = 'score';
    component.registerForm.patchValue({
      fullName: 'John Doe',
      login: 'johndoe',
      password: 'Password@123',
      confirmPassword: 'Password@123',
      email: 'john@example.com'
    });

    (window as any).grecaptcha = {
      enterprise: {
        ready: (cb: any) => cb(),
        execute: vi.fn().mockRejectedValue('error')
      }
    };

    component.onSubmit();

    // Wait for the async reCAPTCHA ready block
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(component.recaptchaStatus).toBe('error');
    expect(component.error).toBe('REGISTER.RECAPTCHA_FAILED');
    expect(component.isSubmitting).toBe(false);
  });

  it('should handle reCAPTCHA visible mode missing token', () => {
    component.recaptchaMode = 'visible';
    component.registerForm.patchValue({
      fullName: 'John Doe',
      login: 'johndoe',
      password: 'Password@123',
      confirmPassword: 'Password@123',
      email: 'john@example.com'
    });

    (window as any).grecaptcha = {
      enterprise: {
        getResponse: () => ''
      }
    };

    component.onSubmit();

    expect(component.error).toBe('ADMIN.ERROR_RECAPTCHA');
    expect(component.isSubmitting).toBe(false);
  });

  it('should handle reCAPTCHA expired callback', () => {
    component.recaptchaMode = 'visible';
    component.scriptLoaded = true;
    const container = document.createElement('div');
    container.id = 'recaptcha-container';
    document.body.appendChild(container);

    let expiredCallback: any;
    (window as any).grecaptcha = {
      enterprise: {
        render: (id: string, options: any) => {
          expiredCallback = options['expired-callback'];
        }
      }
    };

    component.renderRecaptcha();
    expiredCallback();

    expect(component.recaptchaStatus).toBe('expired');
    expect(component.registerForm.get('recaptchaToken')?.value).toBe('');

    document.body.removeChild(container);
  });

  it('should handle reCAPTCHA error callback', () => {
    component.recaptchaMode = 'visible';
    component.scriptLoaded = true;
    const container = document.createElement('div');
    container.id = 'recaptcha-container';
    document.body.appendChild(container);

    let errorCallback: any;
    (window as any).grecaptcha = {
      enterprise: {
        render: (id: string, options: any) => {
          errorCallback = options['error-callback'];
        }
      }
    };

    component.renderRecaptcha();
    errorCallback();

    expect(component.recaptchaStatus).toBe('error');

    document.body.removeChild(container);
  });

  it('should handle bypass recaptcha window flag', () => {
    (window as any).BYPASS_RECAPTCHA = true;
    component.registerForm.patchValue({
      fullName: 'John Doe',
      login: 'johndoe',
      password: 'Password@123',
      confirmPassword: 'Password@123',
      email: 'john@example.com'
    });

    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalledWith(expect.objectContaining({
      recaptchaToken: 'bypass-token'
    }));
    delete (window as any).BYPASS_RECAPTCHA;
  });

  it('should show generic error on registration failure', () => {
    authServiceSpy.register.mockReturnValue(throwError(() => ({ status: 500 })));
    component.registerForm.patchValue({
      fullName: 'John Doe',
      login: 'johndoe',
      password: 'Password@123',
      confirmPassword: 'Password@123',
      email: 'john@example.com'
    });
    component.recaptchaMode = 'disabled';

    component.onSubmit();

    expect(component.error).toBe('COMMON.ERROR');
  });

  it('should validate password mismatch', () => {
    component.registerForm.patchValue({
      password: 'Password@123',
      confirmPassword: 'DifferentPassword@123'
    });
    component.registerForm.get('confirmPassword')?.markAsTouched();
    component.registerForm.updateValueAndValidity();

    expect(component.registerForm.hasError('passwordMismatch')).toBe(true);
    expect(component.registerForm.get('confirmPassword')?.hasError('passwordMismatch')).toBe(true);
  });

  it('should handle register form reset on init', () => {
    const resetSpy = vi.spyOn(component.registerForm, 'reset');
    component.ngOnInit();
    expect(resetSpy).toHaveBeenCalled();
  });

  it('should setup recaptcha script if not present', () => {
    const existingScript = document.getElementById('recaptcha-script');
    if (existingScript) existingScript.remove();

    component.setupRecaptcha();
    const script = document.getElementById('recaptcha-script') as HTMLScriptElement;
    expect(script).toBeTruthy();
    expect(script.src).toContain('recaptcha/enterprise.js');
  });

  it('should validate login format (no spaces)', () => {
    const loginControl = component.registerForm.get('login');
    loginControl?.setValue('john doe');
    loginControl?.markAsTouched();
    expect(loginControl?.hasError('noSpaces')).toBe(true);

    loginControl?.setValue('johndoe');
    expect(loginControl?.hasError('noSpaces')).toBe(false);
  });

  it('should validate email format', () => {
    const emailControl = component.registerForm.get('email');
    emailControl?.setValue('invalid-email');
    emailControl?.markAsTouched();
    expect(emailControl?.hasError('email')).toBe(true);

    emailControl?.setValue('john@example.com');
    expect(emailControl?.hasError('email')).toBe(false);
  });

  it('should validate password match', () => {
    component.registerForm.patchValue({
      password: 'Password@123',
      confirmPassword: 'Different@123'
    });
    // Form level validator
    expect(component.registerForm.hasError('passwordMismatch')).toBe(true);
    // Control level error set by validator
    expect(component.registerForm.get('confirmPassword')?.hasError('passwordMismatch')).toBe(true);

    component.registerForm.patchValue({
      confirmPassword: 'Password@123'
    });
    expect(component.registerForm.hasError('passwordMismatch')).toBe(false);
    expect(component.registerForm.get('confirmPassword')?.hasError('passwordMismatch')).toBe(false);
  });

  it('should show error if reCAPTCHA fails', () => {
    authServiceSpy.register.mockReturnValue(throwError(() => ({ status: 400, error: 'reCAPTCHA verification failed' })));
    component.registerForm.patchValue({
      fullName: 'John Doe',
      login: 'johndoe',
      password: 'Password@123',
      confirmPassword: 'Password@123',
      email: 'john@example.com'
    });
    component.recaptchaMode = 'disabled';

    component.onSubmit();

    expect(component.error).toBe('ADMIN.ERROR_RECAPTCHA');
  });

  describe('password strength', () => {
    it('should be empty for empty password', () => {
      component.onPasswordChange('');
      expect(component.passwordStrength).toBe('');
    });

    it('should be weak for short simple password', () => {
      component.onPasswordChange('123');
      expect(component.passwordStrength).toBe('weak');
    });

    it('should be medium for moderate password', () => {
      component.onPasswordChange('Password123');
      expect(component.passwordStrength).toBe('medium');
    });

    it('should be strong for complex password', () => {
      component.onPasswordChange('ComplexPass@1234');
      expect(component.passwordStrength).toBe('strong');
    });
  });
});
