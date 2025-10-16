import { ParticipantDTO } from './participant.model';

export interface TeamDTO {
  teamId?: number;
  win?: boolean;
  participants?: ParticipantDTO[];
  baronKills?: number;
  dragonKills?: number;
  towerKills?: number;
  inhibitorKills?: number;
  riftHeraldKills?: number;
  bans?: string[];
}
