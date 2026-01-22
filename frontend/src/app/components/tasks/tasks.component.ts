import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { provideNativeDateAdapter } from '@angular/material/core';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { Task, Priority, TaskStatus } from '../../models/task.model';
import { TaskService } from '../../services/task.service';
import { ConfirmDialogComponent } from './confirm-dialog.component';
import { TranslateModule } from '@ngx-translate/core';

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
    MatChipsModule,
    MatDialogModule,
    DragDropModule,
    TranslateModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './tasks.component.html',
  styles: [`
    .tasks-container {
      max-width: 900px;
      margin: 0 auto;
    }
    .page-toolbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 32px;
      gap: 16px;
      flex-wrap: wrap;
    }
    .page-toolbar .left,
    .page-toolbar .right {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
      color: var(--text);
      letter-spacing: -0.5px;
    }
    .add-task-btn {
      border-radius: 10px;
      padding: 0 20px;
      height: 44px !important;
      font-weight: 500;
    }
    .filter-select {
      width: 180px;
    }
    .reset-sort-btn {
      height: 44px !important;
      border-radius: 10px !important;
      border-color: var(--border) !important;
      color: var(--text) !important;
      font-weight: 500;
    }
    .filter-select ::ng-deep .mat-mdc-form-field-subscript-wrapper {
      display: none;
    }
    .add-task-card {
      margin-bottom: 32px;
      border-radius: 12px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06) !important;
      border: 1px solid var(--border) !important;
      overflow: hidden;
      background: var(--surface) !important;
    }
    .add-task-card mat-card-header {
      background-color: var(--surface-2) !important;
      padding: 16px 24px;
      border-bottom: 1px solid var(--border);
    }
    .add-task-card mat-card-content {
      padding: 24px !important;
    }
    .form-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 16px;
      margin-top: 8px;
    }
    @media (max-width: 600px) {
      .form-grid {
        grid-template-columns: 1fr;
      }
    }
    .full-width {
      width: 100%;
    }
    .submit-btn {
      border-radius: 8px;
      padding: 0 24px;
      height: 44px !important;
      font-weight: 600;
    }
    .submit-btn:hover:not(:disabled) {
      transform: translateY(-1px);
    }
    .form-actions {
      display: flex;
      gap: 12px;
      margin-top: 24px;
      padding-top: 16px;
      border-top: 1px solid var(--border);
      justify-content: flex-end;
    }
    .tasks-list {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }
    .task-card {
      border-radius: var(--r);
      padding: 20px;
      border: 1px solid var(--border);
      box-shadow: var(--shadow-1);
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      position: relative;
      background: var(--surface);
    }
    .task-card:hover {
      box-shadow: var(--shadow-2);
      transform: translateY(-2px);
    }
    .task-card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 12px;
    }
    .task-main-info {
      display: flex;
      gap: 12px;
      align-items: flex-start;
    }
    .drag-handle {
      cursor: move;
      color: #9ca3af;
      margin-top: 2px;
      font-size: 20px;
      width: 20px;
      height: 20px;
    }
    .task-title {
      margin: 0 0 4px 0;
      font-size: 18px;
      font-weight: 600;
      color: var(--text);
      line-height: 1.4;
    }
    .task-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 13px;
      color: var(--text-muted);
    }
    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }
    .meta-item mat-icon {
      font-size: 14px;
      width: 14px;
      height: 14px;
    }
    .task-description {
      margin: 0;
      font-size: 15px;
      color: var(--text-muted);
      line-height: 1.6;
      max-width: 720px;
      white-space: pre-wrap;
    }
    .task-actions {
      position: absolute;
      top: 16px;
      right: 16px;
      display: flex;
      gap: 4px;
      opacity: 0;
      transition: opacity 0.2s ease;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 8px;
      padding: 2px;
    }
    .task-card:hover .task-actions {
      opacity: 1;
    }
    .task-actions button {
      width: 36px;
      height: 36px;
      line-height: 36px;
    }
    .task-actions mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    /* Chips */
    .priority-chip {
      padding: 2px 10px;
      border-radius: 12px;
      font-weight: 600;
      font-size: 11px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      border: 1px solid var(--border);
    }
    .priority-low { background: rgba(76, 175, 80, 0.16); color: #2e7d32; }
    .priority-medium { background: rgba(63, 81, 181, 0.14); color: var(--brand); }
    .priority-high { background: rgba(255, 152, 0, 0.16); color: #e65100; }
    .priority-critical { background: rgba(211, 47, 47, 0.12); color: #c62828; }

    .status-chip {
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      border: 1px solid var(--border);
    }
    .status-open {
      background: color-mix(in srgb, var(--surface-2) 70%, transparent);
      color: var(--text-muted);
    }
    .status-in_progress {
      background: rgba(63, 81, 181, 0.14);
      color: var(--brand);
    }
    .status-completed {
      background: rgba(76, 175, 80, 0.16);
      color: #2e7d32;
    }
    .status-closed {
      background: rgba(0, 150, 136, 0.1);
      color: #00796b;
    }

    .empty-message {
      text-align: center;
      padding: 40px;
      color: var(--text-muted);
      font-style: italic;
    }

    /* Drag and Drop */
    .cdk-drag-preview {
      box-sizing: border-box;
      border-radius: 12px;
      box-shadow: 0 5px 5px -3px rgba(0, 0, 0, 0.2),
                  0 8px 10px 1px rgba(0, 0, 0, 0.14),
                  0 3px 14px 2px rgba(0, 0, 0, 0.12);
    }
    .cdk-drag-placeholder {
      opacity: 0;
    }
    .cdk-drag-animating {
      transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
    }
    .tasks-list.cdk-drop-list-dragging .task-card:not(.cdk-drag-placeholder) {
      transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
    }
  `]
})
export class TasksComponent implements OnInit {
  tasks: Task[] = [];
  filteredTasks: Task[] = [];
  priorities = Object.values(Priority);
  statuses = Object.values(TaskStatus);
  currentTask: Task = this.initNewTask();
  editingTask = false;
  showForm = false;

