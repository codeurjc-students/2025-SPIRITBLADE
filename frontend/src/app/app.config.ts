import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authErrorInterceptor } from './service/security/auth-error.interceptor';
import { authInterceptor } from './service/security/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([
        authInterceptor,      // First: adds auth headers and credentials
        authErrorInterceptor  // Second: handles errors and redirects
      ])
    )
  ]
};
