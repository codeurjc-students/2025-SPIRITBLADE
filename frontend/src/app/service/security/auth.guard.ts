import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { AuthService } from '../auth.service';

/**
 * Generic Auth Guard - Only checks if user is authenticated.
 * For role-specific guards, use AdminGuard or UserGuard.
 */
export const AuthGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.checkSession().pipe(
    map(ok => (ok ? true : router.parseUrl('/login')))
  );
};
