import { describe, it, expect, vi } from 'vitest';
import { isOverdue, formatRelativeDue, parseRelativeDate } from './date-utils';

describe('DateUtils', () => {
  describe('isOverdue', () => {
    it('should return false if no dueDate', () => {
      expect(isOverdue(null)).toBe(false);
      expect(isOverdue(undefined)).toBe(false);
    });

    it('should return true if date is in the past', () => {
      const past = new Date();
      past.setDate(past.getDate() - 1);
      expect(isOverdue(past.toISOString().split('T')[0])).toBe(true);
    });

    it('should return false if date is today', () => {
      const today = new Date();
      expect(isOverdue(today.toISOString().split('T')[0])).toBe(false);
    });

    it('should return false if date is in the future', () => {
      const future = new Date();
      future.setDate(future.getDate() + 1);
      expect(isOverdue(future.toISOString().split('T')[0])).toBe(false);
    });
  });

  describe('formatRelativeDue', () => {
    const translateMock = {
      instant: vi.fn().mockImplementation((key, params) => {
        if (params && params.days) return `${key}:${params.days}`;
        return key;
      })
    };

    it('should return NO_DUE_DATE if no dueDate', () => {
      expect(formatRelativeDue(null, translateMock)).toBe('TASKS.NO_DUE_DATE');
    });

    it('should return TODAY for today', () => {
      const today = new Date().toISOString().split('T')[0];
      expect(formatRelativeDue(today, translateMock)).toBe('TASKS.DATES.TODAY');
    });

    it('should return TOMORROW for tomorrow', () => {
      const tomorrowDate = new Date();
      tomorrowDate.setDate(tomorrowDate.getDate() + 1);
      const tomorrow = tomorrowDate.toISOString().split('T')[0];
      expect(formatRelativeDue(tomorrow, translateMock)).toBe('TASKS.DATES.TOMORROW');
    });

    it('should return YESTERDAY for yesterday', () => {
      const yesterdayDate = new Date();
      yesterdayDate.setDate(yesterdayDate.getDate() - 1);
      const yesterday = yesterdayDate.toISOString().split('T')[0];
      expect(formatRelativeDue(yesterday, translateMock)).toBe('TASKS.DATES.YESTERDAY');
    });

    it('should return IN_DAYS for future dates', () => {
      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + 5);
      const future = futureDate.toISOString().split('T')[0];
      expect(formatRelativeDue(future, translateMock)).toBe('TASKS.DATES.IN_DAYS:5');
    });

    it('should return OVERDUE_DAYS for past dates', () => {
      const pastDate = new Date();
      pastDate.setDate(pastDate.getDate() - 5);
      const past = pastDate.toISOString().split('T')[0];
      expect(formatRelativeDue(past, translateMock)).toBe('TASKS.DATES.OVERDUE_DAYS:5');
    });
  });

  describe('parseRelativeDate', () => {
    const todayStr = new Date().toISOString().split('T')[0];

    it('should parse today/heute', () => {
      expect(parseRelativeDate('today')).toBe(todayStr);
      expect(parseRelativeDate('heute')).toBe(todayStr);
    });

    it('should parse tomorrow/morgen', () => {
      const tomorrowDate = new Date();
      tomorrowDate.setDate(tomorrowDate.getDate() + 1);
      const tomorrowStr = tomorrowDate.toISOString().split('T')[0];
      expect(parseRelativeDate('tomorrow')).toBe(tomorrowStr);
      expect(parseRelativeDate('morgen')).toBe(tomorrowStr);
    });

    it('should parse übermorgen', () => {
      const date = new Date();
      date.setDate(date.getDate() + 2);
      expect(parseRelativeDate('übermorgen')).toBe(date.toISOString().split('T')[0]);
    });

    it('should parse yesterday/gestern', () => {
      const date = new Date();
      date.setDate(date.getDate() - 1);
      expect(parseRelativeDate('yesterday')).toBe(date.toISOString().split('T')[0]);
      expect(parseRelativeDate('gestern')).toBe(date.toISOString().split('T')[0]);
    });

    it('should parse week/woche', () => {
      const date = new Date();
      date.setDate(date.getDate() + 7);
      expect(parseRelativeDate('week')).toBe(date.toISOString().split('T')[0]);
      expect(parseRelativeDate('woche')).toBe(date.toISOString().split('T')[0]);
    });

    it('should parse "X days" and "in X days"', () => {
      const date = new Date();
      date.setDate(date.getDate() + 3);
      const expected = date.toISOString().split('T')[0];
      expect(parseRelativeDate('3 days')).toBe(expected);
      expect(parseRelativeDate('in 3 days')).toBe(expected);
      expect(parseRelativeDate('3 tage')).toBe(expected);
    });

    it('should return null for unknown input', () => {
      expect(parseRelativeDate('unknown')).toBeNull();
      expect(parseRelativeDate('')).toBeNull();
    });
  });
});
