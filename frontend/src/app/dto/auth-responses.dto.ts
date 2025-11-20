// DTOs for authentication API responses

export interface AuthResponseDto {
  status: string;
  message: string;
  accessToken?: string;
  refreshToken?: string;
}

export interface RegisterResponseDto {
  success: boolean;
  message: string;
  user?: {
    id: number;
    name: string;
    email: string;
  };
}