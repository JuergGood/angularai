import { TestBed } from '@angular/core/testing';
import { SidenavComponent } from './sidenav.component';
import { AuthService } from '../../services/auth.service';
import { Router, provideRouter } from '@angular/router';
import { routes } from '../../app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { vi } from 'vitest';

describe('SidenavComponent', () => {
  let authServiceSpy: any;

  beforeEach(async () => {
    authServiceSpy = {
      isLoggedIn: vi.fn().mockReturnValue(true),
      isAdmin: vi.fn().mockReturnValue(true),
      logout: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [SidenavComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        provideRouter(routes),
        provideAnimationsAsync()
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
    const authService = TestBed.inject(AuthService);
    const navigateSpy = vi.spyOn(router, 'navigate');
    const logoutSpy = vi.spyOn(authService, 'logout');

    component.onLogout();

    expect(logoutSpy).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
