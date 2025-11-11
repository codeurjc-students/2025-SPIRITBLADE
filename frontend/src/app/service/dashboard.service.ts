import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable } from 'rxjs';
import { MatchHistory } from '../dto/match-history.model';

export interface RankHistoryEntry {
  date: string;
  tier: string;
  rank: string;
  leaguePoints: number;
  wins: number;
  losses: number;
}

export interface AiAnalysisResponse {
  analysis: string;
  generatedAt: string;
  matchesAnalyzed: number;
  summonerName: string;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);

  getPersonalStats(): Observable<any> {
    return this.http.get(`${API_URL}/dashboard/me/stats`);
  }

  getFavoritesOverview(): Observable<any> {
    return this.http.get(`${API_URL}/dashboard/me/favorites`);
  }

  /**
   * Get ranked match history for the linked summoner
   * Returns only RANKED matches (Solo/Duo and Flex)
   * @param page Page number
   * @param size Number of matches per page (default: 100)
   * @param queueId Optional queue filter (420=Solo/Duo, 440=Flex, null=All)
   */
  getRankedMatches(page: number = 0, size: number = 100, queueId: number | null = null): Observable<MatchHistory[]> {
    let url = `${API_URL}/dashboard/me/ranked-matches?page=${page}&size=${size}`;
    if (queueId !== null) {
      url += `&queueId=${queueId}`;
    }
    return this.http.get<MatchHistory[]>(url);
  }

  /**
   * Get AI-powered performance analysis
   * Analyzes recent match history using Google Gemini AI
   * @param matchCount Number of recent matches to analyze (default: 20, min: 5, max: 50)
   */
  getAiAnalysis(matchCount: number = 20): Observable<AiAnalysisResponse> {
    return this.http.post<AiAnalysisResponse>(
      `${API_URL}/dashboard/me/ai-analysis?matchCount=${matchCount}`,
      {}
    );
  }
}
