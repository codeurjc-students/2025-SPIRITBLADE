import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable } from 'rxjs';
import { User } from '../dto/user.dto';

interface LinkSummonerRequest {
  summonerName: string;
  region: string;
}

interface LinkSummonerResponse {
  success: boolean;
  message: string;
  linkedSummoner?: {
    name: string;
    level: number;
    profileIcon: number;
    region: string;
  };
}

interface UnlinkSummonerResponse {
  success: boolean;
  message: string;
}

interface LinkedSummonerResponse {
  linked: boolean;
  summonerName?: string;
  region?: string;
  puuid?: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);

  getProfile(): Observable<User> {
    return this.http.get<User>(`${API_URL}/users/me`, { withCredentials: true });
  }

  updateProfile(payload: Partial<User>): Observable<User> {
    return this.http.put<User>(`${API_URL}/users/me`, payload, { withCredentials: true });
  }

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${API_URL}/admin/users`, { withCredentials: true });
  }

  addFavoriteSummoner(summonerId: string): Observable<any> {
    return this.http.post(`${API_URL}/users/me/favorites`, { summonerId }, { withCredentials: true });
  }

  removeFavoriteSummoner(summonerId: string): Observable<any> {
    return this.http.delete(`${API_URL}/users/me/favorites/${summonerId}`, { withCredentials: true });
  }

  /**
   * Link a League of Legends account to the current user.
   * @param summonerName The summoner's name
   * @param region The region (e.g., 'EUW', 'NA', 'KR')
   * @returns Observable with the link result
   */
  linkSummoner(summonerName: string, region: string): Observable<LinkSummonerResponse> {
    const payload: LinkSummonerRequest = { summonerName, region };
    return this.http.post<LinkSummonerResponse>(`${API_URL}/users/link-summoner`, payload, { withCredentials: true });
  }

  /**
   * Unlink the League of Legends account from the current user.
   * @returns Observable with the unlink result
   */
  unlinkSummoner(): Observable<UnlinkSummonerResponse> {
    return this.http.post<UnlinkSummonerResponse>(`${API_URL}/users/unlink-summoner`, {}, { withCredentials: true });
  }

  /**
   * Get the linked League of Legends account for the current user.
   * @returns Observable with linked summoner information
   */
  getLinkedSummoner(): Observable<LinkedSummonerResponse> {
    return this.http.get<LinkedSummonerResponse>(`${API_URL}/users/linked-summoner`, { withCredentials: true });
  }

  /**
   * Upload avatar for the current user.
   * @param file The image file to upload
   * @returns Observable with upload result including avatarUrl
   */
  uploadAvatar(file: File): Observable<{ success: boolean; message: string; avatarUrl?: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ success: boolean; message: string; avatarUrl?: string }>(
      `${API_URL}/users/avatar`, 
      formData, 
      { withCredentials: true }
    );
  }

  /**
   * Delete avatar for the current user.
   * @returns Observable with delete result
   */
  deleteAvatar(): Observable<{ success: boolean; message: string }> {
    return this.http.delete<{ success: boolean; message: string }>(
      `${API_URL}/users/avatar`, 
      { withCredentials: true }
    );
  }
}
