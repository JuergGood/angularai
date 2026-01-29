import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatMenuModule } from '@angular/material/menu';
import { TranslateModule } from '@ngx-translate/core';
import { Task, Priority } from '../../models/task.model';
import { isOverdue } from '../../utils/date-utils';

@Component({
  selector: 'app-task-item',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatMenuModule,
    TranslateModule
  ],
  template: `
    <mat-card class="task-card">
      <div class="task-row" [class.compact]="viewMode === 'COMPACT'" (click)="onToggleActions($event)" [class.has-actions]="isActiveActions">
        <div class="selection-box">
          <mat-checkbox [checked]="isSelected" (change)="onToggleSelection()"></mat-checkbox>
        </div>

        <button mat-icon-button (click)="onToggleDone($event)" class="task-status-btn" [class.done]="task.status === 'DONE'">
          <mat-icon>{{ task.status === 'DONE' ? 'task_alt' : 'radio_button_unchecked' }}</mat-icon>
        </button>

        <div class="task-actions">
          <button mat-icon-button (click)="onEdit($event)" [title]="'TASKS.EDIT_TASK' | translate">
            <mat-icon>edit</mat-icon>
          </button>
          <button mat-icon-button color="warn" (click)="onDelete($event)" [title]="'ADMIN.DELETE' | translate">
            <mat-icon>delete</mat-icon>
          </button>
        </div>

        <div class="task-title-container" (dblclick)="onStartEditTitle()">
          @if (isEditingTitle) {
            <input class="task-title-input" [value]="task.title" #titleInput
                   (keydown.enter)="onSaveTitle(titleInput.value)"
                   (blur)="onSaveTitle(titleInput.value)" autoFocus>
          } @else {
            <span class="task-title-text" [class.done]="task.status === 'DONE'">{{ task.title }}</span>
          }
        </div>

        <div class="task-meta-info">
          <span class="due-date-info" [class.overdue]="isOverdue(task.dueDate)">
            {{ formattedDate }}
          </span>

          <span class="priority-badge" [class]="'priority-' + task.priority.toLowerCase()" [matMenuTriggerFor]="prioMenu">
            {{ 'TASKS.PRIORITY.' + task.priority | translate }}
          </span>
          <mat-menu #prioMenu="matMenu">
            @for (p of priorities; track p) {
              <button mat-menu-item (click)="onSetPriority(p)">{{ 'TASKS.PRIORITY.' + p | translate }}</button>
            }
          </mat-menu>

          <span class="status-pill" [class]="'status-' + task.status.toLowerCase()" (click)="onCycleStatus($event)">
            {{ 'TASKS.STATUS.' + task.status | translate }}
          </span>
        </div>
      </div>
    </mat-card>
  `,
  styleUrl: './tasks.component.css'
})
export class TaskItemComponent {
  @Input({ required: true }) task!: Task;
  @Input() viewMode: 'COMFORTABLE' | 'COMPACT' = 'COMFORTABLE';
  @Input() isSelected = false;
  @Input() isActiveActions = false;
  @Input() isEditingTitle = false;
  @Input() formattedDate = '';

  @Output() toggleSelection = new EventEmitter<void>();
  @Output() toggleDone = new EventEmitter<void>();
  @Output() edit = new EventEmitter<void>();
  @Output() delete = new EventEmitter<void>();
  @Output() startEditTitle = new EventEmitter<void>();
  @Output() saveTitle = new EventEmitter<string>();
  @Output() setPriority = new EventEmitter<Priority>();
  @Output() cycleStatus = new EventEmitter<MouseEvent>();
  @Output() toggleActions = new EventEmitter<MouseEvent>();

  priorities = Object.values(Priority);

  isOverdue = isOverdue;

  onToggleSelection() {
    this.toggleSelection.emit();
  }

  onToggleDone(event: MouseEvent) {
    event.stopPropagation();
    this.toggleDone.emit();
  }

  onEdit(event: MouseEvent) {
    event.stopPropagation();
    this.edit.emit();
  }

  onDelete(event: MouseEvent) {
    event.stopPropagation();
    this.delete.emit();
  }

  onStartEditTitle() {
    this.startEditTitle.emit();
  }

  onSaveTitle(newTitle: string) {
    this.saveTitle.emit(newTitle);
  }

  onSetPriority(priority: Priority) {
    this.setPriority.emit(priority);
  }

  onCycleStatus(event: MouseEvent) {
    this.cycleStatus.emit(event);
  }

  onToggleActions(event: MouseEvent) {
    this.toggleActions.emit(event);
  }
}
