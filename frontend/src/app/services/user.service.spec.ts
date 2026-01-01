import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserService } from './user.service';
import { AuthService } from './auth.service';
import { User } from '../models/user.model';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;
  let authServiceSpy: any;

  beforeEach(() => {
    authServiceSpy = {
      getAuthHeader: vi.fn().mockReturnValue('mock-token')
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        UserService,
        { provide: AuthService, useValue: authServiceSpy }
      ]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get current user', () => {
    const mockUser: User = { login: 'test', firstName: 'Test', email: 'test@example.com' } as User;
    service.getCurrentUser().subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne('/api/users/me');
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe('Basic mock-token');
    req.flush(mockUser);
  });

  it('should update current user', () => {
    const mockUser: User = { login: 'test', firstName: 'Updated', email: 'updated@example.com' } as User;
    service.updateCurrentUser(mockUser).subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne('/api/users/me');
    expect(req.request.method).toBe('PUT');
    req.flush(mockUser);
  });
});
