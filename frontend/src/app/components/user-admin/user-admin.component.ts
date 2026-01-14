import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { User } from '../../models/user.model';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import { TranslateModule } from '@ngx-translate/core';
import { ConfirmDialogComponent } from '../tasks/confirm-dialog.component';

@Component({
  selector: 'app-user-admin',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    TranslateModule
  ],
  templateUrl: './user-admin.component.html',
  styles: [`
    .admin-container {
      max-width: 1000px;
      margin: 0 auto;
      padding: 0;
    }
    .page-toolbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 32px;
      gap: 16px;
      flex-wrap: wrap;
    }
    .page-toolbar .left,
    .page-toolbar .right {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
      color: #1f2937;
    }
    .add-user-btn {
      border-radius: 10px;
      padding: 0 20px;
      height: 44px !important;
      box-shadow: 0 2px 6px rgba(0,0,0,.15);
      font-weight: 500;
    }
    .main-card {
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.05);
      border: none;
      overflow: hidden;
    }
    table {
      width: 100%;
    }
    .mat-mdc-table {
      background: transparent;
    }
    .mat-mdc-header-row {
      background-color: #f9fafb;
    }
    .mat-mdc-header-cell {
      color: #374151;
      font-weight: 600;
      font-size: 14px;
      padding: 16px;
    }
    .mat-mdc-cell {
      padding: 16px;
      font-size: 14px;
      color: #4b5563;
    }
    .user-login {
      font-weight: 600;
      color: #111827;
    }
    .role-chip {
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      display: inline-block;
    }
    .role-admin {
      background: rgba(124, 58, 237, 0.1);
      color: #5b21b6;
      border: 1px solid rgba(124, 58, 237, 0.2);
    }
    .role-user {
      background: #f3f4f6;
      color: #374151;
      border: 1px solid #e5e7eb;
    }
    .edit-card {
      margin-top: 32px;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    }
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    @media (max-width: 600px) {
      .form-grid {
        grid-template-columns: 1fr;
      }
    }
    mat-form-field {
      width: 100%;
    }
    .form-actions {
      display: flex;
      gap: 12px;
      margin-top: 24px;
      justify-content: flex-end;
    }
    .error {
      color: #dc2626;
      margin-top: 16px;
      font-size: 14px;
    }
    .user-mobile-card {
      margin-bottom: 16px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    }
    .action-buttons {
      display: flex;
      gap: 4px;
    }
  `]
})
export class UserAdminComponent implements OnInit {
  users: User[] = [];
  displayedColumns: string[] = ['login', 'name', 'email', 'role', 'actions'];
  editingUser: User | null = null;
  error: string | null = null;
  isCreating = false;
  isMobile = false;

  constructor(
    public authService: AuthService,
    private adminService: AdminService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef,
    private breakpointObserver: BreakpointObserver
  ) {}

  ngOnInit() {
    this.breakpointObserver.observe([
      Breakpoints.HandsetPortrait
    ]).subscribe(result => {
      this.isMobile = result.matches;
      this.cdr.detectChanges();
    });

    if (this.authService.isAdmin()) {
      this.loadUsers();
    }
  }

  loadUsers() {
    this.adminService.getUsers().subscribe(users => {
      this.users = users;
      this.cdr.detectChanges();
    });
  }

  createNewUser() {
    this.editingUser = {
      firstName: '',
      lastName: '',
      login: '',
      password: '',
      email: '',
      role: 'ROLE_USER',
      birthDate: '',
      address: ''
    };
    this.isCreating = true;
    this.error = null;
    this.cdr.detectChanges();
  }

  editUser(user: User) {
    this.editingUser = { ...user };
    this.isCreating = false;
    this.error = null;
    this.cdr.detectChanges();
  }

  cancelEdit() {
    this.editingUser = null;
    this.isCreating = false;
    this.error = null;
    this.cdr.detectChanges();
  }

  saveUser() {
    if (this.editingUser) {
      const operation = this.isCreating
        ? this.adminService.createUser(this.editingUser)
        : this.adminService.updateUser(this.editingUser.id!, this.editingUser);

      operation.subscribe({
        next: () => {
          this.loadUsers();
          this.editingUser = null;
          this.isCreating = false;
        },
        error: (err) => {
          this.error = err.error || 'Operation failed';
          this.cdr.detectChanges();
        }
      });
    }
  }

  deleteUser(user: User) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result && user.id) {
        this.adminService.deleteUser(user.id).subscribe({
          next: () => this.loadUsers(),
          error: (err) => {
            alert(err.error || 'Failed to delete user');
          }
        });
      }
    });
  }
}
