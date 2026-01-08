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
    DragDropModule
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './tasks.component.html',
  styles: [`
    .tasks-container {
      max-width: 800px;
      margin: 0 auto;
      padding: 20px;
    }
    .tasks-header {
      margin: 20px 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 16px;
    }
    .filter-actions {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    .page-title {
      margin-top: 0;
      margin-bottom: 20px;
    }
    .add-task-card {
      margin-bottom: 30px;
    }
    mat-form-field {
      width: 100%;
      margin-bottom: 10px;
    }
    .form-row {
      display: flex;
      gap: 16px;
    }
    .form-actions {
      display: flex;
      gap: 10px;
    }
    .tasks-list {
      display: grid;
      grid-template-columns: 1fr;
      gap: 16px;
    }
    .task-item {
      border-left: 5px solid #ccc;
      cursor: move;
    }
    .cdk-drag-preview {
      box-sizing: border-box;
      border-radius: 4px;
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
    .tasks-list.cdk-drop-list-dragging .task-item:not(.cdk-drag-placeholder) {
      transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
    }
    .drag-handle {
      cursor: move;
      color: #999;
      margin-right: 8px;
    }
    .task-header-content {
      display: flex;
      align-items: center;
      width: 100%;
    }
    mat-card-actions {
      padding: 8px 16px;
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
    .priority-low { color: green; }
    .priority-medium { color: orange; }
    .priority-high { color: red; }
    .priority-critical { color: purple; font-weight: bold; }

    .status-open { background-color: #e0e0e0 !important; color: #333 !important; }
    .status-in_progress { background-color: #2196f3 !important; color: white !important; }
    .status-closed { background-color: #4caf50 !important; color: white !important; }
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
