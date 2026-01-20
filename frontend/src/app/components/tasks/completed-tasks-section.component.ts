import { Component, Input, Output, EventEmitter, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { Task } from '../../models/task.model';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-completed-tasks-section',
  standalone: true,
  imports: [CommonModule, MatExpansionModule, MatListModule, MatIconModule, TranslateModule],
  template: `
    <mat-expansion-panel class="completed-panel" [expanded]="false">
      <mat-expansion-panel-header>
        <mat-panel-title>
          <mat-icon>check_circle</mat-icon>
          <span>{{ 'TASKS.COMPLETED_RECENTLY' | translate }} ({{ tasks.length }})</span>
        </mat-panel-title>
      </mat-expansion-panel-header>

      <mat-list>
        <mat-list-item *ngFor="let task of tasks">
          <mat-icon matListItemIcon>task_alt</mat-icon>
          <div matListItemTitle [style.text-decoration]="'line-through'">{{ task.title }}</div>
          <div matListItemLine *ngIf="task.completedAt">
            {{ 'TASKS.COMPLETED_AT' | translate }}: {{ task.completedAt | date:'short' }}
          </div>
        </mat-list-item>
      </mat-list>
    </mat-expansion-panel>
  `,
  styles: [`
    .completed-panel {
      margin-top: 24px;
      border: 1px solid #e5e7eb;
      box-shadow: none !important;
    }
    mat-panel-title {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #6b7280;
    }
  `]
})
export class CompletedTasksSectionComponent {
  @Input() tasks: Task[] = [];
}
