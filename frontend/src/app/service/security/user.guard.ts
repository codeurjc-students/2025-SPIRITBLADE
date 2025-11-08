import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { AuthService } from '../auth.service';

/**
 * User Guard - Only allows access to users with USER role (not ADMIN).
 * Admins should login as regular users if they want to access user features.
 * Redirects admins to admin panel.
 */
export const UserGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.checkSession().pipe(
    map(authenticated => {
      if (!authenticated) {
        // Not authenticated - redirect to login
        return router.parseUrl('/login');
      }
      
      if (auth.isAdmin()) {
        // Is admin - redirect to admin panel (admins can't access user features)
        console.warn('Access denied: User account required. Admins must login as regular users to access this feature.');
        return router.parseUrl('/admin');
      }
      
      // Is regular user - allow access
      return true;
    })
  );
};
