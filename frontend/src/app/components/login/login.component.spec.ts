import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
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
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
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

  it('should set error on failed login', () => {
    authServiceSpy.login.mockReturnValue(throwError(() => new Error('Failed')));

    component.onSubmit();

    expect(component.error).toBe('Invalid login or password');
  });
});
