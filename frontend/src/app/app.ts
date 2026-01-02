import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidenavComponent } from './components/layout/sidenav.component';

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
export class App {
  protected readonly title = signal('frontend');
}
