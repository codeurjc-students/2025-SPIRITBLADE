import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthErrorInterceptor } from './service/security/auth-error.interceptor';
import { AuthGuard } from './service/security/auth.guard';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    { provide: HTTP_INTERCEPTORS, useClass: AuthErrorInterceptor, multi: true }
    ,AuthGuard
  ]

};
