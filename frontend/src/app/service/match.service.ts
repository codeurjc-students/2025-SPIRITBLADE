import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class MatchService {
  private http = inject(HttpClient);

  getMatch(matchId: string): Observable<any> {
    return this.http.get(`${API_URL}/matches/${matchId}`);
  }

  addNote(matchId: string, note: { text: string }): Observable<any> {
    return this.http.post(`${API_URL}/matches/${matchId}/notes`, note, { withCredentials: true });
  }

  getNotes(matchId: string): Observable<any> {
    return this.http.get(`${API_URL}/matches/${matchId}/notes`, { withCredentials: true });
  }
}
