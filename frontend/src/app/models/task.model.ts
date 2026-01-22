export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH'
}

export enum TaskStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CLOSED = 'CLOSED'
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
}
