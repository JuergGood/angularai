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
  isInitializing = signal<boolean>(false);

  constructor(private http: HttpClient) {}

  init() {
    this.isInitializing.set(true);
    // Check if user is already logged in via session cookie
    this.http.get<User>(`${this.apiUrl}/info`).subscribe({
      next: (user) => {
        this.currentUser.set(user);
        this.isInitializing.set(false);
      },
      error: () => {
        this.currentUser.set(null);
        this.isInitializing.set(false);
      }
    });
  }

  login(login: string, password: string): Observable<User> {
    const authString = `${login}:${password}`;
    // Use a robust way to encode to Base64 that handles non-ASCII characters
    const encodedAuth = btoa(unescape(encodeURIComponent(authString)));

    const headers = new HttpHeaders({
      'Authorization': 'Basic ' + encodedAuth
    });
    return this.http.post<User>(`${this.apiUrl}/login`, {}, { headers }).pipe(
      tap(user => {
        this.currentUser.set(user);
      })
    );
  }

  register(user: User): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/register`, user);
  }

  resendVerification(email: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/resend-verification`, null, {
      params: { email },
      responseType: 'text'
    });
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/forgot-password`, { email });
  }

  resetPassword(payload: { token: string; password?: string }): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/reset-password`, payload);
  }

  logout() {
    this.http.post(`${this.apiUrl}/logout`, {}).subscribe({
      next: () => this.currentUser.set(null),
      error: (err) => {
        console.error('Logout error:', err);
        // Still clear user locally even if server call fails
        this.currentUser.set(null);
      }
    });
  }

  isLoggedIn(): boolean {
    return this.currentUser() !== null;
  }

  isAdmin(): boolean {
    const role = this.currentUser()?.role;
    return role === 'ROLE_ADMIN' || role === 'ROLE_ADMIN_READ';
  }

  hasAdminWriteAccess(): boolean {
    return this.currentUser()?.role === 'ROLE_ADMIN';
  }
}
