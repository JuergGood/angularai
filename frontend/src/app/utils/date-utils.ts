export function isOverdue(dueDate: string | null | undefined): boolean {
  if (!dueDate) return false;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const due = new Date(dueDate);
  return due < today;
}

export function formatRelativeDue(dueDate: string | null | undefined, translate: any): string {
  if (!dueDate) return translate.instant('TASKS.NO_DUE_DATE');

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const due = new Date(dueDate);
  due.setHours(0, 0, 0, 0);

  const diffTime = due.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return translate.instant('TASKS.DATES.TODAY');
  if (diffDays === 1) return translate.instant('TASKS.DATES.TOMORROW');
  if (diffDays === -1) return translate.instant('TASKS.DATES.YESTERDAY');

  if (diffDays > 0) {
    return translate.instant('TASKS.DATES.IN_DAYS', { days: diffDays });
  } else {
    return translate.instant('TASKS.DATES.OVERDUE_DAYS', { days: Math.abs(diffDays) });
  }
}
