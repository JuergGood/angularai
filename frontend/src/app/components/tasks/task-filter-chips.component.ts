import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatChipsModule } from '@angular/material/chips';
import { SmartFilter } from '../../services/task.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-task-filter-chips',
  standalone: true,
  imports: [CommonModule, MatChipsModule, TranslateModule],
  template: `
    <mat-chip-listbox [value]="activeFilter" (change)="onFilterChange($event.value)">
      <mat-chip-option value="ALL">{{ 'TASKS.FILTERS.ALL' | translate }}</mat-chip-option>
      <mat-chip-option value="TODAY">{{ 'TASKS.FILTERS.TODAY' | translate }}</mat-chip-option>
      <mat-chip-option value="UPCOMING">{{ 'TASKS.FILTERS.UPCOMING' | translate }}</mat-chip-option>
      <mat-chip-option value="OVERDUE">{{ 'TASKS.FILTERS.OVERDUE' | translate }}</mat-chip-option>
      <mat-chip-option value="HIGH">{{ 'TASKS.FILTERS.HIGH_PRIORITY' | translate }}</mat-chip-option>
    </mat-chip-listbox>
  `,
  styles: [`
    mat-chip-listbox {
      margin-bottom: 16px;
    }
  `]
})
export class TaskFilterChipsComponent {
  @Input() activeFilter: SmartFilter = 'ALL';
  @Output() filterChange = new EventEmitter<SmartFilter>();

  onFilterChange(value: SmartFilter) {
    if (value) {
      this.filterChange.emit(value);
    }
  }
}
