// DTOs for summoner API responses

export interface SummonerDto {
  id: number;
  name: string;
  puuid: string;
  level: number;
  profileIconId: number;
  tier?: string;
  rank?: string;
  leaguePoints?: number;
  wins?: number;
  losses?: number;
  lastSearchedAt?: string;
}

export interface LinkSummonerResponseDto {
  success: boolean;
  message: string;
  linkedSummoner?: {
    name: string;
    level: number;
    profileIcon: number;
    region: string;
  };
}

export interface UnlinkSummonerResponseDto {
  success: boolean;
  message: string;
}

export interface LinkedSummonerResponseDto {
  linked: boolean;
  summonerName?: string;
  region?: string;
  puuid?: string;
}