// DTOs for rank history API responses

export interface RankHistoryDto {
  id: number;
  summonerId: number;
  queueType: string;
  tier: string;
  rank: string;
  leaguePoints: number;
  lpChange: number;
  timestamp: string;
}

export interface RankProgressionResponseDto {
  success: boolean;
  progression: RankHistoryDto[];
  totalEntries: number;
  queueType: string;
  peakRank?: RankHistoryDto;
}