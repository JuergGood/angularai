import { ComponentFixture, TestBed } from '@angular/core/testing';
import { QuickAddTaskComponent } from './quick-add-task.component';
import { TaskService } from '../../services/task.service';
import { of, throwError } from 'rxjs';
import { Priority, TaskStatus } from '../../models/task.model';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { signal } from '@angular/core';

import '../../../test';

describe('QuickAddTaskComponent', () => {
  let component: QuickAddTaskComponent;
  let fixture: ComponentFixture<QuickAddTaskComponent>;
  let taskServiceSpy: any;

  beforeEach(async () => {
    taskServiceSpy = {
      quickAdd: vi.fn().mockReturnValue(of({})),
      createTask: vi.fn().mockReturnValue(of({})),
      analyzeTask: vi.fn().mockReturnValue(of({}))
    };

    await TestBed.configureTestingModule({
      imports: [QuickAddTaskComponent, TranslateModule.forRoot()],
      providers: [
        provideNoopAnimations(),
        { provide: TaskService, useValue: taskServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(QuickAddTaskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should parse simple title correctly', () => {
    component.titleControl.setValue('Buy milk');
    component.submit();
    expect(taskServiceSpy.quickAdd).toHaveBeenCalledWith('Buy milk');
    expect(component.titleControl.value).toBe('');
  });

  it('should parse with pipe separator correctly', () => {
    component.titleControl.setValue('Task | Desc | 2026-01-25 | HIGH | OPEN');
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith({
      title: 'Task',
      description: 'Desc',
      dueDate: '2026-01-25',
      priority: 'HIGH',
      status: 'OPEN'
    });
  });

  it('should parse with semicolon separator correctly', () => {
    component.titleControl.setValue('Task ; Desc ; 2026-01-25 ; low ; in progress');
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith({
      title: 'Task',
      description: 'Desc',
      dueDate: '2026-01-25',
      priority: 'LOW',
      status: 'IN_PROGRESS'
    });
  });

  it('should parse with comma separator correctly', () => {
    component.titleControl.setValue('Task, Desc, 2026-01-25, critical, done');
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith({
      title: 'Task',
      description: 'Desc',
      dueDate: '2026-01-25',
      priority: 'CRITICAL',
      status: 'DONE'
    });
  });

  it('should handle relative dates correctly (today)', () => {
    component.titleControl.setValue('Task | | today');
    component.submit();
    const today = new Date().toISOString().split('T')[0];
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith({
      title: 'Task',
      dueDate: today
    });
  });

  it('should handle relative dates correctly (morgen)', () => {
    component.titleControl.setValue('Task | | morgen');
    component.submit();
    const tomorrowDate = new Date();
    tomorrowDate.setDate(tomorrowDate.getDate() + 1);
    const tomorrow = tomorrowDate.toISOString().split('T')[0];
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith({
      title: 'Task',
      dueDate: tomorrow
    });
  });

  it('should handle space-based parsing correctly (Title Date Prio Status)', () => {
    component.titleControl.setValue('Meeting today HIGH IN_PROGRESS');
    component.submit();
    const today = new Date().toISOString().split('T')[0];
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith({
      title: 'Meeting',
      dueDate: today,
      priority: 'HIGH',
      status: 'IN_PROGRESS'
    });
  });

  it('should handle space-based parsing with mixed case', () => {
    component.titleControl.setValue('Call boss tomorrow critical open');
    component.submit();
    const tomorrowDate = new Date();
    tomorrowDate.setDate(tomorrowDate.getDate() + 1);
    const tomorrow = tomorrowDate.toISOString().split('T')[0];
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith({
      title: 'Call boss',
      dueDate: tomorrow,
      priority: 'CRITICAL',
      status: 'OPEN'
    });
  });

  it('should handle space-based parsing with title only', () => {
    component.titleControl.setValue('Just a title');
    component.submit();
    expect(taskServiceSpy.quickAdd).toHaveBeenCalledWith('Just a title');
  });

  it('should show error if title is missing with separators', () => {
    component.titleControl.setValue('| Desc | 2026-01-25');
    component.submit();
    expect(component.errorMessage()).toBe('TASKS.ERROR_TITLE_REQUIRED');
    expect(taskServiceSpy.createTask).not.toHaveBeenCalled();
  });

  it('should show error for invalid date', () => {
    component.titleControl.setValue('Task | | invalid-date');
    component.submit();
    expect(component.errorMessage()).toBe('TASKS.ERROR_INVALID_DATE');
    expect(taskServiceSpy.createTask).not.toHaveBeenCalled();
  });

  it('should show error for invalid priority', () => {
    component.titleControl.setValue('Task | | | BOGUS');
    component.submit();
    expect(component.errorMessage()).toBe('TASKS.ERROR_INVALID_PRIORITY');
    expect(taskServiceSpy.createTask).not.toHaveBeenCalled();
  });

  it('should show error for invalid status', () => {
    component.titleControl.setValue('Task | | | | BOGUS');
    component.submit();
    expect(component.errorMessage()).toBe('TASKS.ERROR_INVALID_STATUS');
    expect(taskServiceSpy.createTask).not.toHaveBeenCalled();
  });

  it('should handle space-based parsing with 5 days', () => {
    component.titleControl.setValue('Pay bills 5 days');
    component.submit();
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 5);
    const expected = futureDate.toISOString().split('T')[0];
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith({
      title: 'Pay bills',
      dueDate: expected
    });
  });

  it('should handle space-based parsing with status only', () => {
    component.titleControl.setValue('Fix bug ARCHIVED');
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith({
      title: 'Fix bug',
      status: 'ARCHIVED'
    });
  });
});
