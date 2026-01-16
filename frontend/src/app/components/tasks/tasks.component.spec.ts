import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TasksComponent } from './tasks.component';
import { TaskService } from '../../services/task.service';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { Priority, Task, TaskStatus } from '../../models/task.model';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';

import { provideNativeDateAdapter } from '@angular/material/core';

describe('TasksComponent', () => {
  let component: TasksComponent;
  let fixture: ComponentFixture<TasksComponent>;
  let taskServiceSpy: any;
  let dialogSpy: any;
  let translateServiceSpy: any;

  const mockTasks: Task[] = [
    { id: 1, title: 'Task 1', description: 'Desc 1', dueDate: '2026-01-01', priority: Priority.MEDIUM, status: TaskStatus.OPEN }
  ];

  beforeEach(async () => {
    try {
      TestBed.initTestEnvironment(
        BrowserDynamicTestingModule,
        platformBrowserDynamicTesting()
      );
    } catch (e) {
      // already initialized
    }

    taskServiceSpy = {
      getTasks: vi.fn().mockReturnValue(of(mockTasks)),
      createTask: vi.fn().mockReturnValue(of({ ...mockTasks[0], id: 2 })),
      updateTask: vi.fn().mockReturnValue(of(mockTasks[0])),
      deleteTask: vi.fn().mockReturnValue(of(null)),
      reorderTasks: vi.fn().mockReturnValue(of([]))
    };

    dialogSpy = {
      open: vi.fn().mockReturnValue({
        afterClosed: () => of(true)
      })
    };

    translateServiceSpy = {
      get: vi.fn().mockReturnValue(of('translated')),
      onTranslationChange: of({}),
      onLangChange: of({}),
      onDefaultLangChange: of({}),
      instant: vi.fn().mockReturnValue('translated'),
      stream: vi.fn().mockReturnValue(of('translated')),
      get currentLang() { return 'en'; }
    };

    authServiceSpy = {
      hasAdminWriteAccess: vi.fn().mockReturnValue(true),
      currentUser: vi.fn().mockReturnValue({ login: 'admin' })
    };

    await TestBed.configureTestingModule({
      imports: [TasksComponent, TranslateModule.forRoot()],
      providers: [
        provideNoopAnimations(),
        provideNativeDateAdapter(),
        { provide: TaskService, useValue: taskServiceSpy },
        { provide: MatDialog, useValue: dialogSpy },
        { provide: TranslateService, useValue: translateServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TasksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(taskServiceSpy.getTasks).toHaveBeenCalled();
  });

  it('should load tasks on init', () => {
    expect(component.tasks.length).toBe(1);
    expect(component.tasks[0].title).toBe('Task 1');
  });

  it('should create a new task', () => {
    component.currentTask = { title: 'New', description: 'New Desc', dueDate: '2026-01-01', priority: Priority.HIGH, status: TaskStatus.OPEN };
    component.onSubmit();
    expect(taskServiceSpy.createTask).toHaveBeenCalled();
    expect(taskServiceSpy.getTasks).toHaveBeenCalledTimes(2);
  });

  it('should create a new task without dueDate', () => {
    component.currentTask = { title: 'No Date', description: 'No Date Desc', dueDate: '', priority: Priority.LOW, status: TaskStatus.OPEN };
    component.onSubmit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(expect.any(Object));
    expect(taskServiceSpy.getTasks).toHaveBeenCalledTimes(2);
  });

  it('should delete a task after confirmation', () => {
    component.deleteTask(mockTasks[0]);
    expect(dialogSpy.open).toHaveBeenCalled();
    expect(taskServiceSpy.deleteTask).toHaveBeenCalledWith(1);
  });
});
