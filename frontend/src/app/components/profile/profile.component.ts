import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="profile-container" *ngIf="user">
      <h2>User Profile</h2>
      <form (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label>First Name</label>
          <input type="text" name="firstName" [(ngModel)]="user.firstName" required>
        </div>
        <div class="form-group">
          <label>Last Name</label>
          <input type="text" name="lastName" [(ngModel)]="user.lastName" required>
        </div>
        <div class="form-group">
          <label>Login</label>
          <input type="text" name="login" [(ngModel)]="user.login" disabled>
        </div>
        <div class="form-group">
          <label>Birth Date</label>
          <input type="date" name="birthDate" [(ngModel)]="user.birthDate" required>
        </div>
        <div class="form-group">
          <label>Address</label>
          <textarea name="address" [(ngModel)]="user.address" required></textarea>
        </div>
        <button type="submit">Save Changes</button>
        <button type="button" (click)="onLogout()" class="logout-btn">Logout</button>
        <p *ngIf="message" class="success">{{ message }}</p>
      </form>
    </div>
  `,
  styles: [`
    .profile-container { max-width: 500px; margin: 50px auto; padding: 20px; border: 1px solid #ccc; border-radius: 5px; }
    .form-group { margin-bottom: 15px; }
    label { display: block; margin-bottom: 5px; }
    input, textarea { width: 100%; padding: 8px; box-sizing: border-box; }
    button { width: 100%; padding: 10px; background-color: #28a745; color: white; border: none; border-radius: 5px; cursor: pointer; margin-bottom: 10px;}
    button:hover { background-color: #218838; }
    .logout-btn { background-color: #dc3545; }
    .logout-btn:hover { background-color: #c82333; }
    .success { color: green; margin-top: 10px; }
  `]
})
export class ProfileComponent implements OnInit {
  user?: User;
  message = '';

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.userService.getCurrentUser().subscribe({
      next: (user) => this.user = user,
      error: () => this.router.navigate(['/login'])
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
