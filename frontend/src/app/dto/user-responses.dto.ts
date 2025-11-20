// DTOs for user API responses

export interface UserProfileDto {
  id: number;
  name: string;
  email: string;
  roles: string[];
  active: boolean;
  image?: string;
  avatarUrl?: string;
  linkedSummonerName?: string;
  linkedSummonerRegion?: string;
}

export interface UploadAvatarResponseDto {
  success: boolean;
  message: string;
  avatarUrl?: string;
}