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

  constructor(private http: HttpClient) {
    const auth = localStorage.getItem('auth');
    if (auth) {
      this.isInitializing.set(true);
      // Basic validation/restore session
      this.http.post<User>(`${this.apiUrl}/login`, {}, {
        headers: new HttpHeaders({ 'Authorization': 'Basic ' + auth })
      }).subscribe({
        next: (user) => {
          this.currentUser.set(user);
          this.isInitializing.set(false);
        },
        error: () => {
          this.logout();
          this.isInitializing.set(false);
        }
      });
    }
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
        localStorage.setItem('auth', encodedAuth);
      })
    );
  }

  register(user: User): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/register`, user);
  }

  logout() {
    this.http.post(`${this.apiUrl}/logout`, {}).subscribe();
    this.currentUser.set(null);
    localStorage.removeItem('auth');
  }

  getAuthHeader(): string | null {
    return localStorage.getItem('auth');
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
