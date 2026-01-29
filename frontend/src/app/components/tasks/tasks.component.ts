import { Component, OnInit, ChangeDetectorRef, effect, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatListModule } from '@angular/material/list';
import { provideNativeDateAdapter } from '@angular/material/core';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { Task, Priority, TaskStatus } from '../../models/task.model';
import { TaskService, SmartFilter } from '../../services/task.service';
import { ConfirmDialogComponent } from './confirm-dialog.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { QuickAddTaskComponent } from './quick-add-task.component';
import { TaskFilterChipsComponent } from './task-filter-chips.component';
import { CompletedTasksSectionComponent } from './completed-tasks-section.component';
import { TaskItemComponent } from './task-item.component';
import { TaskFormComponent } from './task-form.component';
import { formatRelativeDue, isOverdue } from '../../utils/date-utils';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatChipsModule,
    MatDialogModule,
    MatMenuModule,
    MatCheckboxModule,
    MatExpansionModule,
    MatListModule,
    DragDropModule,
    TranslateModule,
    QuickAddTaskComponent,
    TaskFilterChipsComponent,
    CompletedTasksSectionComponent,
    TaskItemComponent,
    TaskFormComponent
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './tasks.component.html',
  styleUrl: './tasks.component.css'
})
export class TasksComponent implements OnInit {
  tasks: Task[] = [];
  filteredTasks: Task[] = [];
  priorities = Object.values(Priority);
  statuses = Object.values(TaskStatus);
  currentTask: Task = this.initNewTask();
  editingTask = false;
  showForm = false;

  editingTitleId: number | null = null;
  activeFilter: SmartFilter = 'ALL';
  isOverdue = isOverdue;
  viewMode: 'COMFORTABLE' | 'COMPACT' = 'COMFORTABLE';

  activeActionsTaskId: number | null = null;

  selectedTaskIds = signal<Set<number>>(new Set<number>());

  completedRecently = computed(() =>
    this.taskService.tasks().filter(t => t.status === TaskStatus.DONE)
      .sort((a, b) => new Date(b.completedAt || 0).getTime() - new Date(a.completedAt || 0).getTime())
      .slice(0, 10)
  );

  filterStatus: string = 'ALL';

  constructor(
    public taskService: TaskService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef,
    private translate: TranslateService
  ) {
    effect(() => {
      this.filteredTasks = this.taskService.tasks();
      this.cdr.detectChanges();
    });
  }

  ngOnInit() {
    this.loadTasks();
  }

  loadTasks() {
    this.taskService.getTasks({ smartFilter: this.activeFilter }).subscribe();
  }

  newSet(): Set<number> {
    return new Set<number>();
  }

  onFilterChange(filter: SmartFilter): void {
    this.activeFilter = filter;
    this.selectedTaskIds.set(new Set<number>());
    this.loadTasks();
  }

  toggleTaskSelection(taskId: number | undefined): void {
    if (taskId === undefined) return;
    this.selectedTaskIds.update((ids: Set<number>) => {
      const newIds = new Set(ids);
      if (newIds.has(taskId)) {
        newIds.delete(taskId);
      } else {
        newIds.add(taskId);
      }
      return newIds;
    });
  }

  toggleViewMode(): void {
    this.viewMode = this.viewMode === 'COMFORTABLE' ? 'COMPACT' : 'COMFORTABLE';
  }

  toggleActions(event: MouseEvent, task: Task): void {
    // If clicking a button or checkbox, don't toggle row actions
    const target = event.target as HTMLElement;
    if (target.closest('button') || target.closest('mat-checkbox') || target.closest('input')) {
      return;
    }

    if (this.activeActionsTaskId === task.id) {
      this.activeActionsTaskId = null;
    } else {
      this.activeActionsTaskId = task.id || null;
    }
  }

  isTaskSelected(taskId: number | undefined): boolean {
    return taskId !== undefined && this.selectedTaskIds().has(taskId);
  }

