export interface Summoner {
  id: string;
  riotId?: string;
  name: string;
  level: number;
  profileIconId: number;
  profileIconUrl?: string; // URL to profile icon image from Data Dragon
  rank?: string;
  tier?: string;
  lp?: number;
  wins?: number;
  losses?: number;
  lastSearchedAt?: string; // ISO date string
}