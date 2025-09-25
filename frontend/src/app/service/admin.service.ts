import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable } from 'rxjs';
import { User } from '../dto/user.dto';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${API_URL}/admin/users`, { withCredentials: true });
  }

  setUserActive(userId: number, active: boolean): Observable<any> {
    return this.http.patch(`${API_URL}/admin/users/${userId}`, { active }, { withCredentials: true });
  }

  deleteUser(userId: number): Observable<any> {
    return this.http.delete(`${API_URL}/admin/users/${userId}`, { withCredentials: true });
  }

  getSystemStats(): Observable<any> {
    return this.http.get(`${API_URL}/admin/stats`, { withCredentials: true });
  }
}