  bulkUpdateStatus(status: TaskStatus): void {
    const ids: number[] = Array.from(this.selectedTaskIds());
    if (ids.length === 0) return;
    this.taskService.bulkPatchTasks(ids, { status }).subscribe({
      next: () => this.selectedTaskIds.set(new Set<number>())
    });
  }

  bulkUpdatePriority(priority: Priority): void {
    const ids: number[] = Array.from(this.selectedTaskIds());
    if (ids.length === 0) return;
    this.taskService.bulkPatchTasks(ids, { priority }).subscribe({
      next: () => this.selectedTaskIds.set(new Set<number>())
    });
  }

  toggleSelectAll(checked: boolean): void {
    if (checked) {
      const allIds = this.filteredTasks.map(t => t.id).filter((id): id is number => id !== undefined);
      this.selectedTaskIds.set(new Set(allIds));
    } else {
      this.selectedTaskIds.set(new Set<number>());
    }
  }

  isAllSelected(): boolean {
    if (this.filteredTasks.length === 0) return false;
    return this.filteredTasks.every(t => t.id !== undefined && this.selectedTaskIds().has(t.id));
  }

  isPartiallySelected(): boolean {
    const selectedCount = Array.from(this.selectedTaskIds()).filter(id =>
      this.filteredTasks.some(t => t.id === id)
    ).length;
    return selectedCount > 0 && selectedCount < this.filteredTasks.length;
  }

  bulkDelete(): void {
    const ids: number[] = Array.from(this.selectedTaskIds());
    if (ids.length === 0) return;

    this.translate.get('TASKS.DELETE_BULK_CONFIRM', { count: ids.length }).subscribe(msg => {
      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        data: { message: msg }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.taskService.bulkDeleteTasks(ids).subscribe({
            next: () => {
              this.selectedTaskIds.set(new Set<number>());
              this.loadTasks();
              this.cdr.detectChanges();
            }
          });
        }
      });
    });
  }

  startEditTitle(task: Task): void {
    if (task.id) {
      this.editingTitleId = task.id;
    }
  }

  saveTitle(task: Task, newTitle: string): void {
    if (task.id && newTitle.trim() && newTitle !== task.title) {
      this.taskService.patchTask(task.id, { title: newTitle.trim() }).subscribe();
    }
    this.editingTitleId = null;
  }

  toggleTaskDone(task: Task): void {
    if (task.id) {
      const newStatus = task.status === TaskStatus.DONE ? TaskStatus.OPEN : TaskStatus.DONE;

      // Optimistic UI update
      const oldStatus = task.status;
      task.status = newStatus;
      this.cdr.detectChanges();

      this.taskService.patchTask(task.id, { status: newStatus }).subscribe({
        error: () => {
          // Revert on error
          task.status = oldStatus;
          this.cdr.detectChanges();
        }
      });
    }
  }

  cycleStatus(task: Task, event: MouseEvent): void {
    event.stopPropagation();
    if (task.id) {
      const statusCycle = [TaskStatus.OPEN, TaskStatus.IN_PROGRESS, TaskStatus.DONE, TaskStatus.ARCHIVED];
      const currentIndex = statusCycle.indexOf(task.status);
      const nextIndex = (currentIndex + 1) % statusCycle.length;
      const newStatus = statusCycle[nextIndex];

      // Optimistic UI update
      const oldStatus = task.status;
      task.status = newStatus;
      this.cdr.detectChanges();

      this.taskService.patchTask(task.id, { status: newStatus }).subscribe({
        error: () => {
          // Revert on error
          task.status = oldStatus;
          this.cdr.detectChanges();
        }
      });
    }
  }

  setPriority(task: Task, priority: Priority): void {
    if (task.id) {
      this.taskService.patchTask(task.id, { priority }).subscribe();
    }
  }

  formatDate(date: string | null | undefined): string {
    return formatRelativeDue(date, this.translate);
  }

  applyFilter() {
    this.loadTasks();
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
      [Priority.CRITICAL]: 0,
      [Priority.HIGH]: 1,
      [Priority.MEDIUM]: 2,
      [Priority.LOW]: 3
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
