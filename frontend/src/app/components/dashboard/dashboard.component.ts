import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TranslateModule } from '@ngx-translate/core';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../../services/dashboard.service';
import { DashboardData } from '../../models/dashboard.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatProgressBarModule,
    TranslateModule,
    RouterLink
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  dashboardData = signal<DashboardData | null>(null);
  isLoading = signal(false);
  recentActivityColumns: string[] = ['timestamp', 'login', 'action'];
  recentUsersColumns: string[] = ['login', 'email', 'role'];

  constructor(
    private dashboardService: DashboardService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.isLoading.set(true);
    this.dashboardService.getDashboardData().subscribe({
      next: (data) => {
        this.dashboardData.set(data);
        this.isLoading.set(false);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching dashboard data', err);
        this.isLoading.set(false);
        this.cdr.detectChanges();
      }
    });
  }

  getPieChartData(data: { open: number, inProgress: number, completed: number, total: number }) {
    if (data.total === 0) return [];

    const openP = (data.open / data.total) * 100;
    const inProgressP = (data.inProgress / data.total) * 100;
    const completedP = (data.completed / data.total) * 100;

    return [
      { color: '#3f51b5', value: openP, offset: 0 },
      { color: '#2196f3', value: inProgressP, offset: openP },
      { color: '#4caf50', value: completedP, offset: openP + inProgressP }
    ];
  }
}
