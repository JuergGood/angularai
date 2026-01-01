import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { AuthService } from './services/auth.service';
import { Router, provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { vi } from 'vitest';

describe('App', () => {
  let authServiceSpy: any;

  beforeEach(async () => {
    authServiceSpy = {
      isLoggedIn: vi.fn().mockReturnValue(true),
      logout: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        provideRouter(routes),
        provideAnimationsAsync()
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render title', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('mat-toolbar span')?.textContent).toContain('User Management System');
  });

  it('should logout and navigate to login', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    const router = TestBed.inject(Router);
    const authService = TestBed.inject(AuthService);
    const navigateSpy = vi.spyOn(router, 'navigate');
    const logoutSpy = vi.spyOn(authService, 'logout');

    app.onLogout();

    expect(logoutSpy).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
