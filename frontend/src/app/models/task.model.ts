export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export enum TaskStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  DONE = 'DONE',
  ARCHIVED = 'ARCHIVED'
}

export interface Task {
  id?: number;
  title: string;
  description: string;
  dueDate: string;
  priority: Priority;
  status: TaskStatus;
  position?: number;
  createdAt?: string;
  updatedAt?: string;
  completedAt?: string;
  tags?: string[];
}
