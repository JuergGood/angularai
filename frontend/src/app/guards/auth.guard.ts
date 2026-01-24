import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { toObservable } from '@angular/core/rxjs-interop';
import { filter, map, take } from 'rxjs';

export const authGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  if (authService.isInitializing()) {
    return toObservable(authService.isInitializing).pipe(
      filter(initializing => !initializing),
      take(1),
      map(() => {
        return authService.isLoggedIn() ? true : router.parseUrl('/login');
      })
    );
  }

  return router.parseUrl('/login');
};
