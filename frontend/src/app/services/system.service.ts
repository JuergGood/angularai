import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { I18nService } from './i18n.service';

export interface SystemInfo {
  backendVersion: string;
  frontendVersion: string;
  mode: string;
  landingMessage?: string;
}

@Injectable({
  providedIn: 'root'
})
export class SystemService {
  private apiUrl = '/api/system';
  private adminUrl = '/api/admin/settings';

  constructor(
    private http: HttpClient,
    private i18nService: I18nService
  ) {}

  getSystemInfo(): Observable<SystemInfo> {
    const headers = new HttpHeaders({
      'Accept-Language': this.i18nService.currentLang()
    });
    return this.http.get<SystemInfo>(`${this.apiUrl}/info`, { headers });
  }

  getRecaptchaSiteKey(): Observable<string> {
    return this.http.get(`${this.apiUrl}/recaptcha-site-key`, { responseType: 'text' });
  }

  getGeolocationEnabled(): Observable<{enabled: boolean}> {
    return this.http.get<{enabled: boolean}>(`${this.adminUrl}/geolocation`);
  }

  setGeolocationEnabled(enabled: boolean): Observable<void> {
    return this.http.post<void>(`${this.adminUrl}/geolocation`, { enabled });
  }

  getRecaptchaConfigIndex(): Observable<{index: number}> {
    return this.http.get<{index: number}>(`${this.adminUrl}/recaptcha`);
  }

  setRecaptchaConfigIndex(index: number): Observable<void> {
    return this.http.post<void>(`${this.adminUrl}/recaptcha`, { index });
  }

  getLandingMessageEnabled(): Observable<{enabled: boolean}> {
    return this.http.get<{enabled: boolean}>(`${this.adminUrl}/landing-message`);
  }

  setLandingMessageEnabled(enabled: boolean): Observable<void> {
    return this.http.post<void>(`${this.adminUrl}/landing-message`, { enabled });
  }
}
