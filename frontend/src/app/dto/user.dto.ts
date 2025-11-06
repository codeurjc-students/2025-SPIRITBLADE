export interface User {
  id: number;
  name: string;  // Backend uses 'name' not 'username'
  email: string;
  password?: string;  // Optional, only for creation
  roles: string[];  // Backend uses array of roles
  active: boolean;
  image?: string;
  avatarUrl?: string;  // Avatar URL from storage service
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}