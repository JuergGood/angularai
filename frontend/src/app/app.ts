import { Component, signal, OnInit } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { SidenavComponent } from './components/layout/sidenav.component';
import { AuthService } from './services/auth.service';
import { filter } from 'rxjs';

declare var gtag: Function;

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    SidenavComponent
  ],
  templateUrl: './app.component.html',
  styles: []
})
export class App implements OnInit {
  constructor(private authService: AuthService, private router: Router) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      if (typeof gtag === 'function') {
        gtag('config', 'G-N366JDK5K0', {
          'page_path': event.urlAfterRedirects
        });
      }
    });
  }

  ngOnInit() {
    this.authService.init();
  }

  protected readonly title = signal('frontend');
}
