export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH'
}

export interface Task {
  id?: number;
  title: string;
  description: string;
  dueDate: string;
  priority: Priority;
}
