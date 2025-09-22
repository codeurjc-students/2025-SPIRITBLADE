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
          if (err.status === 401 || err.status === 403) {
            try { this.auth.logout(); } catch (e) { }
            this.router.navigate(['/login']);
          }
        }
        return throwError(() => err);
      })
    );
  }
}
