import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable } from 'rxjs';
import { Summoner } from '../dto/summoner.model';

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
}
