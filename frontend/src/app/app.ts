import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `
    <header>
      <h1>User Management System</h1>
    </header>
    <main>
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    header { background-color: #333; color: white; padding: 1rem; text-align: center; }
    main { padding: 20px; }
  `]
})
export class App {
  protected readonly title = signal('frontend');
}
