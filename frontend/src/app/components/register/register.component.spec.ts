import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';
import { SystemService } from '../../services/system.service';
import { Router, provideRouter } from '@angular/router';
import { FormsModule } from '@angular/forms';
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
      imports: [RegisterComponent, FormsModule, TranslateModule.forRoot()],
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

  it('should register and navigate to login when recaptcha is disabled', async () => {
    component.user = {
      firstName: 'John',
      lastName: 'Doe',
      login: 'johndoe',
      password: 'Password@123',
      email: 'john@example.com',
      birthDate: '1990-01-01',
      address: 'Test Address'
    };
    component.confirmPassword = 'Password@123';
    component.recaptchaSiteKey = 'disabled';

    vi.useFakeTimers();
    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalled();

    vi.advanceTimersByTime(8000);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
    vi.useRealTimers();
  });

  it('should not register if passwords do not match', () => {
    component.user.password = 'pass1';
    component.confirmPassword = 'pass2';
    component.onSubmit();
    expect(component.error).toBe('ADMIN.ERROR_PASSWORD_MATCH');
    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('should not register if recaptcha is required but not completed', () => {
    component.recaptchaSiteKey = 'some-key';
    component.user.recaptchaToken = '';
    component.user.password = 'Password@123';
    component.confirmPassword = 'Password@123';

    component.onSubmit();

    expect(component.error).toBe('ADMIN.ERROR_RECAPTCHA');
    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('should not show recaptcha error until form is valid', async () => {
    component.recaptchaSiteKey = 'some-key';
    component.user.recaptchaToken = '';
    component.user.firstName = '';
    component.user.lastName = '';
    component.user.login = '';
    component.user.email = '';
    component.user.address = '';
    component.user.password = 'Password@123';
    component.confirmPassword = 'Password@123';

    fixture.detectChanges();
    await fixture.whenStable();

    const errorElement = fixture.nativeElement.querySelector('.recaptcha-wrapper mat-error');
    expect(errorElement).toBeFalsy();

    // Set values and wait for ngModel to update
    component.user.firstName = 'John';
    component.user.lastName = 'Doe';
    component.user.login = 'johndoe';
    component.user.email = 'john@example.com';
    component.user.address = 'Test Address';

    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const errorElementAfter = fixture.nativeElement.querySelector('.recaptcha-wrapper mat-error');
    expect(errorElementAfter).toBeTruthy();
    expect(errorElementAfter.textContent).toBeTruthy();
  });

  it('should hide recaptcha error when success message is present', async () => {
    component.recaptchaSiteKey = 'some-key';
    component.user.recaptchaToken = '';
    component.user.firstName = 'John';
    component.user.lastName = 'Doe';
    component.user.login = 'johndoe';
    component.user.email = 'john@example.com';
    component.user.address = 'Test Address';
    component.user.password = 'Password@123';
    component.confirmPassword = 'Password@123';
    component.message = 'Success';

    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const errorElement = fixture.nativeElement.querySelector('.recaptcha-wrapper mat-error');
    expect(errorElement).toBeFalsy();
  });

  it('should show error on registration failure', () => {
    authServiceSpy.register.mockReturnValue(throwError(() => ({ status: 400, error: 'User already exists' })));
    component.user.password = 'Password@123';
    component.confirmPassword = 'Password@123';
    component.recaptchaSiteKey = 'disabled';

    component.onSubmit();

    expect(component.error).toBe('ADMIN.ERROR_USER_EXISTS');
  });

  it('should show error when email already exists', () => {
    authServiceSpy.register.mockReturnValue(throwError(() => ({ status: 400, error: 'Email already exists' })));
    component.user.password = 'Password@123';
    component.confirmPassword = 'Password@123';
    component.recaptchaSiteKey = 'disabled';

    component.onSubmit();

    expect(component.error).toBe('ADMIN.ERROR_EMAIL_EXISTS');
  });

    describe('password strength', () => {
    it('should be empty for empty password', () => {
      component.user.password = '';
      component.onPasswordChange();
      expect(component.passwordStrength).toBe('');
    });

    it('should be weak for short simple password', () => {
      component.user.password = '123';
      component.onPasswordChange();
      expect(component.passwordStrength).toBe('weak');
    });

    it('should be medium for moderate password', () => {
      component.user.password = 'Password123'; // len 11, upper, lower, digit = score 4 (if len >= 8)
      // score: len>=8 (1), upper (1), lower (1), digit (1) = 4
      component.onPasswordChange();
      expect(component.passwordStrength).toBe('medium');
    });

    it('should be strong for complex password', () => {
      component.user.password = 'ComplexPass@1234';
      // score: len>=8 (1), len>=12 (1), upper (1), lower (1), digit (1), special (1) = 6
      component.onPasswordChange();
      expect(component.passwordStrength).toBe('strong');
    });
  });
});
