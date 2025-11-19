import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

interface LoginRequest { username: string; password: string }
interface LoginResponse { 
  status: string;
  message: string;
  accessToken?: string;
  refreshToken?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private authState = new BehaviorSubject<boolean>(false);
  public readonly isAuthenticated$ = this.authState.asObservable();
  private currentUser: { username?: string; roles?: string[] } | null = null;

  isAdmin(): boolean {
    return !!this.currentUser && 
           Array.isArray(this.currentUser.roles) && 
           (this.currentUser.roles.includes('ADMIN') || this.currentUser.roles.includes('ROLE_ADMIN'));
  }

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_URL}/auth/login`, payload).pipe(
      tap((response) => {
        // Store tokens in localStorage
        if (response.accessToken) {
          localStorage.setItem('accessToken', response.accessToken);
        }
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken);
        }
        this.authState.next(true);
      }),
      catchError(err => {
        this.authState.next(false);
        throw err;
      })
    );
  }

  register(payload: { name: string; email: string; password: string }): Observable<any> {
    return this.http.post<any>(`${API_URL}/auth/register`, payload).pipe(
      catchError(err => {
        this.authState.next(false);
        throw err;
      })
    );
  }

  logout(): void {
    // Clear tokens from localStorage
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this.authState.next(false);
    this.currentUser = null;
  }

  isAuthenticated(): boolean {
    return this.authState.getValue();
  }

  /**
   * Check session on server (safe server-side validation). Returns Observable<boolean>.
   * This should be used by guards to prevent bypass.
   */
  checkSession(): Observable<boolean> {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      this.authState.next(false);
      return of(false);
    }

    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.get<any>(`${API_URL}/auth/me`, { headers }).pipe(
      map((res) => {
        this.authState.next(true);
        this.currentUser = { username: res.username, roles: res.roles };
        return true;
      }),
      catchError((err) => {
        console.debug('Session check failed:', err.status);
        this.authState.next(false);
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        return of(false);
      })
    );
  }
}
