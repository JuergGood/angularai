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
      font-weight: 600;
    }
    .add-user-btn:hover:not(:disabled) {
      transform: translateY(-1px);
    }
    .main-card {
      border-radius: 12px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06) !important;
      border: 1px solid var(--border) !important;
      overflow: hidden;
      background: var(--surface) !important;
    }
    table {
      width: 100%;
    }
    .mat-mdc-table {
      background: transparent;
    }
    .mat-mdc-header-row {
      background-color: var(--surface-2) !important;
      height: 56px;
    }
    .mat-mdc-header-cell {
      color: var(--text-muted) !important;
      font-weight: 600 !important;
      font-size: 11px !important;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      padding: 0 16px !important;
      font-family: inherit !important;
    }
    .mat-mdc-cell {
      padding: 16px !important;
      font-size: 14px;
      color: var(--text);
    }
    .action-buttons {
      display: flex;
      gap: 4px;
    }
    /* Zebra striping */
    .mat-mdc-row:nth-child(even) {
      background-color: color-mix(in srgb, var(--surface-2) 70%, transparent);
    }
    .mat-mdc-row:hover {
      background-color: var(--brand-weak) !important;
    }
    .user-login {
      font-weight: 600;
      color: var(--brand);
    }
    .role-chip {
      height: 22px;
      padding: 0 10px;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 600;
      display: inline-flex;
      align-items: center;
      border: 1px solid var(--border);
    }
    .role-admin {
      background: rgba(255, 152, 0, 0.16) !important;
      color: #e65100 !important;
    }
    .role-admin-read {
      background: rgba(63, 81, 181, 0.14) !important;
      color: var(--brand) !important;
    }
    .role-user {
      background: rgba(76, 175, 80, 0.16) !important;
      color: #2e7d32 !important;
    }
    .role-neutral {
      background: color-mix(in srgb, var(--surface-2) 70%, transparent) !important;
      color: var(--text-muted) !important;
    }
    .edit-card {
      margin-top: 32px;
      border-radius: 12px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06) !important;
      border: 1px solid var(--border) !important;
      overflow: hidden;
      background: var(--surface) !important;
    }
    .edit-card mat-card-header {
      background-color: var(--surface-2) !important;
      padding: 16px 24px;
      border-bottom: 1px solid var(--border);
    }
    .edit-card mat-card-content {
      padding: 24px !important;
    }
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
      margin-top: 8px;
    }
    @media (max-width: 600px) {
      .form-grid {
        grid-template-columns: 1fr;
      }
    }
    mat-form-field {
      width: 100%;
    }
    .submit-btn {
      border-radius: 8px;
      padding: 0 24px;
      height: 44px !important;
      font-weight: 600;
    }
    .submit-btn:hover:not(:disabled) {
      transform: translateY(-1px);
    }
    .form-actions {
      display: flex;
      gap: 12px;
      margin-top: 24px;
      padding-top: 16px;
      border-top: 1px solid var(--border);
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
