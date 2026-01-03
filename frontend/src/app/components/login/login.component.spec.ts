import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { Router, provideRouter } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { User } from '../../models/user.model';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: any;
  let routerSpy: any;

  beforeEach(async () => {
    authServiceSpy = {
      login: vi.fn()
    };
    routerSpy = {
      navigate: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent, FormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        provideRouter([])
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    routerSpy = TestBed.inject(Router);
    vi.spyOn(routerSpy, 'navigate');
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
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/profile']);
  });

  it('should set error on failed login (401)', () => {
    authServiceSpy.login.mockReturnValue(throwError(() => ({ status: 401 })));

    component.onSubmit();

    expect(component.error).toBe('Invalid login or password');
  });

  it('should set generic error on server failure', () => {
    authServiceSpy.login.mockReturnValue(throwError(() => ({ status: 500, statusText: 'Internal Server Error' })));

    component.onSubmit();

    expect(component.error).toBe('An error occurred: Internal Server Error');
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
