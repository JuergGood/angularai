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
    </mat-form-field>
  `,
  styles: [`
    .quick-add-field {
      width: 100%;
      margin-bottom: 24px;
    }
    .quick-add-field ::ng-deep .mat-mdc-form-field-subscript-wrapper {
      display: none;
    }
  `]
})
export class QuickAddTaskComponent {
  titleControl = new FormControl('', { nonNullable: true });

  constructor(private taskService: TaskService) {}

  submit() {
    const title = this.titleControl.value.trim();
    if (title) {
      this.taskService.quickAdd(title).subscribe({
        next: () => this.titleControl.reset()
      });
    }
  }
}
