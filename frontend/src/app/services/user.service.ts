import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = '/api/users';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    const auth = this.authService.getAuthHeader();
    return new HttpHeaders({
      'Authorization': 'Basic ' + auth,
      'Content-Type': 'application/json'
    });
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`, { headers: this.getHeaders() });
  }

  updateCurrentUser(user: User): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/me`, user, { headers: this.getHeaders() });
  }
}
