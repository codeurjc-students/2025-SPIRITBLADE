import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { AuthService } from '../auth.service';

/**
 * Admin Guard - Only allows access to users with ADMIN role.
 * Redirects non-admin users to dashboard.
 */
export const AdminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.checkSession().pipe(
    map(authenticated => {
      if (!authenticated) {
        // Not authenticated - redirect to login
        return router.parseUrl('/login');
      }
      
      if (auth.isAdmin()) {
        // Is admin - allow access
        return true;
      }
      
      // Is authenticated but not admin - redirect to dashboard
      console.warn('Access denied: Admin privileges required');
      return router.parseUrl('/dashboard');
    })
  );
};
