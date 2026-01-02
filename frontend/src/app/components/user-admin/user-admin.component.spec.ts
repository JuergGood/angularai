import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserAdminComponent } from './user-admin.component';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { User } from '../../models/user.model';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';

describe('UserAdminComponent', () => {
  let component: UserAdminComponent;
  let fixture: ComponentFixture<UserAdminComponent>;
  let adminServiceSpy: any;
  let authServiceSpy: any;
  let dialogSpy: any;

  const mockUsers: User[] = [
    { id: 1, login: 'admin', firstName: 'Admin', lastName: 'User', email: 'admin@example.com', role: 'ROLE_ADMIN', birthDate: '', address: '' },
    { id: 2, login: 'user', firstName: 'Normal', lastName: 'User', email: 'user@example.com', role: 'ROLE_USER', birthDate: '', address: '' }
  ];

  beforeEach(async () => {
    adminServiceSpy = {
      getUsers: vi.fn().mockReturnValue(of(mockUsers)),
      updateUser: vi.fn().mockReturnValue(of(mockUsers[1])),
      deleteUser: vi.fn().mockReturnValue(of(null))
    };

    authServiceSpy = {
      isAdmin: vi.fn().mockReturnValue(true),
      currentUser: vi.fn().mockReturnValue(mockUsers[0])
    };

    dialogSpy = {
      open: vi.fn().mockReturnValue({
        afterClosed: () => of(true)
      })
    };

    await TestBed.configureTestingModule({
      imports: [UserAdminComponent, FormsModule],
      providers: [provideNoopAnimations()]
    }).overrideComponent(UserAdminComponent, {
      set: {
        providers: [
          { provide: AdminService, useValue: adminServiceSpy },
          { provide: AuthService, useValue: authServiceSpy },
          { provide: MatDialog, useValue: dialogSpy }
        ]
      }
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
    component.ngOnInit();
    // It was already loaded in beforeEach, so we reset and test
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
    expect(adminServiceSpy.updateUser).toHaveBeenCalled();
    expect(component.editingUser).toBeNull();
  });

  it('should delete a user after confirmation', () => {
    component.deleteUser(mockUsers[1]);
    expect(dialogSpy.open).toHaveBeenCalled();
    expect(adminServiceSpy.deleteUser).toHaveBeenCalledWith(2);
  });
});
