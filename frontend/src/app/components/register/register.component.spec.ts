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
