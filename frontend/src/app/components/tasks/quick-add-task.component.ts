import { Component, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { TaskService } from '../../services/task.service';
import { TranslateModule } from '@ngx-translate/core';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-quick-add-task',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatTooltipModule,
    MatChipsModule,
    TranslateModule
  ],
  template: `
    <div class="quick-add-container">
      <mat-form-field appearance="outline" class="quick-add-field" [subscriptSizing]="'dynamic'">
        <mat-icon matPrefix>add</mat-icon>
        <mat-label>{{ 'TASKS.QUICK_ADD_PLACEHOLDER' | translate }}</mat-label>
        <input matInput [formControl]="titleControl"
               (keydown.enter)="submit()"
               [placeholder]="'TASKS.QUICK_ADD_HINT' | translate"
               [matTooltip]="'TASKS.QUICK_ADD_HELP_DETAILED' | translate"
               matTooltipPosition="above"
               matTooltipClass="quick-add-tooltip">
        <mat-hint>{{ 'TASKS.QUICK_ADD_HELP' | translate }}</mat-hint>
        @if (errorMessage()) {
          <mat-error>{{ errorMessage() | translate }}</mat-error>
        }
      </mat-form-field>

      @if (parsedTask(); as task) {
        @if (titleControl.value.trim()) {
          <div class="parsed-preview">
            <mat-chip-listbox>
              @if (task.title) {
                <mat-chip class="preview-chip title-chip">
                  <mat-icon matChipAvatar>title</mat-icon>
                  {{ task.title }}
                </mat-chip>
              }
              @if (task.description) {
                <mat-chip class="preview-chip desc-chip">
                  <mat-icon matChipAvatar>description</mat-icon>
                  {{ task.description }}
                </mat-chip>
              }
              @if (task.dueDate) {
                <mat-chip class="preview-chip date-chip">
                  <mat-icon matChipAvatar>calendar_today</mat-icon>
                  {{ task.dueDate }}
                </mat-chip>
              }
              @if (task.priority) {
                <mat-chip class="preview-chip prio-chip" [ngClass]="'prio-' + task.priority.toLowerCase()">
                  <mat-icon matChipAvatar>priority_high</mat-icon>
                  {{ 'TASKS.PRIORITY.' + task.priority | translate }}
                </mat-chip>
              }
              @if (task.status) {
                <mat-chip class="preview-chip status-chip" [ngClass]="'status-' + task.status.toLowerCase()">
                  <mat-icon matChipAvatar>info</mat-icon>
                  {{ 'TASKS.STATUS.' + task.status | translate }}
                </mat-chip>
              }
            </mat-chip-listbox>
          </div>
        }
      }
    </div>
  `,
  styles: [`
    .quick-add-container {
      margin-bottom: 24px;
    }
    .quick-add-field {
      width: 100%;
      margin-bottom: 8px;
    }
    .parsed-preview {
      display: flex;
      gap: 8px;
      margin-top: 4px;
      margin-bottom: 16px;
    }
    .preview-chip {
      font-size: 12px;
    }
    .title-chip { --mdc-chip-label-text-color: #4b5563; }
    .desc-chip { --mdc-chip-label-text-color: #6b7280; }
    .prio-low { --mdc-chip-label-text-color: #2e7d32; }
    .prio-medium { --mdc-chip-label-text-color: var(--brand); }
    .prio-high { --mdc-chip-label-text-color: #e65100; }
    .prio-critical { --mdc-chip-label-text-color: #c62828; }

    ::ng-deep .quick-add-tooltip {
      white-space: pre-line;
      font-size: 14px;
      padding: 12px;
    }
  `]
})
export class QuickAddTaskComponent implements OnInit, OnDestroy {
  titleControl = new FormControl('', { nonNullable: true, validators: [Validators.required] });
  errorMessage = signal<string | null>(null);
  parsedTask = signal<Task | null>(null);

  private destroy$ = new Subject<void>();

  constructor(private taskService: TaskService) {}

  ngOnInit() {
    this.titleControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(value => {
      this.analyze(value);
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private analyze(value: string) {
    const trimmed = value.trim();
    if (!trimmed) {
      this.parsedTask.set(null);
      return;
    }
    this.taskService.analyzeTask(trimmed).subscribe({
      next: (task) => this.parsedTask.set(task),
      error: () => this.parsedTask.set(null)
    });
  }

  submit() {
    this.errorMessage.set(null);
    const value = this.titleControl.value.trim();
    if (!value) return;

    const taskToCreate = this.parsedTask();
    if (!taskToCreate || !taskToCreate.title) {
      this.errorMessage.set('TASKS.ERROR_TITLE_REQUIRED');
      return;
    }

    this.taskService.createTask(taskToCreate).subscribe({
      next: () => {
        this.titleControl.reset();
        this.parsedTask.set(null);
        this.errorMessage.set(null);
        // Reset the form control state to pristine/untouched to avoid validation errors showing up
        this.titleControl.markAsPristine();
        this.titleControl.markAsUntouched();
      },
      error: () => this.errorMessage.set('COMMON.ERROR')
    });
  }
}
