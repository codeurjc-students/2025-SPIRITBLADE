// DTOs for dashboard API responses

export interface PersonalStatsDto {
  username: string;
  linkedSummoner: string | null;
  currentRank: string;
  lp7days: number;
  mainRole: string;
  favoriteChampion: string | null;
  averageKda: string;
}

export interface AiAnalysisResponseDto {
  analysis: string;
  generatedAt: string;
  matchesAnalyzed: number;
  summonerName: string;
}