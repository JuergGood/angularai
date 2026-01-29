import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TaskService } from './task.service';
import { Task, Priority, TaskStatus } from '../models/task.model';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';

describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    try {
      TestBed.initTestEnvironment(
        BrowserDynamicTestingModule,
        platformBrowserDynamicTesting()
      );
    } catch (e) {
      // already initialized
    }

    TestBed.configureTestingModule({
      providers: [
        TaskService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get tasks', () => {
    const mockTasks: Task[] = [{ id: 1, title: 'Test Task', description: 'Desc', dueDate: '2026-01-01', priority: Priority.MEDIUM, status: TaskStatus.OPEN }];
    service.getTasks().subscribe(tasks => {
      expect(tasks).toEqual(mockTasks);
    });

    const req = httpMock.expectOne('/api/tasks');
    expect(req.request.method).toBe('GET');
    req.flush(mockTasks);
  });

  it('should create task', () => {
    const newTask: Task = { title: 'New', description: 'Desc', dueDate: '2026-01-01', priority: Priority.HIGH, status: TaskStatus.OPEN };
    service.createTask(newTask).subscribe(task => {
      expect(task).toEqual({ ...newTask, id: 1 });
    });

    const req = httpMock.expectOne('/api/tasks');
    expect(req.request.method).toBe('POST');
    req.flush({ ...newTask, id: 1 });
  });

  it('should update task', () => {
    const updatedTask: Task = { id: 1, title: 'Updated', description: 'Desc', dueDate: '2026-01-01', priority: Priority.HIGH, status: TaskStatus.OPEN };
    service.updateTask(1, updatedTask).subscribe(task => {
      expect(task).toEqual(updatedTask);
    });

    const req = httpMock.expectOne('/api/tasks/1');
    expect(req.request.method).toBe('PUT');
    req.flush(updatedTask);
  });

  it('should delete task', () => {
    service.deleteTask(1).subscribe();

    const req = httpMock.expectOne('/api/tasks/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should reorder tasks', () => {
    const taskIds = [1, 2, 3];
    service.reorderTasks(taskIds).subscribe();

    const req = httpMock.expectOne('/api/tasks/reorder');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(taskIds);
    req.flush(null);
  });

  it('should patch task', () => {
    const patch: Partial<Task> = { status: TaskStatus.DONE };
    const mockTask: Task = { id: 1, title: 'T', status: TaskStatus.DONE };
    service.patchTask(1, patch).subscribe(task => {
      expect(task).toEqual(mockTask);
    });

    const req = httpMock.expectOne('/api/tasks/1');
    expect(req.request.method).toBe('PATCH');
    req.flush(mockTask);
  });

  it('should analyze task', () => {
    const mockTask: Task = { title: 'Analyzed' };
    service.analyzeTask('some input').subscribe(task => {
      expect(task).toEqual(mockTask);
    });

    const req = httpMock.expectOne('/api/tasks/analyze');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBe('some input');
    req.flush(mockTask);
  });

  it('should quickAdd task', () => {
    const mockTask: Task = { id: 1, title: 'Quick' };
    service.quickAdd('Quick').subscribe(task => {
      expect(task).toEqual(mockTask);
    });

    const req = httpMock.expectOne('/api/tasks');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ title: 'Quick' });
    req.flush(mockTask);
  });

  it('should bulkPatch tasks', () => {
    const ids = [1, 2];
    const patch = { status: TaskStatus.DONE };
    const mockTasks = [{ id: 1, status: TaskStatus.DONE }, { id: 2, status: TaskStatus.DONE }];
    service.bulkPatchTasks(ids, patch).subscribe(tasks => {
      expect(tasks).toEqual(mockTasks as any);
    });

    const req = httpMock.expectOne('/api/tasks/bulk');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ ids, patch });
    req.flush(mockTasks);
  });

  it('should bulkDelete tasks', () => {
    const ids = [1, 2];
    service.bulkDeleteTasks(ids).subscribe();

    const req = httpMock.expectOne('/api/tasks/bulk');
    expect(req.request.method).toBe('DELETE');
    expect(req.request.body).toEqual(ids);
    req.flush(null);
  });

  it('should get tasks with params', () => {
    service.getTasks({ status: TaskStatus.IN_PROGRESS, smartFilter: 'TODAY', sort: 'title,asc' }).subscribe();
    const req = httpMock.expectOne(req => req.url === '/api/tasks' && req.params.get('status') === TaskStatus.IN_PROGRESS);
    expect(req.request.params.get('smartFilter')).toBe('TODAY');
    expect(req.request.params.get('sort')).toBe('title,asc');
    req.flush([]);
  });
});
