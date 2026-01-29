import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { User } from '../models/user.model';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    try {
      TestBed.initTestEnvironment(
        BrowserDynamicTestingModule,
        platformBrowserDynamicTesting()
      );
    } catch (e) {
      // already initialized
    }

    // Mock localStorage
    const store: Record<string, string> = {};

    const mockStorage = {
      getItem: vi.fn((key) => store[key] || null),
      setItem: vi.fn((key, value) => store[key] = value),
      clear: vi.fn(() => { for (const key in store) delete store[key]; }),
      removeItem: vi.fn((key) => delete store[key]),
      length: 0,
      key: vi.fn((index) => Object.keys(store)[index] || null)
    };

    vi.stubGlobal('localStorage', mockStorage);

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should login and set current user', () => {
    const mockUser: User = { login: 'test', firstName: 'Test', email: 'test@example.com' } as User;
    service.login('test', 'password').subscribe(user => {
      expect(user).toEqual(mockUser);
      expect(service.currentUser()).toEqual(mockUser);
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockUser);
  });

  it('should logout and clear current user', () => {
    service.currentUser.set({ login: 'test' } as User);

    service.logout();

    const req = httpMock.expectOne('/api/auth/logout');
    expect(req.request.method).toBe('POST');
    req.flush({});

    expect(service.currentUser()).toBeNull();
  });

  it('should initialize and restore session', () => {
    const mockUser: User = { login: 'test', firstName: 'Test', role: 'ROLE_USER' } as User;

    service.init();
    expect(service.isInitializing()).toBe(true);

    const req = httpMock.expectOne('/api/auth/info');
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);

    expect(service.currentUser()).toEqual(mockUser);
    expect(service.isInitializing()).toBe(false);
  });

  it('should handle init error', () => {
    service.init();

    const req = httpMock.expectOne('/api/auth/info');
    req.error(new ProgressEvent('error'));

    expect(service.currentUser()).toBeNull();
    expect(service.isInitializing()).toBe(false);
  });

  it('should register user', () => {
    const mockUser: User = { login: 'new', firstName: 'New' } as User;
    service.register(mockUser).subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne('/api/auth/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockUser);
    req.flush(mockUser);
  });

  it('should check permissions', () => {
    service.currentUser.set({ role: 'ROLE_ADMIN' } as User);
    expect(service.isLoggedIn()).toBe(true);
    expect(service.isAdmin()).toBe(true);
    expect(service.hasAdminWriteAccess()).toBe(true);

    service.currentUser.set({ role: 'ROLE_ADMIN_READ' } as User);
    expect(service.isAdmin()).toBe(true);
    expect(service.hasAdminWriteAccess()).toBe(false);

    service.currentUser.set({ role: 'ROLE_USER' } as User);
    expect(service.isAdmin()).toBe(false);
    expect(service.hasAdminWriteAccess()).toBe(false);

    service.currentUser.set(null);
    expect(service.isLoggedIn()).toBe(false);
    expect(service.isAdmin()).toBe(false);
  });
});
