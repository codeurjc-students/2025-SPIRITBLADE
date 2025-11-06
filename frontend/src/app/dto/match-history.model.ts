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
  lpAtMatch?: number; // Approximate LP at this match
  queueId?: number; // 420=Solo/Duo, 440=Flex, etc.
}
