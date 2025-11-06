import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Auth interceptor that adds Authorization header and withCredentials to requests.
 * 
 * PUBLIC ENDPOINTS (Home & Summoner - no auth required):
 * - /auth/login and /auth/register
 * - /summoners/** (all GET requests - League of Legends public data)
 * - /files/** (public avatar access)
 * 
 * PROTECTED ENDPOINTS (Dashboard & Admin - auth required):
 * - /dashboard/** (user's personal dashboard)
 * - /users/** (user profile management)
 * - /admin/** (admin panel)
 * 
 * For public endpoints: Only adds Authorization header if token exists
 * For protected endpoints: Adds both Authorization header and withCredentials
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Get token from localStorage
  const token = localStorage.getItem('accessToken');
  
  // Define public endpoints that DON'T require withCredentials
  const publicEndpoints = [
    '/auth/login', 
    '/auth/register',
    '/summoners/',  // All GET summoner endpoints (Home & Summoner components)
    '/files/'       // Public file access
  ];
  
  const isPublicEndpoint = publicEndpoints.some(endpoint => req.url.includes(endpoint));
  
  // Clone request with Authorization header and conditional withCredentials
  const clonedRequest = req.clone({
    // Add withCredentials only for protected endpoints (dashboard, users, admin)
    ...(!isPublicEndpoint && { withCredentials: true }),
    // Add Authorization header if token exists (for both public and protected)
    ...(token && {
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    })
  });
  
  return next(clonedRequest);
};
