/**
 * Champion Mastery data from Riot API
 * Matches backend RiotChampionMasteryDTO
 */
export interface ChampionMastery {
  puuid?: string;
  championId?: number;
  championName?: string; // Populated by backend using Data Dragon
  championIconUrl?: string; // Champion icon URL from Data Dragon
  championLevel?: number;
  championPoints?: number;
  lastPlayTime?: number;
  championPointsSinceLastLevel?: number;
  championPointsUntilNextLevel?: number;
  chestGranted?: boolean;
  tokensEarned?: number;
}
