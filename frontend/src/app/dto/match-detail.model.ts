import { TeamDTO } from './team.model';
import { ParticipantDTO } from './participant.model';

export interface MatchDetailDTO {
  matchId?: string;
  gameCreation?: number;
  gameDuration?: number;
  gameMode?: string;
  gameType?: string;
  gameVersion?: string;
  teams?: TeamDTO[];
  participants?: ParticipantDTO[];
}
