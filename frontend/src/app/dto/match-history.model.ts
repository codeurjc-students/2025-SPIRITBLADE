export interface MatchHistory {
  matchId?: string;
  championName?: string;
  championIconUrl?: string;
  win?: boolean;
  kills?: number;
  deaths?: number;
  assists?: number;
  gameDuration?: number;
  gameTimestamp?: number;
}
