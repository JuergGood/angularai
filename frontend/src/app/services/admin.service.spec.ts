import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
import { AuthService } from './auth.service';
import { User } from '../models/user.model';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;
  let authServiceSpy: any;

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

    TestBed.configureTestingModule({
      providers: [
        AdminService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceSpy }
      ]
    });
    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should get users', () => {
    const mockUsers: User[] = [{ id: 1, login: 'admin' } as User];
    service.getUsers().subscribe(users => expect(users).toEqual(mockUsers));
    const req = httpMock.expectOne('/api/admin/users');
    expect(req.request.method).toBe('GET');
    req.flush(mockUsers);
  });

  it('should create user', () => {
    const user = { login: 'new' } as User;
    service.createUser(user).subscribe(u => expect(u.login).toBe('new'));
    const req = httpMock.expectOne('/api/admin/users');
    expect(req.request.method).toBe('POST');
    req.flush({ ...user, id: 2 });
  });

  it('should update user', () => {
    const user = { id: 1, login: 'updated' } as User;
    service.updateUser(1, user).subscribe(u => expect(u).toEqual(user));
    const req = httpMock.expectOne('/api/admin/users/1');
    expect(req.request.method).toBe('PUT');
    req.flush(user);
  });

  it('should delete user', () => {
    service.deleteUser(1).subscribe();
    const req = httpMock.expectOne('/api/admin/users/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
