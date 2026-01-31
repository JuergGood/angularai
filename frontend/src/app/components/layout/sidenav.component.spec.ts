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
        backendVersion: '1.0.6',
        frontendVersion: '1.0.6',
        mode: 'test',
        landingMessage: 'Test Message'
      })),
      getGeolocationEnabled: vi.fn().mockReturnValue(of({ enabled: true })),
      setGeolocationEnabled: vi.fn().mockReturnValue(of(null)),
      getRecaptchaConfigIndex: vi.fn().mockReturnValue(of({ index: 1 })),
      setRecaptchaConfigIndex: vi.fn().mockReturnValue(of(null)),
      getLandingMessageEnabled: vi.fn().mockReturnValue(of({ enabled: true })),
      setLandingMessageEnabled: vi.fn().mockReturnValue(of(null))
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

  it('should logout', () => {
    const fixture = TestBed.createComponent(SidenavComponent);
    const component = fixture.componentInstance;
    component.onLogout();
    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should set language', () => {
    const fixture = TestBed.createComponent(SidenavComponent);
    const component = fixture.componentInstance;

    component.setLanguage('de-ch');
    expect(i18nServiceSpy.setLanguage).toHaveBeenCalledWith('de-ch');
  });

  it('should toggle geolocation', () => {
    const fixture = TestBed.createComponent(SidenavComponent);
    const component = fixture.componentInstance;

    component.geolocationEnabled.set(false);
    component.toggleGeolocation();

    expect(systemServiceSpy.setGeolocationEnabled).toHaveBeenCalledWith(true);
    expect(component.geolocationEnabled()).toBe(true);
  });

  it('should toggle landing message', () => {
    const fixture = TestBed.createComponent(SidenavComponent);
    const component = fixture.componentInstance;

    component.landingMessageEnabled.set(true);
    component.toggleLandingMessage();

    expect(systemServiceSpy.setLandingMessageEnabled).toHaveBeenCalledWith(false);
    expect(component.landingMessageEnabled()).toBe(false);
  });

  it('should set recaptcha config', () => {
    const fixture = TestBed.createComponent(SidenavComponent);
    const component = fixture.componentInstance;

    component.setRecaptchaConfig(2);

    expect(systemServiceSpy.setRecaptchaConfigIndex).toHaveBeenCalledWith(2);
    expect(component.recaptchaConfigIndex()).toBe(2);
  });

  it('should show help dialog', () => {
    const fixture = TestBed.createComponent(SidenavComponent);
    const component = fixture.componentInstance;
    const dialogOpenSpy = vi.spyOn(component['dialog'], 'open');

    component.showHelp();
    expect(dialogOpenSpy).toHaveBeenCalled();
  });
});
