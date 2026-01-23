import { Component, OnInit, signal, ChangeDetectorRef, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslateModule } from '@ngx-translate/core';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../../services/dashboard.service';
import { DashboardData } from '../../models/dashboard.model';
import { AuthService } from '../../services/auth.service';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatProgressBarModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    TranslateModule,
    RouterLink
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  dashboardData = signal<DashboardData | null>(null);
  isLoading = signal(false);
  activityFilter = signal('');
  recentActivityColumns: string[] = ['timestamp', 'login', 'action'];
  recentUsersColumns: string[] = ['login', 'email', 'role'];

  /**
   * Action presentation: semantic chip colors.
   */
  private readonly actionLabelMap: Record<string, string> = {
    USER_LOGIN: 'Login',
    USER_LOGOUT: 'Logout',
    TASK_CREATED: 'Task created',
    TASK_UPDATED: 'Task updated',
    TASK_COMPLETED: 'Task completed',
    USER_CREATED: 'User created',
    USER_UPDATED: 'User updated'
  };

  private readonly actionClassMap: Record<string, string> = {
    USER_LOGIN: 'action-login',
    USER_LOGOUT: 'action-logout',
    TASK_CREATED: 'action-neutral',
    TASK_UPDATED: 'action-neutral',
    TASK_COMPLETED: 'action-neutral',
    USER_CREATED: 'action-neutral',
    USER_UPDATED: 'action-neutral'
  };

  /**
   * Role presentation: short labels + semantic chip colors.
   */
  private readonly roleLabelMap: Record<string, string> = {
    ROLE_ADMIN: 'Admin',
    ROLE_ADMIN_READ: 'Admin (read)',
    ROLE_USER: 'User'
  };

  private humanizeRole(role: string | undefined | null): string {
    if (!role) return '';
    return this.roleLabelMap[role] ?? role.replace(/^ROLE_/, '').replaceAll('_', ' ').toLowerCase();
  }

  private roleClass(role: string | undefined | null): 'role-admin' | 'role-admin-read' | 'role-user' | 'role-neutral' {
    if (!role) return 'role-neutral';
    if (role === 'ROLE_ADMIN') return 'role-admin';
    if (role === 'ROLE_ADMIN_READ') return 'role-admin-read';
    if (role === 'ROLE_USER') return 'role-user';
    return 'role-neutral';
  }

  humanizeAction(action: string): string {
    if (!action) return '';
    return this.actionLabelMap[action] ?? action.replaceAll('_', ' ').toLowerCase().replace(/^\w/, c => c.toUpperCase());
  }

  private actionClass(action: string | undefined | null): 'action-login' | 'action-logout' | 'action-neutral' {
    if (!action) return 'action-neutral';
    if (action === 'USER_LOGIN') return 'action-login';
    if (action === 'USER_LOGOUT') return 'action-logout';
    return this.actionClassMap[action] as any ?? 'action-neutral';
  }


  /** View model helpers */
  readonly vm = computed(() => {
    const data = this.dashboardData();
    if (!data) return null;

    const q = (this.activityFilter() ?? '').trim().toLowerCase();

    // Enrich once, then reuse for different slices (filtered/sorted) to keep the template simple.
    const recentActivityEnriched = (data.recentActivity ?? []).map(l => ({
      ...l,
      timestampLabel: this.toRelativeTime(l.timestamp),
      actionLabel: this.humanizeAction(l.action),
      actionClass: this.actionClass(l.action),
      timestampMs: this.parseTimestampMs(l.timestamp)
    }));

    const recentActivityFiltered = recentActivityEnriched
      .filter(l => {
        if (!q) return true;
        return (
          (l.login ?? '').toLowerCase().includes(q) ||
          (l.actionLabel ?? '').toLowerCase().includes(q) ||
          (l.action ?? '').toLowerCase().includes(q)
        );
      })
      // Default: newest first
      .sort((a, b) => (b.timestampMs ?? 0) - (a.timestampMs ?? 0));

    return {
      ...data,
      recentActivity: recentActivityEnriched,
      recentActivityFiltered,
      recentUsers: (data.recentUsers ?? []).map(u => ({
        ...u,
        roleLabel: this.humanizeRole((u as any).role),
        roleClass: this.roleClass((u as any).role)
      })),
      priorityTasks: (data.priorityTasks ?? []).map(t => ({
        ...t,
        dueChip: this.getDueChip(t)
      }))
    };
  });

  /**
   * Small view-model helpers keep the template readable.
   */
  hasPriorityTasks = computed(() => (this.dashboardData()?.priorityTasks?.length ?? 0) > 0);

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

  trackById = (_: number, item: { id?: number } | null) => item?.id ?? _;

  clearActivityFilter(): void {
    this.activityFilter.set('');
  }

  /**
   * Returns a small chip label + tone based on the task due date.
   * Works best with ISO dates like YYYY-MM-DD.
   */
  getDueChip(task: Task): { label: string; tone: 'danger' | 'warning' | 'neutral' } {
    const due = this.parseDateOnly(task.dueDate);
    if (!due) return { label: task.dueDate, tone: 'neutral' };

    const today = this.startOfDay(new Date());
    const diffDays = Math.round((due.getTime() - today.getTime()) / (24 * 60 * 60 * 1000));

    if (diffDays < 0) return { label: 'Overdue', tone: 'danger' };
    if (diffDays === 0) return { label: 'Today', tone: 'warning' };
    if (diffDays === 1) return { label: 'Tomorrow', tone: 'warning' };
    if (diffDays <= 7) return { label: `In ${diffDays} days`, tone: 'neutral' };
    return { label: task.dueDate, tone: 'neutral' };
  }

  private parseDateOnly(value: string | undefined | null): Date | null {
    if (!value) return null;
    // Supports "YYYY-MM-DD" and falls back to Date parsing.
    const m = /^\d{4}-\d{2}-\d{2}$/.test(value);
    if (m) {
      const [y, mo, d] = value.split('-').map(v => Number(v));
      if (!Number.isFinite(y) || !Number.isFinite(mo) || !Number.isFinite(d)) return null;
      return new Date(y, mo - 1, d);
    }
    const parsed = new Date(value);
    return Number.isFinite(parsed.getTime()) ? parsed : null;
  }

  private startOfDay(d: Date): Date {
    return new Date(d.getFullYear(), d.getMonth(), d.getDate());
  }

  /**
   * Converts timestamps like "2026-01-15 15:30:56" into "5m ago".
   * Falls back to the raw value if parsing fails.
   */
  toRelativeTime(timestamp: string): string {
    try {
      const iso = timestamp.includes('T')
        ? timestamp
        : timestamp.replace(' ', 'T');
      const d = new Date(iso);
      const diffMs = Date.now() - d.getTime();
      if (!Number.isFinite(diffMs)) return timestamp;
      const diffSec = Math.floor(diffMs / 1000);
      if (diffSec < 60) return `${diffSec}s ago`;
      const diffMin = Math.floor(diffSec / 60);
      if (diffMin < 60) return `${diffMin}m ago`;
      const diffH = Math.floor(diffMin / 60);
      if (diffH < 24) return `${diffH}h ago`;
      const diffD = Math.floor(diffH / 24);
      return `${diffD}d ago`;
    } catch {
      return timestamp;
    }
  }

  private parseTimestampMs(timestamp: string): number {
    try {
      const iso = timestamp.includes('T') ? timestamp : timestamp.replace(' ', 'T');
      const d = new Date(iso);
      const ms = d.getTime();
      return Number.isFinite(ms) ? ms : 0;
    } catch {
      return 0;
    }
  }

  getPieChartData(data: { open: number, inProgress: number, completed: number, archived: number, total: number }) {
    if (data.total === 0) return [];

    const openP = (data.open / data.total) * 100;
    const inProgressP = (data.inProgress / data.total) * 100;
    const completedP = (data.completed / data.total) * 100;
    const archivedP = (data.archived / data.total) * 100;

    return [
      { color: '#3f51b5', value: openP, offset: 0 },
      { color: '#2196f3', value: inProgressP, offset: openP },
      { color: '#4caf50', value: completedP, offset: openP + inProgressP },
      { color: '#9e9e9e', value: archivedP, offset: openP + inProgressP + completedP }
    ];
  }
}
