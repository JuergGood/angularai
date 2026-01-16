import { TestBed } from '@angular/core/testing';
import { SidenavComponent } from './sidenav.component';
import { AuthService } from '../../services/auth.service';
import { Router, provideRouter } from '@angular/router';
import { routes } from '../../app.routes';
import { provideAnimations } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';
import { TranslateModule } from '@ngx-translate/core';
import { SystemService } from '../../services/system.service';
import { of } from 'rxjs';
import { I18nService } from '../../services/i18n.service';
import { signal } from '@angular/core';

describe('SidenavComponent', () => {
  let authServiceSpy: any;
  let snackBarSpy: any;
  let systemServiceSpy: any;
  let i18nServiceSpy: any;

  beforeEach(async () => {
    try {
      TestBed.initTestEnvironment(
        BrowserDynamicTestingModule,
        platformBrowserDynamicTesting()
      );
    } catch (e) {
      // already initialized
    }

    authServiceSpy = {
      isLoggedIn: vi.fn().mockReturnValue(true),
      isAdmin: vi.fn().mockReturnValue(true),
      logout: vi.fn(),
      init: vi.fn(),
      currentUser: vi.fn().mockReturnValue({ firstName: 'Test' })
    };

    snackBarSpy = {
      open: vi.fn()
    };

    systemServiceSpy = {
      getSystemInfo: vi.fn().mockReturnValue(of({ version: '1.0.0', environment: 'test' }))
    };

    i18nServiceSpy = {
      currentLang: signal('en'),
      setLanguage: vi.fn(),
      getCurrentLanguage: vi.fn().mockReturnValue('en')
    };

    await TestBed.configureTestingModule({
      imports: [SidenavComponent, TranslateModule.forRoot()],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: SystemService, useValue: systemServiceSpy },
        { provide: I18nService, useValue: i18nServiceSpy },
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
    vi.spyOn(router, 'navigate');

    // Sidenav uses snackBar directly, but we provide a mock in TestBed
    // The previous failure was likely because detectChanges was needed or the injection was stale
    fixture.detectChanges();
    component.onLogout();

    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(snackBarSpy.open).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
