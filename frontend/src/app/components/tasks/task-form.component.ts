import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { TranslateModule } from '@ngx-translate/core';
import { Task, Priority, TaskStatus } from '../../models/task.model';

@Component({
  selector: 'app-task-form',
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
    TranslateModule
  ],
  template: `
    <mat-card class="add-task-card">
      <mat-card-header>
        <mat-card-title>{{ (isEditing ? 'TASKS.EDIT_TASK' : 'TASKS.ADD_TASK') | translate }}</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <form (ngSubmit)="onSubmit()" #taskForm="ngForm">
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>{{ 'TASKS.FORM.TITLE' | translate }}</mat-label>
            <input matInput name="title" [(ngModel)]="task.title" required placeholder="{{ 'TASKS.FORM.TITLE_PH' | translate }}">
          </mat-form-field>

          <mat-form-field appearance="outline" class="full-width">
            <mat-label>{{ 'TASKS.FORM.DESCRIPTION' | translate }}</mat-label>
            <textarea matInput name="description" [(ngModel)]="task.description" required rows="3"></textarea>
          </mat-form-field>

          <div class="form-grid">
            <mat-form-field appearance="outline">
              <mat-label>{{ 'TASKS.FORM.DUE_DATE' | translate }}</mat-label>
              <input matInput [matDatepicker]="picker" name="dueDate" [(ngModel)]="task.dueDate">
              <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
              <mat-datepicker #picker></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>{{ 'TASKS.FORM.PRIORITY' | translate }}</mat-label>
              <mat-select name="priority" [(ngModel)]="task.priority" required>
                @for (p of priorities; track p) {
                  <mat-option [value]="p">{{ 'TASKS.PRIORITY.' + p | translate }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>{{ 'TASKS.FORM.STATUS' | translate }}</mat-label>
              <mat-select name="status" [(ngModel)]="task.status" required>
                @for (s of statuses; track s) {
                  <mat-option [value]="s">{{ 'TASKS.STATUS.' + s | translate }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
          </div>

          <div class="form-actions">
            <button mat-flat-button color="primary" type="submit" [disabled]="!taskForm.form.valid" class="submit-btn">
              {{ (isEditing ? 'TASKS.UPDATE_TASK' : 'TASKS.ADD_TASK') | translate }}
            </button>
            <button mat-button type="button" (click)="onCancel()">{{ 'TASKS.CANCEL' | translate }}</button>
          </div>
        </form>
      </mat-card-content>
    </mat-card>
  `,
  styleUrl: './tasks.component.css'
})
export class TaskFormComponent {
  @Input({ required: true }) task!: Task;
  @Input() isEditing = false;

  @Output() save = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  priorities = Object.values(Priority);
  statuses = Object.values(TaskStatus);

  onSubmit() {
    this.save.emit();
  }

  onCancel() {
    this.cancel.emit();
  }
}
