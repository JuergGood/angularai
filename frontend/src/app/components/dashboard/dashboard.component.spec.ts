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
});
