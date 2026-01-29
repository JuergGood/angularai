import { Component, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
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
  templateUrl: './quick-add-task.component.html',
  styleUrl: './quick-add-task.component.css'
})
export class QuickAddTaskComponent implements OnInit, OnDestroy {
  titleControl = new FormControl('', { nonNullable: true });
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
    if (!value) {
      this.errorMessage.set('TASKS.ERROR_TITLE_REQUIRED');
      return;
    }

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
