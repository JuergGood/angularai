import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-user-admin',
  standalone: true,
  imports: [MatCardModule],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>User Admin</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <p>This is a dummy component for User Admin.</p>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    mat-card { margin: 20px; }
  `]
})
export class UserAdminComponent {}
