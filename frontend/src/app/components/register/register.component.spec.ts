import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';
import { Router, provideRouter } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { User } from '../../models/user.model';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: any;
  let routerSpy: any;

  beforeEach(async () => {
    authServiceSpy = {
      register: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, FormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        provideRouter([]),
        provideNoopAnimations()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    routerSpy = TestBed.inject(Router);
    vi.spyOn(routerSpy, 'navigate');
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
    expect(component.message).toContain('Registration successful');

    vi.advanceTimersByTime(2000);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
    vi.useRealTimers();
  });

  it('should not register if passwords do not match', () => {
    component.user.password = 'password';
    component.confirmPassword = 'wrong-password';

    component.onSubmit();

    expect(authServiceSpy.register).not.toHaveBeenCalled();
    expect(component.error).toBe('Passwords do not match');
  });

  it('should show error on registration failure', () => {
    authServiceSpy.register.mockReturnValue(throwError(() => ({ error: 'User already exists' })));
    component.user.password = 'password';
    component.confirmPassword = 'password';

    component.onSubmit();

    expect(component.error).toBe('User already exists');
  });
});
