import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { SidenavComponent } from './sidenav.component';
import { AuthService } from '../../services/auth.service';
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { MatSnackBar } from '@angular/material/snack-bar';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TranslateModule } from '@ngx-translate/core';
import { SystemService } from '../../services/system.service';
import { of, Subject } from 'rxjs';
import { I18nService } from '../../services/i18n.service';
import { signal, NO_ERRORS_SCHEMA } from '@angular/core';

describe('SidenavComponent', () => {
  let authServiceSpy: any;
  let snackBarSpy: any;
  let systemServiceSpy: any;
  let i18nServiceSpy: any;
  let routerEvents: Subject<any>;
  let routerSpy: any;

  beforeEach(async () => {
    routerEvents = new Subject<any>();

    authServiceSpy = {
      isLoggedIn: vi.fn().mockReturnValue(true),
      isAdmin: vi.fn().mockReturnValue(false),
      logout: vi.fn(),
      currentUser: signal({ firstName: 'Test' })
    };

    snackBarSpy = {
      open: vi.fn()
    };

    systemServiceSpy = {
      getSystemInfo: vi.fn().mockReturnValue(of({
        backendVersion: '1.0.0',
        frontendVersion: '1.0.0',
        mode: 'test',
        landingMessage: 'Test Message'
      }))
    };

    i18nServiceSpy = {
      currentLang: signal('en'),
      setLanguage: vi.fn()
    };

    routerSpy = {
      events: routerEvents.asObservable(),
      navigate: vi.fn(),
      createUrlTree: vi.fn().mockReturnValue({}),
      serializeUrl: vi.fn().mockReturnValue(''),
      isActive: vi.fn().mockReturnValue(false)
    };

    await TestBed.configureTestingModule({
      imports: [SidenavComponent, TranslateModule.forRoot()],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: SystemService, useValue: systemServiceSpy },
        { provide: I18nService, useValue: i18nServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: { params: of({}) } },
        provideNoopAnimations()
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  it('should hide banner after 20 seconds', async () => {
    vi.useFakeTimers();
    const fixture = TestBed.createComponent(SidenavComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.showBanner()).toBe(true);

    vi.advanceTimersByTime(20000);
    expect(component.isHiding()).toBe(true);

    vi.advanceTimersByTime(500);
    expect(component.showBanner()).toBe(false);
    vi.useRealTimers();
  });

  it('should hide banner after 3 navigations', () => {
    const fixture = TestBed.createComponent(SidenavComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.showBanner()).toBe(true);

    routerEvents.next(new NavigationEnd(1, '/test1', '/test1'));
    routerEvents.next(new NavigationEnd(2, '/test2', '/test2'));
    expect(component.showBanner()).toBe(true);

    routerEvents.next(new NavigationEnd(3, '/test3', '/test3'));
    expect(component.isHiding()).toBe(true);
  });

  it('should hide banner when close is called', async () => {
    vi.useFakeTimers();
    const fixture = TestBed.createComponent(SidenavComponent);
    const component = fixture.componentInstance;
    fixture.detectChanges();

    component.hideBanner();
    expect(component.isHiding()).toBe(true);

    vi.advanceTimersByTime(500);
    expect(component.showBanner()).toBe(false);
    vi.useRealTimers();
  });
});
