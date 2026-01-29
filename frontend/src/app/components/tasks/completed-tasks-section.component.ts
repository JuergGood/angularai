import { Component, Input } from '@angular/core';
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
        @for (task of tasks; track task.id) {
          <mat-list-item>
            <mat-icon matListItemIcon>task_alt</mat-icon>
            <div matListItemTitle [style.text-decoration]="'line-through'">{{ task.title }}</div>
            @if (task.completedAt) {
              <div matListItemLine>
                {{ 'TASKS.COMPLETED_AT' | translate }}: {{ task.completedAt | date:'short' }}
              </div>
            }
          </mat-list-item>
        }
      </mat-list>
    </mat-expansion-panel>
  `,
  styles: [`
    .completed-panel {
      margin-top: 24px;
      border: 1px solid var(--border);
      background: var(--surface) !important;
      box-shadow: none !important;
    }
    mat-panel-title {
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--text-muted);
    }
    body.theme-dark .completed-panel {
      border-color: rgba(255, 255, 255, 0.1);
    }
    :host ::ng-deep .mat-expansion-panel-content {
      background: var(--surface) !important;
      color: var(--text) !important;
    }
  `]
})
export class CompletedTasksSectionComponent {
  @Input() tasks: Task[] = [];
}
