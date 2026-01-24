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
      password: 'password123',
      email: 'john@example.com',
      birthDate: '1990-01-01',
      address: 'Test Address'
    };
    component.confirmPassword = 'password123';
    component.recaptchaSiteKey = 'disabled';

    vi.useFakeTimers();
    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalled();

    vi.advanceTimersByTime(2000);
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
    component.user.password = 'password123';
    component.confirmPassword = 'password123';

    component.onSubmit();

    expect(component.error).toBe('ADMIN.ERROR_RECAPTCHA');
    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('should show error on registration failure', () => {
    authServiceSpy.register.mockReturnValue(throwError(() => ({ status: 400, error: 'User already exists' })));
    component.user.password = 'password123';
    component.confirmPassword = 'password123';
    component.recaptchaSiteKey = 'disabled';

    component.onSubmit();

    expect(component.error).toBe('ADMIN.ERROR_USER_EXISTS');
  });
});
