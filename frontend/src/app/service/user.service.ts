import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable } from 'rxjs';
import { User } from '../dto/user.dto';
import { LinkSummonerResponseDto, UnlinkSummonerResponseDto, LinkedSummonerResponseDto } from '../dto/summoner-responses.dto';
import { UploadAvatarResponseDto } from '../dto/user-responses.dto';
import { FavoriteResponseDto } from '../dto/common-responses.dto';

interface LinkSummonerRequest {
  summonerName: string;
  region: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);

  getProfile(): Observable<User> {
    return this.http.get<User>(`${API_URL}/users/me`);
  }

  updateProfile(updates: any): Observable<User> {
    return this.http.put<User>(`${API_URL}/users/me`, updates);
  }

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${API_URL}/admin/users`);
  }

  addFavoriteSummoner(summonerName: string): Observable<FavoriteResponseDto> {
    // Encode the summoner name to handle special characters like #
    const encodedName = encodeURIComponent(summonerName);
    return this.http.post<FavoriteResponseDto>(`${API_URL}/dashboard/me/favorites/${encodedName}`, {});
  }

  removeFavoriteSummoner(summonerName: string): Observable<FavoriteResponseDto> {
    // Encode the summoner name to handle special characters like #
    const encodedName = encodeURIComponent(summonerName);
    return this.http.delete<FavoriteResponseDto>(`${API_URL}/dashboard/me/favorites/${encodedName}`);
  }

  /**
   * Link a League of Legends account to the current user.
   * @param summonerName The summoner's name
   * @param region The region (e.g., 'EUW', 'NA', 'KR')
   * @returns Observable with the link result
   */
  linkSummoner(summonerName: string, region: string): Observable<LinkSummonerResponseDto> {
    const payload: LinkSummonerRequest = { summonerName, region };
    return this.http.post<LinkSummonerResponseDto>(`${API_URL}/users/link-summoner`, payload);
  }

  /**
   * Unlink the League of Legends account from the current user.
   * @returns Observable with the unlink result
   */
  unlinkSummoner(): Observable<UnlinkSummonerResponseDto> {
    return this.http.post<UnlinkSummonerResponseDto>(`${API_URL}/users/unlink-summoner`, {});
  }

  /**
   * Get the linked League of Legends account for the current user.
   * @returns Observable with linked summoner information
   */
  getLinkedSummoner(): Observable<LinkedSummonerResponseDto> {
    return this.http.get<LinkedSummonerResponseDto>(`${API_URL}/users/linked-summoner`);
  }

  /**
   * Upload avatar for the current user.
   * @param file The image file to upload
   * @returns Observable with upload result including avatarUrl
   */
  uploadAvatar(file: File): Observable<UploadAvatarResponseDto> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<UploadAvatarResponseDto>(
      `${API_URL}/users/avatar`,
      formData
    );
  }
}
