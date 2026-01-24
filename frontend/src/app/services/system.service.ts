import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface SystemInfo {
  backendVersion: string;
  frontendVersion: string;
  mode: string;
}

@Injectable({
  providedIn: 'root'
})
export class SystemService {
  private apiUrl = '/api/system';
  private adminUrl = '/api/admin/settings';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    const auth = this.authService.getAuthHeader();
    return new HttpHeaders({
      'Authorization': 'Basic ' + auth,
      'Content-Type': 'application/json'
    });
  }

  getSystemInfo(): Observable<SystemInfo> {
    return this.http.get<SystemInfo>(`${this.apiUrl}/info`);
  }

  getRecaptchaSiteKey(): Observable<string> {
    return this.http.get(`${this.apiUrl}/recaptcha-site-key`, { responseType: 'text' });
  }

  getGeolocationEnabled(): Observable<{enabled: boolean}> {
    return this.http.get<{enabled: boolean}>(`${this.adminUrl}/geolocation`, { headers: this.getHeaders() });
  }

  setGeolocationEnabled(enabled: boolean): Observable<void> {
    return this.http.post<void>(`${this.adminUrl}/geolocation`, { enabled }, { headers: this.getHeaders() });
  }
}
