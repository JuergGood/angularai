import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ActionLogResponse } from '../models/action-log.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class LogService {
  private apiUrl = '/api/admin/logs';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getHeaders(): HttpHeaders {
    const auth = this.authService.getAuthHeader();
    return new HttpHeaders({
      'Authorization': 'Basic ' + auth,
      'Content-Type': 'application/json'
    });
  }

  getLogs(page: number, size: number, sort: string, type?: string, startDate?: string, endDate?: string): Observable<ActionLogResponse> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (type && type !== 'all') {
      params = params.set('type', type);
    }
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }

    return this.http.get<ActionLogResponse>(this.apiUrl, {
      params,
      headers: this.getHeaders()
    });
  }

  clearLogs(): Observable<void> {
    return this.http.delete<void>(this.apiUrl, { headers: this.getHeaders() });
  }
}
