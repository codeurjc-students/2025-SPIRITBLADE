import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable } from 'rxjs';
import { User } from '../dto/user.dto';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);

  getProfile(): Observable<User> {
    return this.http.get<User>(`${API_URL}/users/me`);
  }

  updateProfile(payload: Partial<User>): Observable<User> {
    return this.http.put<User>(`${API_URL}/users/me`, payload);
  }

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${API_URL}/admin/users`);
  }

  addFavoriteSummoner(summonerId: string): Observable<any> {
    return this.http.post(`${API_URL}/users/me/favorites`, { summonerId });
  }

  removeFavoriteSummoner(summonerId: string): Observable<any> {
    return this.http.delete(`${API_URL}/users/me/favorites/${summonerId}`);
  }
}
