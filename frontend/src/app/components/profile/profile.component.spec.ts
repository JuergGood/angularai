import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile.component';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { User } from '../../models/user.model';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';

import { NO_ERRORS_SCHEMA, signal } from '@angular/core';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let userServiceSpy: any;
  let authServiceSpy: any;
  let translateServiceSpy: any;
  let routerSpy: any;
  let snackBarSpy: any;
  let dialogSpy: any;
  let cdrSpy: any;

  const mockUser: User = {
    firstName: 'John',
    lastName: 'Doe',
    login: 'johndoe',
    email: 'john@example.com',
    birthDate: '1990-01-01',
    address: '123 St'
  } as User;

  beforeEach(() => {
    userServiceSpy = {
      getCurrentUser: vi.fn().mockReturnValue(of(mockUser)),
      updateCurrentUser: vi.fn().mockReturnValue(of(mockUser))
    };
    authServiceSpy = {
      logout: vi.fn(),
      currentUser: signal(mockUser),
      isInitializing: signal(false)
    };
    routerSpy = {
      navigate: vi.fn()
    };
    snackBarSpy = {
      open: vi.fn()
    };
    dialogSpy = {
      open: vi.fn()
    };
    cdrSpy = {
      detectChanges: vi.fn()
    };

    translateServiceSpy = {
      get: vi.fn().mockReturnValue(of('translated')),
      instant: vi.fn().mockReturnValue('translated'),
      stream: vi.fn().mockReturnValue(of('translated')),
      currentLang: 'en'
    };

    component = new ProfileComponent(
      userServiceSpy,
      authServiceSpy,
      routerSpy,
      cdrSpy,
      dialogSpy,
      snackBarSpy,
      translateServiceSpy
    );
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update profile and handle direct user response', () => {
    component.user = { ...mockUser };
    userServiceSpy.updateCurrentUser.mockReturnValue(of({ ...mockUser, firstName: 'Updated' }));

    component.onSubmit();

    expect(userServiceSpy.updateCurrentUser).toHaveBeenCalled();
    expect(component.user.firstName).toBe('Updated');
    expect(component.message).toBe('COMMON.SUCCESS');
  });

  it('should update profile and handle wrapper response with message', () => {
    component.user = { ...mockUser };
    const wrappedResponse = {
      message: 'Verification email sent',
      user: { ...mockUser, pendingEmail: 'new@example.com' }
    };
    userServiceSpy.updateCurrentUser.mockReturnValue(of(wrappedResponse));

    component.onSubmit();

    expect(userServiceSpy.updateCurrentUser).toHaveBeenCalled();
    expect(component.user.pendingEmail).toBe('new@example.com');
    expect(snackBarSpy.open).toHaveBeenCalled();
    expect(component.message).toBe('');
  });

  it('should handle error during update', () => {
    component.user = { ...mockUser };
    userServiceSpy.updateCurrentUser.mockReturnValue(throwError(() => ({ error: 'Error occurred' })));

    component.onSubmit();

    expect(component.message).toBe('Error occurred');
  });

  it('should logout and navigate to login', () => {
    component.onLogout();
    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should navigate to login on error loading user', () => {
    userServiceSpy.getCurrentUser.mockReturnValue(throwError(() => 'error'));
    component.ngOnInit();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });
});
