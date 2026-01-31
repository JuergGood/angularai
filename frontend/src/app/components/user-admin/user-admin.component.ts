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
  styleUrl: './user-admin.component.css'
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

  isProtectedUser(user: User): boolean {
    return ['admin', 'user', 'admin-read'].includes(user.login) || user.login === this.authService.currentUser()?.login;
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
