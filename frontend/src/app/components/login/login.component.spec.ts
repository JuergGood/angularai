import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { Router, provideRouter } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
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
      currentUser: vi.fn().mockReturnValue(null)
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
      imports: [LoginComponent, FormsModule, TranslateModule.forRoot()],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: TranslateService, useValue: translateServiceSpy },
        provideRouter([]),
        provideNoopAnimations(),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
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

  it('should navigate to profile on successful login', () => {
    authServiceSpy.login.mockReturnValue(of({} as User));
    component.login = 'test';
    component.password = 'pass';

    component.onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalledWith('test', 'pass');
    expect(router.navigate).toHaveBeenCalledWith(['/profile']);
  });

  it('should set error on failed login (401)', () => {
    authServiceSpy.login.mockReturnValue(throwError(() => ({ status: 401 })));

    component.onSubmit();

    expect(component.error).toBe('COMMON.ERROR');
  });

  it('should set generic error on server failure', () => {
    authServiceSpy.login.mockReturnValue(throwError(() => ({ status: 500, statusText: 'Internal Server Error' })));

    component.onSubmit();

    expect(component.error).toBe('COMMON.ERROR');
  });

  it('should toggle password visibility', () => {
    expect(component.hidePassword).toBe(true);

    const toggleBtn = fixture.nativeElement.querySelector('button[mat-icon-button]');
    toggleBtn.click();
    fixture.detectChanges();

    expect(component.hidePassword).toBe(false);
    expect(fixture.nativeElement.querySelector('input[name="password"]').type).toBe('text');

    toggleBtn.click();
    fixture.detectChanges();

    expect(component.hidePassword).toBe(true);
    expect(fixture.nativeElement.querySelector('input[name="password"]').type).toBe('password');
  });
});
