import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

/**
 * HTTP Error Interceptor - handles authentication and other HTTP errors
 * 
 * 401/403 Unauthorized: Logs out user and redirects to login
 * Other 4xx/5xx errors: Redirects to error page with details
 */
export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const auth = inject(AuthService);

  return next(req).pipe(
    catchError((err: any) => {
      if (err instanceof HttpErrorResponse) {
        // 401 Unauthorized: User is not logged in - redirect to login
        if (err.status === 401) {
          console.warn('🔒 Not authenticated, redirecting to login...');
          try { 
            auth.logout(); 
          } catch (e) {
            console.error('Error during logout:', e);
          }
          router.navigate(['/login'], {
            queryParams: { returnUrl: router.url }
          });
        }
        // 403 Forbidden: User is logged in but lacks permissions - redirect to error page
        else if (err.status === 403) {
          const errorMessage = extractErrorMessage(err);
          const errorDetails = err.error?.error || err.error?.details || err.message || 'You do not have permission to access this resource';
          
          console.warn(`🚫 Access forbidden (403): ${errorMessage}`);
          
          router.navigate(['/error'], {
            queryParams: {
              code: 403,
              message: errorMessage,
              details: errorDetails
            }
          });
        }
        // Handle other HTTP errors - redirect to error page
        else if (err.status >= 400) {
          const errorMessage = extractErrorMessage(err);
          const errorDetails = err.error?.error || err.error?.details || err.message || '';
          
          console.error(`❌ HTTP Error ${err.status}: ${errorMessage}`);
          
          router.navigate(['/error'], {
            queryParams: {
              code: err.status,
              message: errorMessage,
              details: errorDetails
            }
          });
        }
      }
      return throwError(() => err);
    })
  );
};

/**
 * Extract error message from HTTP error response
 */
function extractErrorMessage(err: HttpErrorResponse): string {
  // Try to extract message from different possible locations in the error response
  if (err.error?.message) {
    return err.error.message;
  }
  if (typeof err.error === 'string') {
    return err.error;
  }
  
  // Default messages based on status code
  switch (err.status) {
    case 400:
      return 'Bad Request';
    case 401:
      return 'Unauthorized - Please login';
    case 403:
      return 'Forbidden - Access Denied';
    case 404:
      return 'Resource Not Found';
    case 500:
      return 'Internal Server Error';
    case 503:
      return 'Service Unavailable';
    default:
      return 'An error occurred';
  }
}
