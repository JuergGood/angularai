import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-help-content',
  standalone: true,
  imports: [CommonModule, MatCardModule, TranslateModule],
  templateUrl: './help-content.component.html',
  styles: [`
    .help-container {
      padding: 24px;
      max-width: 900px;
      margin: 0 auto;
    }
    .help-card {
      padding: 16px;
    }
    ::ng-deep .help-content h1 { font-size: 2.2rem; margin-bottom: 1.5rem; border-bottom: 1px solid #eee; padding-bottom: 0.5rem; }
    ::ng-deep .help-content h2 { font-size: 1.8rem; margin-top: 2rem; margin-bottom: 1rem; border-bottom: 1px solid #f0f0f0; }
    ::ng-deep .help-content h3 { font-size: 1.4rem; margin-top: 1.5rem; }
    ::ng-deep .help-content p { line-height: 1.6; margin-bottom: 1rem; color: #333; }
    ::ng-deep .help-content ul, ::ng-deep .help-content ol { margin-bottom: 1rem; padding-left: 2rem; }
    ::ng-deep .help-content li { margin-bottom: 0.5rem; }
    ::ng-deep .help-content code { background-color: #f5f5f5; padding: 2px 4px; border-radius: 4px; font-family: monospace; }
    ::ng-deep .help-content pre { background-color: #f5f5f5; padding: 1rem; border-radius: 8px; overflow-x: auto; margin-bottom: 1rem; }
    ::ng-deep .help-content pre code { background-color: transparent; padding: 0; }
    ::ng-deep .help-content blockquote { border-left: 4px solid #ddd; padding-left: 1rem; margin-left: 0; color: #666; font-style: italic; }
  `]
})
export class HelpContentComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);

  htmlContent = signal<string>('');
  loading = signal<boolean>(true);

  ngOnInit() {
    this.route.params.subscribe(params => {
      const pageId = params['pageId'];
      this.loadHelpPage(pageId);
    });
  }

  loadHelpPage(pageId: string) {
    this.loading.set(true);
    this.http.get<{ [key: string]: string }>('./assets/help/help-data.json').subscribe({
      next: (data) => {
        this.htmlContent.set(data[pageId] || '<p>Page not found.</p>');
        this.loading.set(false);
      },
      error: () => {
        this.htmlContent.set('<p>Error loading help content.</p>');
        this.loading.set(false);
      }
    });
  }
}
