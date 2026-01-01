import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { provideNativeDateAdapter } from '@angular/material/core';
import { Task, Priority } from '../../models/task.model';
import { TaskService } from '../../services/task.service';
import { ConfirmDialogComponent } from './confirm-dialog.component';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatDialogModule
  ],
  providers: [provideNativeDateAdapter()],
  template: `
    <div class="tasks-container">
      <mat-card class="add-task-card">
        <mat-card-header>
          <mat-card-title>{{ editingTask ? 'Edit Task' : 'Add New Task' }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form (ngSubmit)="onSubmit()" #taskForm="ngForm">
            <mat-form-field appearance="fill">
              <mat-label>Title</mat-label>
              <input matInput name="title" [(ngModel)]="currentTask.title" required>
            </mat-form-field>

            <mat-form-field appearance="fill">
              <mat-label>Description</mat-label>
              <textarea matInput name="description" [(ngModel)]="currentTask.description" required></textarea>
            </mat-form-field>

            <mat-form-field appearance="fill">
              <mat-label>Due Date</mat-label>
              <input matInput [matDatepicker]="picker" name="dueDate" [(ngModel)]="currentTask.dueDate" required>
              <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
              <mat-datepicker #picker></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="fill">
              <mat-label>Priority</mat-label>
              <mat-select name="priority" [(ngModel)]="currentTask.priority" required>
                @for (p of priorities; track p) {
                  <mat-option [value]="p">{{ p }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            <div class="form-actions">
              <button mat-raised-button color="primary" type="submit" [disabled]="!taskForm.form.valid">
                {{ editingTask ? 'Update Task' : 'Add Task' }}
              </button>
              @if (editingTask) {
                <button mat-button type="button" (click)="cancelEdit()">Cancel</button>
              }
            </div>
          </form>
        </mat-card-content>
      </mat-card>

      <div class="tasks-list">
        @for (task of tasks; track task.id) {
          <mat-card class="task-item">
            <mat-card-header>
              <mat-card-title>{{ task.title }}</mat-card-title>
              <mat-card-subtitle>
                Due: {{ task.dueDate }} | Priority: <span [class]="'priority-' + task.priority.toLowerCase()">{{ task.priority }}</span>
              </mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <p>{{ task.description }}</p>
            </mat-card-content>
            <mat-card-actions align="end">
              <button mat-icon-button color="primary" (click)="editTask(task)">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="deleteTask(task)">
                <mat-icon>delete</mat-icon>
              </button>
            </mat-card-actions>
          </mat-card>
        } @empty {
          <p>No tasks found.</p>
        }
      </div>
    </div>
  `,
  styles: [`
    .tasks-container {
      max-width: 800px;
      margin: 0 auto;
      padding: 20px;
    }
    .add-task-card {
      margin-bottom: 30px;
    }
    mat-form-field {
      width: 100%;
      margin-bottom: 10px;
    }
    .form-actions {
      display: flex;
      gap: 10px;
    }
    .tasks-list {
      display: grid;
      grid-template-columns: 1fr;
      gap: 20px;
    }
    .task-item {
      border-left: 5px solid #ccc;
    }
    .priority-low { color: green; }
    .priority-medium { color: orange; }
    .priority-high { color: red; }
    .priority-critical { color: purple; font-weight: bold; }
  `]
})
export class TasksComponent implements OnInit {
  tasks: Task[] = [];
  priorities = Object.values(Priority);
  currentTask: Task = this.initNewTask();
  editingTask = false;

  constructor(private taskService: TaskService, private dialog: MatDialog) {}

  ngOnInit() {
    this.loadTasks();
  }

  loadTasks() {
    this.taskService.getTasks().subscribe(tasks => this.tasks = tasks);
  }

  initNewTask(): Task {
    return {
      title: '',
      description: '',
      dueDate: '',
      priority: Priority.MEDIUM
    };
  }

  onSubmit() {
    const taskToSave = { ...this.currentTask };
    if (taskToSave.dueDate) {
      const d = new Date(taskToSave.dueDate);
      if (!isNaN(d.getTime())) {
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        taskToSave.dueDate = `${year}-${month}-${day}`;
      }
    }

    if (this.editingTask && taskToSave.id) {
      this.taskService.updateTask(taskToSave.id, taskToSave).subscribe(() => {
        this.loadTasks();
        this.cancelEdit();
      });
    } else {
      this.taskService.createTask(taskToSave).subscribe(() => {
        this.loadTasks();
        this.currentTask = this.initNewTask();
      });
    }
  }

  editTask(task: Task) {
    this.currentTask = { ...task };
    this.editingTask = true;
  }

  cancelEdit() {
    this.currentTask = this.initNewTask();
    this.editingTask = false;
  }

  deleteTask(task: Task) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent);

    dialogRef.afterClosed().subscribe(result => {
      if (result && task.id) {
        this.taskService.deleteTask(task.id).subscribe(() => {
          this.loadTasks();
        });
      }
    });
  }
}
