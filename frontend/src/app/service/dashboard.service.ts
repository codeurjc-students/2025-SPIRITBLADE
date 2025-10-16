import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable } from 'rxjs';

export interface RankHistoryEntry {
  date: string;
  tier: string;
  rank: string;
  leaguePoints: number;
  wins: number;
  losses: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);

  getPersonalStats(): Observable<any> {
    return this.http.get(`${API_URL}/dashboard/me/stats`, { withCredentials: true });
  }

  getFavoritesOverview(): Observable<any> {
    return this.http.get(`${API_URL}/dashboard/me/favorites`, { withCredentials: true });
  }

  /**
   * Get rank history for LP progression chart
   * Returns historical data of LP changes over time
   */
  getRankHistory(): Observable<RankHistoryEntry[]> {
    return this.http.get<RankHistoryEntry[]>(`${API_URL}/dashboard/me/rank-history`, { withCredentials: true });
  }
}
