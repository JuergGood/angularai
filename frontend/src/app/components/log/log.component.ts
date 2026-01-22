import { Component, OnInit, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { LogService } from '../../services/log.service';
import { ActionLog } from '../../models/action-log.model';
import { ConfirmDialogComponent } from '../tasks/confirm-dialog.component';
import { AuthService } from '../../services/auth.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-log',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    TranslateModule
  ],
  templateUrl: './log.component.html',
  styleUrls: ['./log.component.css']
})
export class LogComponent implements OnInit {
  logs = signal<ActionLog[]>([]);
  totalElements = signal(0);
  pageSize = signal(10);
  currentPage = signal(0);
  sortField = 'timestamp';
  sortDirection = 'desc';

  filterType = 'all';
  startDate: Date | null = null;
  endDate: Date | null = null;

  displayedColumns: string[] = ['timestamp', 'login', 'action', 'details', 'metadata'];

  /**
   * Action presentation: semantic chip colors and labels.
   */
  private readonly actionLabelMap: Record<string, string> = {
    USER_LOGIN: 'Login',
    USER_LOGOUT: 'Logout',
    TASK_CREATED: 'Task created',
    TASK_UPDATED: 'Task updated',
    TASK_COMPLETED: 'Task completed',
    USER_CREATED: 'User created',
    USER_UPDATED: 'User updated'
  };

  private readonly actionClassMap: Record<string, string> = {
    USER_LOGIN: 'action-login',
    USER_LOGOUT: 'action-logout',
    TASK_CREATED: 'action-neutral',
    TASK_UPDATED: 'action-neutral',
    TASK_COMPLETED: 'action-neutral',
    USER_CREATED: 'action-neutral',
    USER_UPDATED: 'action-neutral'
  };

  getActionLabel(action: string | undefined | null): string {
    if (!action) return '';
    return this.actionLabelMap[action] ?? action.replaceAll('_', ' ').toLowerCase().replace(/^\w/, c => c.toUpperCase());
  }

  getActionClass(action: string | undefined | null): string {
    if (!action) return 'action-neutral';
    return this.actionClassMap[action] ?? 'action-neutral';
  }

  constructor(
    private logService: LogService,
    private dialog: MatDialog,
    public authService: AuthService
  ) {}

  ngOnInit() {
    this.loadLogs();
  }

  loadLogs() {
    const sort = `${this.sortField},${this.sortDirection}`;
    const startStr = this.startDate ? this.startDate.toISOString() : undefined;
    const endStr = this.endDate ? this.endDate.toISOString() : undefined;

    this.logService.getLogs(
      this.currentPage(),
      this.pageSize(),
      sort,
      this.filterType,
      startStr,
      endStr
    ).subscribe(response => {
      this.logs.set(response.content);
      this.totalElements.set(response.totalElements);
    });
  }

  onPageChange(event: PageEvent) {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadLogs();
  }

  onSortChange(sort: Sort) {
    this.sortField = sort.active;
    this.sortDirection = sort.direction || 'desc';
    this.currentPage.set(0);
    this.loadLogs();
  }

  onFilterChange() {
    this.currentPage.set(0);
    this.loadLogs();
  }

  clearFilter() {
    this.filterType = 'all';
    this.startDate = null;
    this.endDate = null;
    this.currentPage.set(0);
    this.loadLogs();
  }

  clearLog() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent);

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.logService.clearLogs().subscribe(() => {
          this.currentPage.set(0);
          this.loadLogs();
        });
      }
    });
  }
}
