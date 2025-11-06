import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable } from 'rxjs';
import { Summoner } from '../dto/summoner.model';
import { ChampionMastery } from '../dto/champion-mastery.model';
import { MatchHistory } from '../dto/match-history.model';
import { MatchDetailDTO } from '../dto/match-detail.model';

@Injectable({ providedIn: 'root' })
export class SummonerService {
  private http = inject(HttpClient);

  getByName(name: string): Observable<Summoner> {
    return this.http.get<Summoner>(`${API_URL}/summoners/name/${encodeURIComponent(name)}`);
  }

  getById(id: string): Observable<Summoner> {
    return this.http.get<Summoner>(`${API_URL}/summoners/${id}`);
  }

  getChampionStats(summonerId: string): Observable<any> {
    return this.http.get(`${API_URL}/summoners/${summonerId}/champion-stats`);
  }

  getMatchHistory(summonerId: string, page = 0, size = 20): Observable<any> {
    return this.http.get(`${API_URL}/summoners/${summonerId}/matches?page=${page}&size=${size}`);
  }

  getTopChampions(name: string): Observable<ChampionMastery[]> {
    return this.http.get<ChampionMastery[]>(`${API_URL}/summoners/name/${encodeURIComponent(name)}/masteries`);
  }

  getRecentMatches(name: string, page: number = 0, size: number = 5): Observable<MatchHistory[]> {
    return this.http.get<MatchHistory[]>(`${API_URL}/summoners/name/${encodeURIComponent(name)}/matches?page=${page}&size=${size}`);
  }

  getRecentSearches(): Observable<Summoner[]> {
    return this.http.get<Summoner[]>(`${API_URL}/summoners/recent`);
  }

  getMatchDetails(matchId: string): Observable<MatchDetailDTO> {
    return this.http.get<MatchDetailDTO>(`${API_URL}/summoners/matches/${encodeURIComponent(matchId)}`);
  }
}
