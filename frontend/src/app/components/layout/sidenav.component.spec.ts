import { TestBed } from '@angular/core/testing';
import { SidenavComponent } from './sidenav.component';
import { AuthService } from '../../services/auth.service';
import { Router, provideRouter } from '@angular/router';
import { routes } from '../../app.routes';
import { provideAnimations } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { vi } from 'vitest';

describe('SidenavComponent', () => {
  let authServiceSpy: any;
  let snackBarSpy: any;

  beforeEach(async () => {
    authServiceSpy = {
      isLoggedIn: vi.fn().mockReturnValue(true),
      isAdmin: vi.fn().mockReturnValue(true),
      logout: vi.fn()
    };

    snackBarSpy = {
      open: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [SidenavComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
        provideRouter(routes),
        provideAnimations()
      ]
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(SidenavComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });

  it('should logout and navigate to login', () => {
    const fixture = TestBed.createComponent(SidenavComponent);
    const component = fixture.componentInstance;
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate');

    // Manually inject mocks if needed, but they should be there from TestBed
    (component as any).authService = authServiceSpy;
    (component as any).snackBar = snackBarSpy;

    component.onLogout();

    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(snackBarSpy.open).toHaveBeenCalledWith('Logout successful', 'Close', { duration: 3000 });
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
