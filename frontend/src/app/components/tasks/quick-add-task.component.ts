import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { TaskService } from '../../services/task.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-quick-add-task',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    TranslateModule
  ],
  template: `
    <mat-form-field appearance="outline" class="quick-add-field">
      <mat-icon matPrefix>add</mat-icon>
      <mat-label>{{ 'TASKS.QUICK_ADD_PLACEHOLDER' | translate }}</mat-label>
      <input matInput [formControl]="titleControl" (keydown.enter)="submit()" [placeholder]="'TASKS.QUICK_ADD_HINT' | translate">
      <mat-hint>{{ 'TASKS.QUICK_ADD_HELP' | translate }}</mat-hint>
    </mat-form-field>
  `,
  styles: [`
    .quick-add-field {
      width: 100%;
      margin-bottom: 32px;
    }
  `]
})
export class QuickAddTaskComponent {
  titleControl = new FormControl('', { nonNullable: true });

  constructor(private taskService: TaskService) {}

  submit() {
    const value = this.titleControl.value.trim();
    if (!value) return;

    // Parsing logic: Title | Description | Due Date | Priority | Status
    // Separators: | or ;
    const parts = value.split(/[|;]/).map(p => p.trim());

    if (parts.length === 1) {
      // Simple title-only add
      this.taskService.quickAdd(value).subscribe({
        next: () => this.titleControl.reset()
      });
    } else {
      // Detailed add
      const [title, description, dueDate, priority, status] = parts;

      const task: any = { title };
      if (description) task.description = description;
      if (dueDate && /^\d{4}-\d{2}-\d{2}$/.test(dueDate)) task.dueDate = dueDate;
      if (priority) {
        const p = priority.toUpperCase();
        if (['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].includes(p)) {
          task.priority = p;
        }
      }
      if (status) {
        const s = status.toUpperCase();
        if (['OPEN', 'IN_PROGRESS', 'DONE', 'ARCHIVED'].includes(s)) {
          task.status = s;
        }
      }

      this.taskService.createTask(task).subscribe({
        next: () => this.titleControl.reset()
      });
    }
  }
}
