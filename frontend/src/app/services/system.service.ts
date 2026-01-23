import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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

  constructor(private http: HttpClient) {}

  getSystemInfo(): Observable<SystemInfo> {
    return this.http.get<SystemInfo>(`${this.apiUrl}/info`);
  }

  getGeolocationEnabled(): Observable<{enabled: boolean}> {
    return this.http.get<{enabled: boolean}>(`${this.adminUrl}/geolocation`);
  }

  setGeolocationEnabled(enabled: boolean): Observable<void> {
    return this.http.post<void>(`${this.adminUrl}/geolocation`, { enabled });
  }
}
