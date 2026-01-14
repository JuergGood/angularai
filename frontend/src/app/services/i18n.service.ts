import { Injectable, signal, effect } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

export type Language = 'en' | 'de-ch';

@Injectable({
  providedIn: 'root'
})
export class I18nService {
  currentLang = signal<Language>('en');

  constructor(private translate: TranslateService) {
    const savedLang = localStorage.getItem('lang') as Language;
    if (savedLang) {
      this.currentLang.set(savedLang);
    }

    this.translate.addLangs(['en', 'de-ch']);
    this.translate.setDefaultLang('en');
    this.translate.use(this.currentLang());

    effect(() => {
      const lang = this.currentLang();
      this.translate.use(lang);
      localStorage.setItem('lang', lang);
    });
  }

  setLanguage(lang: Language) {
    this.currentLang.set(lang);
  }

  getCurrentLanguage(): Language {
    return this.currentLang();
  }
}
