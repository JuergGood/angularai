import { TestBed } from '@angular/core/testing';
import { I18nService } from './i18n.service';
import { TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';

describe('I18nService', () => {
  let service: I18nService;
  let translateServiceSpy: any;

  beforeEach(() => {
    translateServiceSpy = {
      addLangs: vi.fn(),
      setDefaultLang: vi.fn(),
      use: vi.fn().mockReturnValue(of({})),
      getBrowserLang: vi.fn().mockReturnValue('en'),
      currentLang: 'en'
    };

    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        I18nService,
        { provide: TranslateService, useValue: translateServiceSpy }
      ]
    });
  });

  it('should be created', () => {
    service = TestBed.inject(I18nService);
    expect(service).toBeTruthy();
  });

  it('should initialize with default language if nothing saved', () => {
    service = TestBed.inject(I18nService);
    expect(translateServiceSpy.addLangs).toHaveBeenCalledWith(['en', 'de-ch']);
    expect(translateServiceSpy.setDefaultLang).toHaveBeenCalledWith('en');
    expect(translateServiceSpy.use).toHaveBeenCalledWith('en');
  });

  it('should initialize with saved language', () => {
    localStorage.setItem('lang', 'de-ch');
    service = TestBed.inject(I18nService);
    expect(translateServiceSpy.use).toHaveBeenCalledWith('de-ch');
    expect(service.currentLang()).toBe('de-ch');
  });

  it('should switch language', async () => {
    service = TestBed.inject(I18nService);
    service.setLanguage('de-ch');

    expect(service.currentLang()).toBe('de-ch');
    // The effect might be async, but signal update is sync.
    // In tests, effects run when change detection or flush effects is called.
    // However, vitest-angular usually handles this.
    // Let's check signal first.
  });
});
