import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

interface LoginRequest { username: string; password: string }
interface LoginResponse { token?: string; username?: string; role?: string }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  // in-memory auth state; do NOT rely on localStorage token for isAuthenticated()
  private authState = new BehaviorSubject<boolean>(false);
  public readonly isAuthenticated$ = this.authState.asObservable();
  private currentUser: { username?: string; roles?: string[] } | null = null;

  isAdmin(): boolean {
    return !!this.currentUser && Array.isArray(this.currentUser.roles) && this.currentUser.roles.includes('ADMIN');
  }

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_URL}/auth/login`, payload, { withCredentials: true }).pipe(
      tap(() => this.authState.next(true)),
      catchError(err => {
        this.authState.next(false);
        throw err;
      })
    );
  }

  register(payload: { name: string; email: string; password: string }): Observable<any> {
    return this.http.post<any>(`${API_URL}/auth/register`, payload, { withCredentials: true }).pipe(
      tap(() => this.authState.next(true)),
      catchError(err => {
        this.authState.next(false);
        throw err;
      })
    );
  }

  logout(): void {
    // clear client state immediately and attempt server logout
    this.authState.next(false);
    this.http.post<any>(`${API_URL}/auth/logout`, {}, { withCredentials: true }).subscribe({
      next: () => {},
      error: () => {}
    });
  }

  isAuthenticated(): boolean {
    return this.authState.getValue();
  }

  /**
   * Check session on server (safe server-side validation). Returns Observable<boolean>.
   * This should be used by guards to prevent bypass.
   */
  checkSession(): Observable<boolean> {
    return this.http.get<any>(`${API_URL}/auth/me`, { withCredentials: true }).pipe(
      map((res) => {
        this.authState.next(true);
        this.currentUser = { username: res.username, roles: res.roles };
        return true;
      }),
      catchError(() => {
        this.authState.next(false);
        return of(false);
      })
    );
  }

  getCurrentUser() {
    return this.currentUser;
  }
}
