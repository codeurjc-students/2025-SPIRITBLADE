import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthErrorInterceptor } from './service/security/auth-error.interceptor';
import { authInterceptor } from './service/security/auth.interceptor';
import { AuthGuard } from './service/security/auth.guard';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authInterceptor])
    ),
    { provide: HTTP_INTERCEPTORS, useClass: AuthErrorInterceptor, multi: true },
    AuthGuard
  ]

};
