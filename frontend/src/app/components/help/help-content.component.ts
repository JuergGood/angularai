import { Component, OnInit, inject, signal, effect, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { TranslateModule } from '@ngx-translate/core';
import { I18nService } from '../../services/i18n.service';

@Component({
  selector: 'app-help-content',
  standalone: true,
  imports: [CommonModule, MatCardModule, TranslateModule],
  templateUrl: './help-content.component.html',
  styles: [`
    .help-container {
      max-width: 900px;
      margin: 0 auto;
    }
    .help-card {
      /* Padding is now handled by mat-card-content */
    }
    ::ng-deep .help-content h1 { font-size: 2.2rem; margin-bottom: 1.5rem; border-bottom: 1px solid var(--border); padding-bottom: 0.5rem; color: var(--text); }
    ::ng-deep .help-content h2 { font-size: 1.8rem; margin-top: 2rem; margin-bottom: 1rem; border-bottom: 1px solid var(--border); color: var(--text); }
    ::ng-deep .help-content h3 { font-size: 1.4rem; margin-top: 1.5rem; color: var(--text); }
    ::ng-deep .help-content p { line-height: 1.6; margin-bottom: 1rem; color: var(--text); }
    ::ng-deep .help-content ul, ::ng-deep .help-content ol { margin-bottom: 1rem; padding-left: 2rem; color: var(--text); }
    ::ng-deep .help-content li { margin-bottom: 0.5rem; }
    ::ng-deep .help-content code { background-color: var(--surface-2); padding: 2px 4px; border-radius: 4px; font-family: monospace; color: var(--brand); }
    ::ng-deep .help-content pre { background-color: var(--surface-2); padding: 1rem; border-radius: 8px; overflow-x: auto; margin-bottom: 1rem; border: 1px solid var(--border); }
    ::ng-deep .help-content pre code { background-color: transparent; padding: 0; color: var(--text); }
    ::ng-deep .help-content blockquote { border-left: 4px solid var(--brand); padding-left: 1rem; margin-left: 0; color: var(--text-muted); font-style: italic; }
    ::ng-deep .help-content a { color: var(--brand); text-decoration: none; cursor: pointer; }
    ::ng-deep .help-content a:hover { text-decoration: underline; }
  `]
})
export class HelpContentComponent implements OnInit, AfterViewChecked {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private i18nService = inject(I18nService);
  private el = inject(ElementRef);

  htmlContent = signal<string>('');
  loading = signal<boolean>(true);
  private currentPageId = '';

  constructor() {
    effect(() => {
      // Reload content when language changes
      const lang = this.i18nService.currentLang();
      if (this.currentPageId) {
        this.loadHelpPage(this.currentPageId, lang);
      }
    });
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.currentPageId = params['pageId'];
      this.loadHelpPage(this.currentPageId, this.i18nService.getCurrentLanguage());
    });
  }

  ngAfterViewChecked() {
    this.handleLinks();
  }

  loadHelpPage(pageId: string, lang: string) {
    this.loading.set(true);
    this.http.get<{ [key: string]: string }>(`./assets/help/help-data-${lang}.json`).subscribe({
      next: (data) => {
        this.htmlContent.set(data[pageId] || `<p>Page not found (${lang}).</p>`);
        this.loading.set(false);
      },
      error: () => {
        this.htmlContent.set('<p>Error loading help content.</p>');
        this.loading.set(false);
      }
    });
  }

  private handleLinks() {
    const links = this.el.nativeElement.querySelectorAll('.help-content a');
    links.forEach((link: HTMLAnchorElement) => {
      if (!link.onclick) {
        link.onclick = (event: MouseEvent) => {
          const href = link.getAttribute('href');
          if (href) {
            if (href.startsWith('#')) {
              event.preventDefault();
              const element = this.el.nativeElement.querySelector(href);
              if (element) {
                element.scrollIntoView({ behavior: 'smooth' });
              }
            } else if (href.startsWith('doc/') || href === 'README.md' || href === 'README_de.md' || href.startsWith('scripts/')) {
              event.preventDefault();
              let pageId = '';
              if (href.includes('user-guide')) pageId = 'user-guide';
              else if (href.includes('admin-guide')) pageId = 'admin-guide';
              else if (href.includes('faq')) pageId = 'faq';
              else if (href.includes('release-notes')) pageId = 'release-notes';
              else if (href.includes('README')) pageId = 'readme';
              else if (href.includes('android-build-instructions')) pageId = 'android-build-instructions';
              else if (href.includes('aws_setup')) pageId = 'aws-setup';
              else if (href.includes('postgres_setup')) pageId = 'postgres-setup';
              else if (href.includes('aws_fargate_config')) pageId = 'aws-fargate-config';
              else if (href.includes('aws_create_target_group')) pageId = 'aws-create-target-group';
              else if (href.includes('aws_alb_troubleshooting')) pageId = 'aws-alb-troubleshooting';
              else if (href.includes('aws_ecs_push_instructions')) pageId = 'aws-ecs-push-instructions';
              else if (href.includes('md_to_confluence')) pageId = 'md-to-confluence';
              else if (href.includes('confluence')) pageId = 'md-to-confluence';

              if (pageId) {
                this.router.navigate(['/help', pageId]);
              }
            } else if (!href.startsWith('http') && !href.startsWith('mailto')) {
              // Handle other local relative links that might not be explicitly mapped
              event.preventDefault();
              console.log('Intercepted unmapped relative link:', href);
            }
          }
        };
      }
    });
  }
}
