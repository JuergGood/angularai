import { Injectable, signal } from '@angular/core';

/**
 * Token-based theme toggling.
 *
 * We keep Angular Material's prebuilt theme, and only switch app-level surfaces/text
 * via CSS variables (see src/styles.css). This keeps the change small and low-risk.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly storageKey = 'goodone.theme';
  readonly isDark = signal(false);

  constructor() {
    const saved = this.safeGet();
    const prefersDark =
      typeof window !== 'undefined' &&
      !!window.matchMedia &&
      window.matchMedia('(prefers-color-scheme: dark)').matches;

    const dark = saved ? saved === 'dark' : prefersDark;
    this.setDark(dark, false);

    // Watch for system theme changes
    if (typeof window !== 'undefined' && !!window.matchMedia) {
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        // Only auto-update if the user hasn't explicitly set a preference in this session/localStorage
        if (!this.safeGet()) {
          this.setDark(e.matches, false);
        }
      });
    }
  }

  toggle(): void {
    this.setDark(!this.isDark());
  }

  setDark(dark: boolean, persist = true): void {
    this.isDark.set(dark);
    document.body.classList.toggle('theme-dark', dark);
    if (persist) this.safeSet(dark ? 'dark' : 'light');
  }

  private safeGet(): string | null {
    try {
      return localStorage.getItem(this.storageKey);
    } catch {
      return null;
    }
  }

  private safeSet(value: string): void {
    try {
      localStorage.setItem(this.storageKey, value);
    } catch {
      // ignore (private browsing / storage blocked)
    }
  }
}
