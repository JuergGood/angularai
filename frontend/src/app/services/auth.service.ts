import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/auth';
  currentUser = signal<User | null>(null);

  constructor(private http: HttpClient) {}

  login(login: string, password: string): Observable<User> {
    const headers = new HttpHeaders({
      authorization: 'Basic ' + btoa(login + ':' + password)
    });
    return this.http.post<User>(`${this.apiUrl}/login`, {}, { headers }).pipe(
      tap(user => {
        this.currentUser.set(user);
        localStorage.setItem('auth', btoa(login + ':' + password));
      })
    );
  }

  register(user: User): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/register`, user);
  }

  logout() {
    this.currentUser.set(null);
    localStorage.removeItem('auth');
  }

  getAuthHeader(): string | null {
    return localStorage.getItem('auth');
  }

  isLoggedIn(): boolean {
    return this.currentUser() !== null || this.getAuthHeader() !== null;
  }

  isAdmin(): boolean {
    return this.currentUser()?.role === 'ROLE_ADMIN';
  }
}
