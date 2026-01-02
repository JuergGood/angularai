import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { provideNativeDateAdapter } from '@angular/material/core';

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
    MatDatepickerModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './profile.component.html',
  styles: [`
    .profile-page {
      display: flex;
      justify-content: center;
      padding: 20px;
    }
    .profile-card {
      width: 100%;
      max-width: 500px;
    }
    mat-form-field {
      width: 100%;
      margin-bottom: 10px;
    }
    .actions {
      display: flex;
      flex-direction: column;
      gap: 10px;
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
    private cdr: ChangeDetectorRef
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
        next: () => {
          this.message = 'Profile updated successfully!';
          this.cdr.detectChanges();
        },
        error: () => {
          this.message = 'Error updating profile.';
          this.cdr.detectChanges();
        }
      });
    }
  }

  onLogout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
