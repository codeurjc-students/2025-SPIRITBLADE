import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable } from 'rxjs';

interface LoginRequest { username: string; password: string }
interface LoginResponse { token: string; username: string; role: string }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_URL}/auth/login`, payload);
  }

  register(payload: { username: string; email: string; password: string }): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_URL}/auth/register`, payload);
  }

  logout(): void {
    // Frontend-only logout behaviour (clear token)
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
  }

  saveSession(token: string, username: string) {
    localStorage.setItem('auth_token', token);
    localStorage.setItem('auth_user', username);
  }

  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}
