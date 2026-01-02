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
import { User } from '../../models/user.model';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
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
    MatDialogModule
  ],
  template: `
    <div class="admin-container">
      @if (authService.isAdmin()) {
        <mat-card>
          <mat-card-header>
            <mat-card-title>User Administration</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <table mat-table [dataSource]="users" class="mat-elevation-z8">
              <ng-container matColumnDef="login">
                <th mat-header-cell *matHeaderCellDef> Login </th>
                <td mat-cell *matCellDef="let user"> {{user.login}} </td>
              </ng-container>

              <ng-container matColumnDef="name">
                <th mat-header-cell *matHeaderCellDef> Name </th>
                <td mat-cell *matCellDef="let user"> {{user.firstName}} {{user.lastName}} </td>
              </ng-container>

              <ng-container matColumnDef="email">
                <th mat-header-cell *matHeaderCellDef> Email </th>
                <td mat-cell *matCellDef="let user"> {{user.email}} </td>
              </ng-container>

              <ng-container matColumnDef="role">
                <th mat-header-cell *matHeaderCellDef> Role </th>
                <td mat-cell *matCellDef="let user"> {{user.role}} </td>
              </ng-container>

              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef> Actions </th>
                <td mat-cell *matCellDef="let user">
                  <button mat-icon-button color="primary" (click)="editUser(user)">
                    <mat-icon>edit</mat-icon>
                  </button>
                  <button mat-icon-button color="warn" (click)="deleteUser(user)" [disabled]="user.login === authService.currentUser()?.login">
                    <mat-icon>delete</mat-icon>
                  </button>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>

            @if (editingUser) {
              <mat-card class="edit-card">
                <mat-card-header>
                  <mat-card-title>Edit User: {{editingUser.login}}</mat-card-title>
                </mat-card-header>
                <mat-card-content>
                  <form (ngSubmit)="saveUser()">
                    <mat-form-field appearance="fill">
                      <mat-label>First Name</mat-label>
                      <input matInput name="firstName" [(ngModel)]="editingUser.firstName" required>
                    </mat-form-field>
                    <mat-form-field appearance="fill">
                      <mat-label>Last Name</mat-label>
                      <input matInput name="lastName" [(ngModel)]="editingUser.lastName" required>
                    </mat-form-field>
                    <mat-form-field appearance="fill">
                      <mat-label>Email</mat-label>
                      <input matInput type="email" name="email" [(ngModel)]="editingUser.email" required email>
                    </mat-form-field>
                    <mat-form-field appearance="fill">
                      <mat-label>Role</mat-label>
                      <mat-select name="role" [(ngModel)]="editingUser.role" required [disabled]="editingUser.login === authService.currentUser()?.login">
                        <mat-option value="ROLE_USER">User</mat-option>
                        <mat-option value="ROLE_ADMIN">Admin</mat-option>
                      </mat-select>
                    </mat-form-field>
                    <div class="form-actions">
                      <button mat-raised-button color="primary" type="submit">Save</button>
                      <button mat-button type="button" (click)="cancelEdit()">Cancel</button>
                    </div>
                    @if (error) {
                      <p class="error">{{error}}</p>
                    }
                  </form>
                </mat-card-content>
              </mat-card>
            }
          </mat-card-content>
        </mat-card>
      } @else {
        <mat-card>
          <mat-card-content>
            <p>Access Denied. Admin privileges required.</p>
          </mat-card-content>
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .admin-container { padding: 20px; }
    table { width: 100%; margin-top: 20px; }
    .edit-card { margin-top: 30px; }
    mat-form-field { width: 100%; margin-bottom: 10px; }
    .form-actions { display: flex; gap: 10px; }
    .error { color: #f44336; margin-top: 10px; }
  `]
})
export class UserAdminComponent implements OnInit {
  users: User[] = [];
  displayedColumns: string[] = ['login', 'name', 'email', 'role', 'actions'];
  editingUser: User | null = null;
  error: string | null = null;

  constructor(
    public authService: AuthService,
    private adminService: AdminService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
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

  editUser(user: User) {
    this.editingUser = { ...user };
    this.error = null;
  }

  cancelEdit() {
    this.editingUser = null;
    this.error = null;
  }

  saveUser() {
    if (this.editingUser && this.editingUser.id) {
      this.adminService.updateUser(this.editingUser.id, this.editingUser).subscribe({
        next: () => {
          this.loadUsers();
          this.editingUser = null;
        },
        error: (err) => {
          this.error = err.error || 'Failed to update user';
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
