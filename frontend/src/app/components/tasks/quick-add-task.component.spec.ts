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
    const task: any = { title: 'Buy milk' };
    component.titleControl.setValue('Buy milk');
    component.parsedTask.set(task);
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(task);
    expect(component.titleControl.value).toBe('');
  });

  it('should parse with pipe separator correctly', () => {
    const task: any = {
      title: 'Task',
      description: 'Desc',
      dueDate: '2026-01-25',
      priority: 'HIGH',
      status: 'OPEN'
    };
    component.titleControl.setValue('Task | Desc | 2026-01-25 | HIGH | OPEN');
    component.parsedTask.set(task);
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(task);
  });

  it('should parse with semicolon separator correctly', () => {
    const task: any = {
      title: 'Task',
      description: 'Desc',
      dueDate: '2026-01-25',
      priority: 'LOW',
      status: 'IN_PROGRESS'
    };
    component.titleControl.setValue('Task ; Desc ; 2026-01-25 ; low ; in progress');
    component.parsedTask.set(task);
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(task);
  });

  it('should parse with comma separator correctly', () => {
    const task: any = {
      title: 'Task',
      description: 'Desc',
      dueDate: '2026-01-25',
      priority: 'CRITICAL',
      status: 'DONE'
    };
    component.titleControl.setValue('Task, Desc, 2026-01-25, critical, done');
    component.parsedTask.set(task);
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(task);
  });

  it('should handle relative dates correctly (today)', () => {
    const today = new Date().toISOString().split('T')[0];
    const task: any = {
      title: 'Task',
      dueDate: today
    };
    component.titleControl.setValue('Task | | today');
    component.parsedTask.set(task);
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(task);
  });

  it('should handle relative dates correctly (morgen)', () => {
    const tomorrowDate = new Date();
    tomorrowDate.setDate(tomorrowDate.getDate() + 1);
    const tomorrow = tomorrowDate.toISOString().split('T')[0];
    const task: any = {
      title: 'Task',
      dueDate: tomorrow
    };
    component.titleControl.setValue('Task | | morgen');
    component.parsedTask.set(task);
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(task);
  });

  it('should handle space-based parsing correctly (Title Date Prio Status)', () => {
    const today = new Date().toISOString().split('T')[0];
    const task: any = {
      title: 'Meeting',
      dueDate: today,
      priority: 'HIGH',
      status: 'IN_PROGRESS'
    };
    component.titleControl.setValue('Meeting today HIGH IN_PROGRESS');
    component.parsedTask.set(task);
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(task);
  });

  it('should handle space-based parsing with mixed case', () => {
    const tomorrowDate = new Date();
    tomorrowDate.setDate(tomorrowDate.getDate() + 1);
    const tomorrow = tomorrowDate.toISOString().split('T')[0];
    const task: any = {
      title: 'Call boss',
      dueDate: tomorrow,
      priority: 'CRITICAL',
      status: 'OPEN'
    };
    component.titleControl.setValue('Call boss tomorrow critical open');
    component.parsedTask.set(task);
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(task);
  });

  it('should handle space-based parsing with title only', () => {
    const task: any = { title: 'Just a title' };
    component.titleControl.setValue('Just a title');
    component.parsedTask.set(task);
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(task);
  });

  it('should show error if title is missing with separators', () => {
    component.titleControl.setValue('| Desc | 2026-01-25');
    component.parsedTask.set({ description: 'Desc', dueDate: '2026-01-25' } as any);
    component.submit();
    expect(component.errorMessage()).toBe('TASKS.ERROR_TITLE_REQUIRED');
    expect(taskServiceSpy.createTask).not.toHaveBeenCalled();
  });

  it('should show error for invalid date', () => {
    // If analyzeTask returns null for invalid input
    component.titleControl.setValue('Task | | invalid-date');
    component.parsedTask.set(null);
    component.submit();
    expect(component.errorMessage()).toBe('TASKS.ERROR_TITLE_REQUIRED');
    expect(taskServiceSpy.createTask).not.toHaveBeenCalled();
  });

  it('should show error for invalid priority', () => {
    // If analyzeTask returns null for invalid input
    component.titleControl.setValue('Task | | | BOGUS');
    component.parsedTask.set(null);
    component.submit();
    expect(component.errorMessage()).toBe('TASKS.ERROR_TITLE_REQUIRED');
    expect(taskServiceSpy.createTask).not.toHaveBeenCalled();
  });

  it('should show error for invalid status', () => {
    // If analyzeTask returns null for invalid input
    component.titleControl.setValue('Task | | | | BOGUS');
    component.parsedTask.set(null);
    component.submit();
    expect(component.errorMessage()).toBe('TASKS.ERROR_TITLE_REQUIRED');
    expect(taskServiceSpy.createTask).not.toHaveBeenCalled();
  });

  it('should handle space-based parsing with 5 days', () => {
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 5);
    const expected = futureDate.toISOString().split('T')[0];
    const task: any = {
      title: 'Pay bills',
      dueDate: expected
    };
    component.titleControl.setValue('Pay bills 5 days');
    component.parsedTask.set(task);
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(task);
  });

  it('should handle space-based parsing with status only', () => {
    const task: any = {
      title: 'Fix bug',
      status: 'ARCHIVED'
    };
    component.titleControl.setValue('Fix bug ARCHIVED');
    component.parsedTask.set(task);
    component.submit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(task);
  });
});
