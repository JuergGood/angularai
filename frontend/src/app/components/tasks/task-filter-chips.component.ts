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
    :host ::ng-deep .mat-mdc-chip-option {
      --mdc-chip-label-text-color: var(--text);
      --mdc-chip-elevated-unselected-container-color: var(--surface-2);
      --mdc-chip-flat-unselected-container-color: var(--surface-2);
      --mdc-chip-flat-unselected-outline-color: var(--border);
      background-color: var(--surface-2);
      border: 1px solid var(--border);
    }
    body.theme-dark :host ::ng-deep .mat-mdc-chip-option:not(.mdc-chip--selected) {
      background-color: var(--surface-2) !important;
      --mdc-chip-elevated-unselected-container-color: var(--surface-2) !important;
      --mdc-chip-flat-unselected-container-color: var(--surface-2) !important;
      --mdc-chip-label-text-color: var(--text) !important;
      --mdc-chip-unselected-flat-label-text-color: var(--text) !important;
      --mdc-chip-flat-unselected-outline-color: var(--border) !important;
    }
    body.theme-dark :host ::ng-deep .mat-mdc-chip-option:not(.mdc-chip--selected) .mdc-chip__container,
    body.theme-dark :host ::ng-deep .mat-mdc-chip-option:not(.mdc-chip--selected) .mdc-chip__content,
    :host ::ng-deep .mat-mdc-chip-option .mdc-chip__container,
    :host ::ng-deep .mat-mdc-chip-option .mdc-chip__content {
      background-color: inherit !important;
    }
    body.theme-dark :host ::ng-deep .mat-mdc-chip-option:not(.mdc-chip--selected) .mdc-chip__text-label,
    body.theme-dark :host ::ng-deep .mat-mdc-standard-chip:not(.mdc-chip--selected) .mdc-chip__text-label,
    body.theme-dark :host ::ng-deep .mat-mdc-chip .mdc-chip__text-label,
    :host ::ng-deep .mat-mdc-chip-option .mdc-chip__text-label {
      color: inherit !important;
      opacity: 1 !important;
    }
    :host ::ng-deep .mat-mdc-chip-option.mdc-chip--selected {
      --mdc-chip-elevated-selected-container-color: var(--brand);
      --mdc-chip-selected-label-text-color: #0b1220;
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
