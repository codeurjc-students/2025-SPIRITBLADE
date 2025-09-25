import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);

  getPersonalStats(): Observable<any> {
    return this.http.get(`${API_URL}/dashboard/me/stats`, { withCredentials: true });
  }

  getFavoritesOverview(): Observable<any> {
    return this.http.get(`${API_URL}/dashboard/me/favorites`, { withCredentials: true });
  }
}