  filterStatus: string = 'ALL';

  constructor(
    private taskService: TaskService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadTasks();
  }

  loadTasks() {
    this.taskService.getTasks().subscribe(tasks => {
      this.tasks = tasks;
      this.applyFilter();
      this.cdr.detectChanges();
    });
  }

  applyFilter() {
    if (this.filterStatus === 'ALL') {
      this.filteredTasks = [...this.tasks];
    } else {
      this.filteredTasks = this.tasks.filter(t => t.status === this.filterStatus);
    }
  }

  clearFilter() {
    this.filterStatus = 'ALL';
    this.applyFilter();
  }

  initNewTask(): Task {
    return {
      title: '',
      description: '',
      dueDate: '',
      priority: Priority.MEDIUM,
      status: TaskStatus.OPEN
    };
  }

  showAddTaskForm() {
    this.showForm = true;
    this.editingTask = false;
    this.currentTask = this.initNewTask();
    this.cdr.detectChanges();
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
        this.showForm = false;
        this.cdr.detectChanges();
      });
    }
  }

  editTask(task: Task) {
    this.currentTask = { ...task };
    this.editingTask = true;
    this.showForm = true;
    this.cdr.detectChanges();
  }

  cancelEdit() {
    this.currentTask = this.initNewTask();
    this.editingTask = false;
    this.showForm = false;
    this.cdr.detectChanges();
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

  drop(event: CdkDragDrop<Task[]>) {
    if (this.filterStatus !== 'ALL') {
      return; // Reordering only allowed when all tasks are shown
    }
    moveItemInArray(this.tasks, event.previousIndex, event.currentIndex);
    this.applyFilter();

    const taskIds = this.tasks.map(t => t.id).filter((id): id is number => id !== undefined);
    this.taskService.reorderTasks(taskIds).subscribe();
  }

  resetSorting() {
    const priorityMap: Record<Priority, number> = {
      [Priority.HIGH]: 0,
      [Priority.MEDIUM]: 1,
      [Priority.LOW]: 2
    };

    this.tasks.sort((a, b) => {
      const pA = priorityMap[a.priority];
      const pB = priorityMap[b.priority];
      if (pA !== pB) {
        return pA - pB;
      }
      // Secondary sort by due date
      if (a.dueDate && b.dueDate) {
        return a.dueDate.localeCompare(b.dueDate);
      }
      return 0;
    });

    this.applyFilter();
    const taskIds = this.tasks.map(t => t.id).filter((id): id is number => id !== undefined);
    this.taskService.reorderTasks(taskIds).subscribe();
  }
}
