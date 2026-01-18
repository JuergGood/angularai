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

  constructor(private http: HttpClient) {}

  getSystemInfo(): Observable<SystemInfo> {
    return this.http.get<SystemInfo>(`${this.apiUrl}/info`);
  }
}
