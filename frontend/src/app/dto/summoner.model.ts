export interface Summoner {
  id: string;
  name: string;
  level: number;
  profileIconId: number;
  rank?: string;
  tier?: string;
  lp?: number;
}