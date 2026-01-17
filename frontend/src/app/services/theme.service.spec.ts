import { ThemeService } from './theme.service';
import { beforeEach, describe, expect, it, vi } from 'vitest';

describe('ThemeService', () => {
  let service: ThemeService;

  beforeEach(() => {
    // Clear localStorage
    localStorage.clear();
    // Reset body classes
    document.body.className = '';

    service = new ThemeService();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should toggle theme', () => {
    const initial = service.isDark();
    service.toggle();
    expect(service.isDark()).toBe(!initial);
    expect(document.body.classList.contains('theme-dark')).toBe(!initial);
  });

  it('should set dark theme', () => {
    service.setDark(true);
    expect(service.isDark()).toBe(true);
    expect(document.body.classList.contains('theme-dark')).toBe(true);
    expect(localStorage.getItem('goodone.theme')).toBe('dark');
  });

  it('should set light theme', () => {
    service.setDark(false);
    expect(service.isDark()).toBe(false);
    expect(document.body.classList.contains('theme-dark')).toBe(false);
    expect(localStorage.getItem('goodone.theme')).toBe('light');
  });

  it('should initialize from localStorage', () => {
    localStorage.setItem('goodone.theme', 'dark');
    const newService = new ThemeService();
    expect(newService.isDark()).toBe(true);
    expect(document.body.classList.contains('theme-dark')).toBe(true);
  });
});
