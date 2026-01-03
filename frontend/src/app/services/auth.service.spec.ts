import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { User } from '../models/user.model';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    // Mock localStorage on window object
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
      expect(localStorage.getItem('auth')).toBeTruthy();
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockUser);
  });

  it('should logout and clear current user', () => {
    service.currentUser.set({ login: 'test' } as User);
    localStorage.setItem('auth', 'some-token');

    service.logout();

    expect(service.currentUser()).toBeNull();
    expect(localStorage.getItem('auth')).toBeNull();
  });
});
