// Common DTOs for API responses

export interface ApiResponseDto<T = any> {
  success: boolean;
  message: string;
  data?: T;
}

export interface FavoriteResponseDto {
  success: boolean;
  message: string;
}