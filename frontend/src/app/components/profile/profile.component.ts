import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../tasks/confirm-dialog.component';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDatepickerModule,
    MatDialogModule,
    MatSnackBarModule,
    TranslateModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './profile.component.html',
  styles: [`
    .profile-page {
      max-width: 800px;
      margin: 0 auto;
    }
    .page-toolbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 24px;
      gap: 16px;
      flex-wrap: wrap;
    }
    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
      color: var(--text);
      letter-spacing: -0.5px;
    }
    .form-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }
    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      padding-top: 16px;
      border-top: 1px solid var(--border);
    }
    .success { color: #4caf50; margin-top: 10px; text-align: center; }
    .error { color: #f44336; margin-top: 10px; text-align: center; }
  `]
})
export class ProfileComponent implements OnInit {
  user?: User;
  message = '';

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) {}

  ngOnInit() {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching user profile:', err);
        this.router.navigate(['/login']);
      }
    });
  }

  onSubmit() {
    if (this.user) {
      const userToSave = { ...this.user };
      if (userToSave.birthDate) {
        const d = new Date(userToSave.birthDate);
        if (!isNaN(d.getTime())) {
          // Format to YYYY-MM-DD
          const year = d.getFullYear();
          const month = String(d.getMonth() + 1).padStart(2, '0');
          const day = String(d.getDate()).padStart(2, '0');
          userToSave.birthDate = `${year}-${month}-${day}`;
        }
      }

      this.userService.updateCurrentUser(userToSave).subscribe({
        next: (response) => {
          if (response && response.user) {
            this.user = response.user;
          } else if (response) {
            this.user = response;
          }

          if (response && response.message) {
            this.snackBar.open(this.translate.instant('NAV.PROFILE_EMAIL_VERIFICATION_SENT'), 'OK', { duration: 5000 });
            this.message = '';
          } else {
            this.message = 'COMMON.SUCCESS';
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.message = err.error || 'COMMON.ERROR';
          this.cdr.detectChanges();
        }
      });
    }
  }

  onLogout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  onDeleteAccount() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: { message: 'NAV.PROFILE_DELETE_CONFIRM' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.userService.deleteCurrentUser().subscribe({
          next: () => {
            const successMsg = this.translate.instant('NAV.PROFILE_DELETED_SUCCESS');
            this.snackBar.open(successMsg, 'OK', { duration: 5000 });
            this.authService.logout();
            this.router.navigate(['/login']);
          },
          error: (err) => {
            console.error('Error deleting account:', err);
            this.message = 'COMMON.ERROR';
            this.cdr.detectChanges();
          }
        });
      }
    });
  }

  isDeletable(): boolean {
    return this.user !== undefined && !['admin', 'admin-read', 'user'].includes(this.user.login);
  }
}
