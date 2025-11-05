import { inject, Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Injectable()
export class AuthErrorInterceptor implements HttpInterceptor {
  private auth = inject(AuthService);
  private router = inject(Router);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((err: any) => {
        if (err instanceof HttpErrorResponse) {
          // Handle authentication errors (401/403) - redirect to login
          if (err.status === 401 || err.status === 403) {
            try { this.auth.logout(); } catch (e) { }
            this.router.navigate(['/login']);
          }
          // Handle other HTTP errors - redirect to error page
          else if (err.status >= 400) {
            const errorMessage = this.extractErrorMessage(err);
            const errorDetails = err.error?.error || err.error?.details || err.message || '';
            
            this.router.navigate(['/error'], {
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
  }

  private extractErrorMessage(err: HttpErrorResponse): string {
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
}
