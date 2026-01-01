import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TaskService } from './task.service';
import { AuthService } from './auth.service';
import { Task, Priority } from '../models/task.model';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';

describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;
  let authServiceSpy: any;

  beforeEach(() => {
    authServiceSpy = {
      getAuthHeader: vi.fn().mockReturnValue('mock-token')
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        TaskService,
        { provide: AuthService, useValue: authServiceSpy }
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
    const mockTasks: Task[] = [{ id: 1, title: 'Test Task', description: 'Desc', dueDate: '2026-01-01', priority: Priority.MEDIUM }];
    service.getTasks().subscribe(tasks => {
      expect(tasks).toEqual(mockTasks);
    });

    const req = httpMock.expectOne('/api/tasks');
    expect(req.request.method).toBe('GET');
    req.flush(mockTasks);
  });

  it('should create task', () => {
    const newTask: Task = { title: 'New', description: 'Desc', dueDate: '2026-01-01', priority: Priority.HIGH };
    service.createTask(newTask).subscribe(task => {
      expect(task).toEqual({ ...newTask, id: 1 });
    });

    const req = httpMock.expectOne('/api/tasks');
    expect(req.request.method).toBe('POST');
    req.flush({ ...newTask, id: 1 });
  });

  it('should update task', () => {
    const updatedTask: Task = { id: 1, title: 'Updated', description: 'Desc', dueDate: '2026-01-01', priority: Priority.HIGH };
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
});
