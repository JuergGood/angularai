import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { DashboardService } from '../../services/dashboard.service';
import { AuthService } from '../../services/auth.service';
import { of } from 'rxjs';
import { signal } from '@angular/core';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { TranslateModule } from '@ngx-translate/core';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { By } from '@angular/platform-browser';
import { DashboardData } from '../../models/dashboard.model';
import { provideRouter } from '@angular/router';

import '../../../test';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let dashboardServiceSpy: any;
  let authServiceSpy: any;

  const mockDashboardData: DashboardData = {
    summary: {
      openTasks: 10,
      openTasksDelta: 2,
      activeUsers: 5,
      activeUsersDelta: 1,
      completedTasks: 50,
      completedTasksDelta: 5,
      todayLogs: 100,
      todayLogsDelta: 10
    },
    taskDistribution: {
      open: 10,
      inProgress: 5,
      completed: 50,
      archived: 5,
      total: 70
    },
    recentActivity: [],
    recentUsers: [],
    priorityTasks: []
  };

  beforeEach(async () => {
    dashboardServiceSpy = {
      getDashboardData: vi.fn().mockReturnValue(of(mockDashboardData))
    };

    authServiceSpy = {
      isAdmin: vi.fn().mockReturnValue(true),
      currentUser: signal({ login: 'admin' })
    };

    await TestBed.configureTestingModule({
      imports: [DashboardComponent, TranslateModule.forRoot()],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        { provide: DashboardService, useValue: dashboardServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render all summary cards when data is loaded', () => {
    const summaryCards = fixture.debugElement.queryAll(By.css('.summary-card'));
    expect(summaryCards.length).toBe(4);

    // Check for specific summary cards by their data-cy attributes
    expect(fixture.debugElement.query(By.css('[data-cy="dashboard-open-tasks"]'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('[data-cy="dashboard-active-users"]'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('[data-cy="dashboard-completed-tasks"]'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('[data-cy="dashboard-today-logs"]'))).toBeTruthy();
  });

  it('should render main grid cards when data is loaded', () => {
    // There should be 4 cards in the main grid based on the current template
    // 1. Task Overview (if taskDistribution exists)
    // 2. Recent Activity
    // 3. Admin Preview
    // 4. Priority Tasks
    const mainGridCards = fixture.debugElement.queryAll(By.css('.main-grid mat-card'));
    expect(mainGridCards.length).toBe(4);
  });

  it('should render specific sections in the main grid', () => {
    const cards = fixture.debugElement.queryAll(By.css('.main-grid mat-card'));

    // Verify specific titles are present in the cards
    // Using translation keys as they might not be translated yet in the test environment
    const cardTexts = cards.map(card => card.nativeElement.textContent);

    // Check if sections are represented (checking for labels or translated keys)
    const titles = fixture.debugElement.queryAll(By.css('mat-card-title'));
    const titleTexts = titles.map(t => t.nativeElement.textContent.trim());

    expect(titleTexts).toContain('DASHBOARD.TASK_OVERVIEW');
    expect(titleTexts).toContain('ADMIN.TITLE');
    expect(titleTexts).toContain('DASHBOARD.PRIORITY_TASKS');

    // Recent activity title is in a div with class subpanel-title
    const subpanelTitle = fixture.debugElement.query(By.css('.subpanel-title')).nativeElement.textContent.trim();
    expect(subpanelTitle).toBe('DASHBOARD.RECENT_ACTIVITY');
  });

  it('should show loading bar when isLoading is true', () => {
    component.isLoading.set(true);
    fixture.detectChanges();

    const loadingContainer = fixture.debugElement.query(By.css('.dashboard-loading'));
    expect(loadingContainer).toBeTruthy();
    expect(fixture.debugElement.query(By.css('mat-progress-bar'))).toBeTruthy();
  });

  it('should not render dashboard container when vm is null', () => {
    // Mock dashboardData to return null
    component.dashboardData.set(null);
    fixture.detectChanges();

    const container = fixture.debugElement.query(By.css('.dashboard-container'));
    expect(container).toBeFalsy();
  });

  it('should humanize roles correctly', () => {
    // Access private method via any
    expect((component as any).humanizeRole('ROLE_ADMIN')).toBe('Admin');
    expect((component as any).humanizeRole('ROLE_USER')).toBe('User');
    expect((component as any).humanizeRole('UNKNOWN')).toBe('unknown');
  });

  it('should return correct role classes', () => {
    expect((component as any).roleClass('ROLE_ADMIN')).toBe('role-admin');
    expect((component as any).roleClass('ROLE_USER')).toBe('role-user');
    expect((component as any).roleClass('OTHER')).toBe('role-neutral');
  });

  it('should humanize actions correctly', () => {
    expect(component.humanizeAction('TASK_CREATED')).toBe('Task created');
    expect(component.humanizeAction('USER_LOGIN')).toBe('Login');
    expect(component.humanizeAction('UNKNOWN_ACTION')).toBe('Unknown action');
  });

  it('should return correct action classes', () => {
    expect((component as any).actionClass('TASK_CREATED')).toBe('action-neutral');
    expect((component as any).actionClass('USER_LOGIN')).toBe('action-login');
    expect((component as any).actionClass('OTHER')).toBe('action-neutral');
  });

  it('should clear activity filter', () => {
    component.activityFilter.set('something');
    component.clearActivityFilter();
    expect(component.activityFilter()).toBe('');
  });

  it('should calculate pie chart data', () => {
    const data = { open: 1, inProgress: 1, completed: 1, archived: 1, total: 4 };
    const pieData = component.getPieChartData(data);
    expect(pieData.length).toBe(4);
    expect(pieData[0].value).toBe(25);
  });

  it('should parse date only strings correctly', () => {
    expect((component as any).parseDateOnly('2026-01-23')).toBeInstanceOf(Date);
    expect((component as any).parseDateOnly(null)).toBeNull();
  });

  it('should return correct due chip labels and tones', () => {
    const todayStr = new Date().toISOString().split('T')[0];

    const overdueDate = new Date();
    overdueDate.setDate(overdueDate.getDate() - 5);
    const overdueStr = overdueDate.toISOString().split('T')[0];

    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 10);
    const futureStr = futureDate.toISOString().split('T')[0];

    expect(component.getDueChip({ dueDate: todayStr } as any).label).toBe('Today');
    expect(component.getDueChip({ dueDate: todayStr } as any).tone).toBe('warning');

    expect(component.getDueChip({ dueDate: overdueStr } as any).label).toBe('Overdue');
    expect(component.getDueChip({ dueDate: overdueStr } as any).tone).toBe('danger');

    expect(component.getDueChip({ dueDate: futureStr } as any).label).toBe(futureStr);
    expect(component.getDueChip({ dueDate: futureStr } as any).tone).toBe('neutral');
  });

  it('should return relative time strings', () => {
    const now = new Date();
    const oneHourAgo = new Date(now.getTime() - 3600 * 1000);
    const iso = oneHourAgo.toISOString();

    expect(component.toRelativeTime(iso)).toBe('1h ago');
  });

  it('should filter recent activity', () => {
    component.dashboardData.set({
      ...mockDashboardData,
      recentActivity: [
        { id: 1, timestamp: '2026-01-01 10:00:00', login: 'admin', action: 'USER_LOGIN', details: 'test' },
        { id: 2, timestamp: '2026-01-01 11:00:00', login: 'user', action: 'TASK_CREATED', details: 'test' }
      ]
    });
    fixture.detectChanges();

    component.activityFilter.set('admin');
    fixture.detectChanges();

    expect(component.vm()?.recentActivityFiltered.length).toBe(1);
    expect(component.vm()?.recentActivityFiltered[0].login).toBe('admin');
  });
});
