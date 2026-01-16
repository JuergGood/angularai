import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserAdminComponent } from './user-admin.component';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { User } from '../../models/user.model';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';

describe('UserAdminComponent', () => {
  let component: UserAdminComponent;
  let fixture: ComponentFixture<UserAdminComponent>;
  let adminServiceSpy: any;
  let authServiceSpy: any;
  let dialogSpy: any;
  let translateServiceSpy: any;

  const mockUsers: User[] = [
    { id: 1, login: 'admin', firstName: 'Admin', lastName: 'User', email: 'admin@example.com', role: 'ROLE_ADMIN', birthDate: '', address: '' },
    { id: 2, login: 'user', firstName: 'Normal', lastName: 'User', email: 'user@example.com', role: 'ROLE_USER', birthDate: '', address: '' }
  ];

  beforeEach(async () => {
    try {
      TestBed.initTestEnvironment(
        BrowserDynamicTestingModule,
        platformBrowserDynamicTesting()
      );
    } catch (e) {
      // already initialized
    }

    adminServiceSpy = {
      getUsers: vi.fn().mockReturnValue(of(mockUsers)),
      updateUser: vi.fn().mockReturnValue(of(mockUsers[1])),
      deleteUser: vi.fn().mockReturnValue(of(null))
    };

    authServiceSpy = {
      isAdmin: vi.fn().mockReturnValue(true),
      currentUser: vi.fn().mockReturnValue(mockUsers[0]),
      hasAdminWriteAccess: vi.fn().mockReturnValue(true)
    };

    dialogSpy = {
      open: vi.fn().mockReturnValue({
        afterClosed: () => of(true)
      })
    };

    translateServiceSpy = {
      get: vi.fn().mockReturnValue(of('translated')),
      onTranslationChange: of({}),
      onLangChange: of({}),
      onDefaultLangChange: of({})
    };

    await TestBed.configureTestingModule({
      imports: [UserAdminComponent, FormsModule, TranslateModule.forRoot()],
      providers: [
        provideNoopAnimations(),
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MatDialog, useValue: dialogSpy },
        { provide: TranslateService, useValue: translateServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(adminServiceSpy.getUsers).toHaveBeenCalled();
  });

  it('should load users on init if admin', () => {
    expect(component.users.length).toBe(2);
  });

  it('should not load users if not admin', () => {
    authServiceSpy.isAdmin.mockReturnValue(false);
    // Re-run init to test logic
    adminServiceSpy.getUsers.mockClear();
    component.ngOnInit();
    expect(adminServiceSpy.getUsers).not.toHaveBeenCalled();
  });

  it('should start editing a user', () => {
    component.editUser(mockUsers[1]);
    expect(component.editingUser?.login).toBe('user');
  });

  it('should save user changes', () => {
    component.editUser(mockUsers[1]);
    component.editingUser!.firstName = 'Updated';
    component.saveUser();
    expect(adminServiceSpy.updateUser).toHaveBeenCalledWith(2, vi.any(Object));
    expect(component.editingUser).toBeNull();
  });

  it('should delete a user after confirmation', () => {
    component.deleteUser(mockUsers[1]);
    expect(dialogSpy.open).toHaveBeenCalled();
    expect(adminServiceSpy.deleteUser).toHaveBeenCalledWith(2);
  });
});
