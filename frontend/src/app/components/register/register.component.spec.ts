import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';
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

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: any;
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
      register: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, FormsModule, TranslateModule.forRoot()],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        provideRouter([]),
        provideNoopAnimations()
      ]
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

  it('should register and navigate to login', async () => {
    vi.useFakeTimers();
    authServiceSpy.register.mockReturnValue(of({} as User));

    component.user = {
      firstName: 'New',
      lastName: 'User',
      login: 'newuser',
      password: 'password',
      email: 'new@example.com',
      birthDate: '',
      address: ''
    };
    component.confirmPassword = 'password';

    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalled();
    expect(component.message).toContain('COMMON.SUCCESS');

    vi.advanceTimersByTime(2000);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
    vi.useRealTimers();
  });

  it('should not register if passwords do not match', () => {
    component.user.password = 'password';
    component.confirmPassword = 'wrong-password';

    component.onSubmit();

    expect(authServiceSpy.register).not.toHaveBeenCalled();
    expect(component.error).toBe('ADMIN.ERROR_PASSWORD_MATCH');
  });

  it('should show error on registration failure', () => {
    authServiceSpy.register.mockReturnValue(throwError(() => ({ error: 'User already exists' })));
    component.user.password = 'password';
    component.confirmPassword = 'password';

    component.onSubmit();

    expect(component.error).toBe('COMMON.ERROR');
  });
});
