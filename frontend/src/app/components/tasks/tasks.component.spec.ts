import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TasksComponent } from './tasks.component';
import { TaskService } from '../../services/task.service';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Priority, Task } from '../../models/task.model';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { provideNativeDateAdapter } from '@angular/material/core';

describe('TasksComponent', () => {
  let component: TasksComponent;
  let fixture: ComponentFixture<TasksComponent>;
  let taskServiceSpy: any;
  let dialogSpy: any;

  const mockTasks: Task[] = [
    { id: 1, title: 'Task 1', description: 'Desc 1', dueDate: '2026-01-01', priority: Priority.MEDIUM }
  ];

  beforeEach(async () => {
    taskServiceSpy = {
      getTasks: vi.fn().mockReturnValue(of(mockTasks)),
      createTask: vi.fn().mockReturnValue(of({ ...mockTasks[0], id: 2 })),
      updateTask: vi.fn().mockReturnValue(of(mockTasks[0])),
      deleteTask: vi.fn().mockReturnValue(of(null))
    };

    dialogSpy = {
      open: vi.fn().mockReturnValue({
        afterClosed: () => of(true)
      })
    };

    await TestBed.configureTestingModule({
      imports: [NoopAnimationsModule, TasksComponent],
    }).overrideComponent(TasksComponent, {
      set: {
        providers: [
          { provide: TaskService, useValue: taskServiceSpy },
          { provide: MatDialog, useValue: dialogSpy },
          provideNativeDateAdapter()
        ]
      }
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
    component.currentTask = { title: 'New', description: 'New Desc', dueDate: '2026-01-01', priority: Priority.HIGH };
    component.onSubmit();
    expect(taskServiceSpy.createTask).toHaveBeenCalled();
    expect(taskServiceSpy.getTasks).toHaveBeenCalledTimes(2);
  });

  it('should create a new task without dueDate', () => {
    component.currentTask = { title: 'No Date', description: 'No Date Desc', dueDate: '', priority: Priority.LOW };
    component.onSubmit();
    expect(taskServiceSpy.createTask).toHaveBeenCalledWith(expect.objectContaining({
      title: 'No Date',
      dueDate: ''
    }));
    expect(taskServiceSpy.getTasks).toHaveBeenCalledTimes(2);
  });

  it('should delete a task after confirmation', () => {
    component.deleteTask(mockTasks[0]);
    expect(dialogSpy.open).toHaveBeenCalled();
    expect(taskServiceSpy.deleteTask).toHaveBeenCalledWith(1);
  });
});
