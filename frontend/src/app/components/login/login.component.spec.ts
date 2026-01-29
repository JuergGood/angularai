import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { Router, provideRouter } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, throwError, Observable } from 'rxjs';
import { User } from '../../models/user.model';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';

import { NO_ERRORS_SCHEMA, signal } from '@angular/core';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: any;
  let translateServiceSpy: any;
  let router: Router;

  beforeEach(async () => {
    try {
      TestBed.initTestEnvironment(
        BrowserDynamicTestingModule,
        platformBrowserDynamicTesting()
      );
    } catch (e) {
      // already initialized
    }

    authServiceSpy = {
      login: vi.fn(),
      isLoggedIn: vi.fn().mockReturnValue(false),
      currentUser: signal(null),
      isInitializing: signal(false)
    };

    translateServiceSpy = {
      get: vi.fn().mockReturnValue(of('translated')),
      onTranslationChange: of({}),
      onLangChange: of({}),
      onDefaultLangChange: of({}),
      instant: vi.fn().mockReturnValue('translated'),
      stream: vi.fn().mockReturnValue(of('translated')),
      get currentLang() { return 'en'; },
      getCurrentLang: vi.fn().mockReturnValue('en'),
      getFallbackLang: vi.fn().mockReturnValue('en'),
      getTranslation: vi.fn().mockReturnValue(of({}))
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, FormsModule, TranslateModule.forRoot()],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        provideRouter([]),
        provideNoopAnimations(),
        provideHttpClient(),
        provideHttpClientTesting()
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate on successful login', () => {
    const user = { role: 'ROLE_ADMIN' } as any;
    authServiceSpy.login.mockReturnValue(of(user));
    component.login = 'admin';
    component.password = 'password';
    component.onSubmit();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should set error on failed login (401)', () => {
    authServiceSpy.login.mockReturnValue(throwError(() => ({ status: 401 })));
    component.onSubmit();
    expect(component.error).toBe('COMMON.ERROR_LOGIN_FAILED');
  });

  it('should set specific error for non-active user (403)', () => {
    authServiceSpy.login.mockReturnValue(throwError(() => ({ status: 403 })));
    component.onSubmit();
    expect(component.error).toBe('ADMIN.ERROR_USER_NOT_ACTIVE');
  });

  it('should toggle password visibility', () => {
    expect(component.hidePassword).toBe(true);
    component.hidePassword = false;
    expect(component.hidePassword).toBe(false);
  });
});
