import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from './api.config';
import { Observable } from 'rxjs';
import { User, PagedResponse } from '../dto/user.dto';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private readonly usersUrl = `${API_URL}/users`;

  /**
   * Get all users from the system with pagination and filters.
   */
  getUsers(page: number = 0, size: number = 20, filters?: {
    role?: string;
    active?: boolean;
    search?: string;
  }): Observable<PagedResponse<User>> {
    let params: any = { page, size };
    
    if (filters) {
      if (filters.role) params.role = filters.role;
      if (filters.active !== undefined) params.active = filters.active;
      if (filters.search) params.search = filters.search;
    }
    
    return this.http.get<PagedResponse<User>>(this.usersUrl, { 
      params, 
      withCredentials: true 
    });
  }

  /**
   * Get a single user by ID.
   */
  getUserById(userId: number): Observable<User> {
    return this.http.get<User>(`${this.usersUrl}/${userId}`, { withCredentials: true });
  }

  /**
   * Create a new user.
   */
  createUser(user: User): Observable<User> {
    return this.http.post<User>(this.usersUrl, user, { withCredentials: true });
  }

  /**
   * Update an existing user.
   */
  updateUser(userId: number, user: User): Observable<User> {
    return this.http.put<User>(`${this.usersUrl}/${userId}`, user, { withCredentials: true });
  }

  /**
   * Change user role.
   */
  changeUserRole(userId: number, role: string): Observable<User> {
    return this.http.put<User>(`${this.usersUrl}/${userId}/role`, role, { withCredentials: true });
  }

  /**
   * Toggle user active status.
   */
  toggleUserActive(userId: number): Observable<User> {
    return this.http.put<User>(`${this.usersUrl}/${userId}/toggle-active`, {}, { withCredentials: true });
  }

  /**
   * Delete a user from the system.
   */
  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.usersUrl}/${userId}`, { withCredentials: true });
  }
}
