import { Task } from './task.model';
import { ActionLog } from './action-log.model';
import { User } from './user.model';

export interface DashboardData {
  summary: SummaryStats;
  priorityTasks: Task[];
  recentActivity: ActionLog[];
  recentUsers: User[];
  taskDistribution: TaskStatusDistribution;
}

export interface SummaryStats {
  openTasks: number;
  openTasksDelta: number;
  activeUsers: number;
  activeUsersDelta: number;
  completedTasks: number;
  completedTasksDelta: number;
  todayLogs: number;
  todayLogsDelta: number;
}

export interface TaskStatusDistribution {
  open: number;
  inProgress: number;
  completed: number;
  total: number;
}
