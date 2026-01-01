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
  template: `
    <div class="profile-page">
      @if (user) {
      <mat-card class="profile-card">
        <mat-card-header>
          <mat-card-title>User Profile Details</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form (ngSubmit)="onSubmit()">
            <mat-form-field appearance="fill">
              <mat-label>First Name</mat-label>
              <input matInput name="firstName" [(ngModel)]="user.firstName" required>
            </mat-form-field>

            <mat-form-field appearance="fill">
              <mat-label>Last Name</mat-label>
              <input matInput name="lastName" [(ngModel)]="user.lastName" required>
            </mat-form-field>

            <mat-form-field appearance="fill">
              <mat-label>Login</mat-label>
              <input matInput name="login" [(ngModel)]="user.login" disabled>
            </mat-form-field>

            <mat-form-field appearance="fill">
              <mat-label>Birth Date</mat-label>
              <input matInput [matDatepicker]="picker" name="birthDate" [(ngModel)]="user.birthDate" required>
              <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
              <mat-datepicker #picker></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="fill">
              <mat-label>Address</mat-label>
              <textarea matInput name="address" [(ngModel)]="user.address" required></textarea>
            </mat-form-field>

            <div class="actions">
              <button mat-raised-button color="primary" type="submit">Save Changes</button>
              <button mat-button color="warn" type="button" (click)="onLogout()">Logout</button>
            </div>

            @if (message) {
              <p [class.success]="!message.includes('Error')" [class.error]="message.includes('Error')">
                {{ message }}
              </p>
            }
          </form>
        </mat-card-content>
      </mat-card>
      }
    </div>
  `,
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
    console.log('Fetching user profile...');
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        console.log('User profile received:', user);
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
      this.userService.updateCurrentUser(this.user).subscribe({
        next: () => this.message = 'Profile updated successfully!',
        error: () => this.message = 'Error updating profile.'
      });
    }
  }

  onLogout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
