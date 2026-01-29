import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Task, TaskStatus } from '../models/task.model';
import { AuthService } from './auth.service';

export type SmartFilter = 'ALL' | 'TODAY' | 'UPCOMING' | 'OVERDUE' | 'HIGH';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = '/api/tasks';

  private tasksSignal = signal<Task[]>([]);
  readonly tasks = this.tasksSignal.asReadonly();

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  getTasks(params?: { status?: TaskStatus; smartFilter?: SmartFilter; sort?: string }): Observable<Task[]> {
    let httpParams = new HttpParams();
    if (params) {
      if (params.status) httpParams = httpParams.set('status', params.status);
      if (params.smartFilter) httpParams = httpParams.set('smartFilter', params.smartFilter);
      if (params.sort) httpParams = httpParams.set('sort', params.sort);
    }
    return this.http.get<Task[]>(this.apiUrl, { headers: this.getHeaders(), params: httpParams }).pipe(
      tap(tasks => this.tasksSignal.set(tasks))
    );
  }

  createTask(task: Task): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, task, { headers: this.getHeaders() }).pipe(
      tap(newTask => this.tasksSignal.update(tasks => [newTask, ...tasks]))
    );
  }

  quickAdd(title: string): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, { title }, { headers: this.getHeaders() }).pipe(
      tap(newTask => {
        // Use a new array reference to ensure signal consumers (like effects) are triggered
        this.tasksSignal.update(tasks => [newTask, ...tasks]);
      })
    );
  }

  updateTask(id: number, task: Task): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, task, { headers: this.getHeaders() }).pipe(
      tap(updatedTask => this.tasksSignal.update(tasks => tasks.map(t => t.id === id ? updatedTask : t)))
    );
  }

  patchTask(id: number, patch: Partial<Task>): Observable<Task> {
    return this.http.patch<Task>(`${this.apiUrl}/${id}`, patch, { headers: this.getHeaders() }).pipe(
      tap(updatedTask => this.tasksSignal.update(tasks => tasks.map(t => t.id === id ? updatedTask : t)))
    );
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() }).pipe(
      tap(() => this.tasksSignal.update(tasks => tasks.filter(t => t.id !== id)))
    );
  }

  reorderTasks(taskIds: number[]): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/reorder`, taskIds, { headers: this.getHeaders() });
  }

  analyzeTask(input: string): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}/analyze`, input, { headers: this.getHeaders() });
  }

  bulkPatchTasks(ids: number[], patch: Partial<Task>): Observable<Task[]> {
    return this.http.patch<Task[]>(`${this.apiUrl}/bulk`, { ids, patch }, { headers: this.getHeaders() }).pipe(
      tap(updatedTasks => {
        const updatedMap = new Map(updatedTasks.map(t => [t.id, t]));
        this.tasksSignal.update(tasks => tasks.map(t => updatedMap.get(t.id) ?? t));
      })
    );
  }

  bulkDeleteTasks(ids: number[]): Observable<void> {
    const options = {
      headers: this.getHeaders(),
      body: ids
    };
    return this.http.delete<void>(`${this.apiUrl}/bulk`, options).pipe(
      tap(() => this.tasksSignal.update(tasks => tasks.filter(t => !ids.includes(t.id!))))
    );
  }
}
