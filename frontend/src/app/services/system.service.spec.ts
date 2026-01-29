import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { SystemService, SystemInfo } from './system.service';
import { AuthService } from './auth.service';
import { I18nService } from './i18n.service';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';

describe('SystemService', () => {
  let service: SystemService;
  let httpMock: HttpTestingController;
  let authServiceSpy: any;
  let i18nServiceSpy: any;

  beforeEach(() => {
    try {
      TestBed.initTestEnvironment(
        BrowserDynamicTestingModule,
        platformBrowserDynamicTesting()
      );
    } catch (e) {}

    authServiceSpy = {
      getAuthHeader: vi.fn().mockReturnValue('mock-token')
    };

    i18nServiceSpy = {
      currentLang: vi.fn().mockReturnValue('en')
    };

    TestBed.configureTestingModule({
      providers: [
        SystemService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: I18nService, useValue: i18nServiceSpy }
      ]
    });
    service = TestBed.inject(SystemService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get system info', () => {
    const mockInfo: SystemInfo = {
      backendVersion: '1.0.0',
      frontendVersion: '1.0.0',
      mode: 'DEV'
    };

    service.getSystemInfo().subscribe(info => {
      expect(info).toEqual(mockInfo);
    });

    const req = httpMock.expectOne('/api/system/info');
    expect(req.request.method).toBe('GET');
    req.flush(mockInfo);
  });

  it('should get geolocation enabled status', () => {
    const mockResponse = { enabled: true };

    service.getGeolocationEnabled().subscribe(response => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('/api/admin/settings/geolocation');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Basic mock-token');
    req.flush(mockResponse);
  });

  it('should set geolocation enabled status', () => {
    service.setGeolocationEnabled(true).subscribe();

    const req = httpMock.expectOne('/api/admin/settings/geolocation');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ enabled: true });
    expect(req.request.headers.get('Authorization')).toBe('Basic mock-token');
    req.flush(null);
  });
});
