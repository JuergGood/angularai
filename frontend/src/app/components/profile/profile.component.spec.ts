import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile.component';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
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

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let userServiceSpy: any;
  let authServiceSpy: any;
  let routerSpy: any;

  const mockUser: User = {
    firstName: 'John',
    lastName: 'Doe',
    login: 'johndoe',
    email: 'john@example.com',
    birthDate: '1990-01-01',
    address: '123 St'
  } as User;

  beforeEach(async () => {
    try {
      TestBed.initTestEnvironment(
        BrowserDynamicTestingModule,
        platformBrowserDynamicTesting()
      );
    } catch (e) {
      // already initialized
    }

    userServiceSpy = {
      getCurrentUser: vi.fn().mockReturnValue(of(mockUser)),
      updateCurrentUser: vi.fn().mockReturnValue(of(mockUser))
    };
    authServiceSpy = {
      logout: vi.fn()
    };
    routerSpy = {
      navigate: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [ProfileComponent, FormsModule, TranslateModule.forRoot()],
      providers: [
        { provide: UserService, useValue: userServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        provideNoopAnimations()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(userServiceSpy.getCurrentUser).toHaveBeenCalled();
  });

  it('should update profile', () => {
    component.user = { ...mockUser, firstName: 'Updated' };
    component.onSubmit();

    expect(userServiceSpy.updateCurrentUser).toHaveBeenCalledWith(component.user);
    expect(component.message).toBe('COMMON.SUCCESS');
  });

  it('should logout and navigate to login', () => {
    component.onLogout();
    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should navigate to login on error loading user', () => {
    userServiceSpy.getCurrentUser.mockReturnValue(throwError(() => new Error('Not auth')));
    component.ngOnInit();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });
});
