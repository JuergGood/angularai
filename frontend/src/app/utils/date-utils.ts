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

export function parseRelativeDate(input: string): string | null {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const text = input.toLowerCase().trim();

  if (text === 'today' || text === 'heute') {
    return formatDate(today);
  }
  if (text === 'tomorrow' || text === 'morgen') {
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    return formatDate(tomorrow);
  }
  if (text === 'übermorgen') {
    const dayAfterTomorrow = new Date(today);
    dayAfterTomorrow.setDate(today.getDate() + 2);
    return formatDate(dayAfterTomorrow);
  }
  if (text === 'yesterday' || text === 'gestern') {
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);
    return formatDate(yesterday);
  }
  if (text === 'week' || text === 'woche') {
    const nextWeek = new Date(today);
    nextWeek.setDate(today.getDate() + 7);
    return formatDate(nextWeek);
  }

  // Handle "X days" or "in X days" or "X tage"
  const daysMatch = text.match(/^(\d+)\s*(days?|tage?)$/) || text.match(/^in\s*(\d+)\s*(days?|tage?)$/) || text.match(/^(\d+)\s*(days?|tage?)$/);

  // More robust regex for tokens that might be part of a larger string
  const standaloneDaysMatch = text.match(/^(\d+)\s*(days?|tage?)$/) || text.match(/^in\s*(\d+)\s*(days?|tage?)$/);

  if (standaloneDaysMatch) {
    const days = parseInt(standaloneDaysMatch[1], 10);
    const futureDate = new Date(today);
    futureDate.setDate(today.getDate() + days);
    return formatDate(futureDate);
  }

  return null;
}

function formatDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}
