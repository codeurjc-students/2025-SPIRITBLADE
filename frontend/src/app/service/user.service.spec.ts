import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserService } from './user.service';
import { User } from '../dto/user.dto';
import { API_URL } from './api.config';

describe('UserService - Unit Tests', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getProfile()', () => {
    it('should fetch user profile', () => {
      // Arrange
      const mockUser: User = {
        id: 1,
        username: 'testuser',
        email: 'test@example.com',
        role: 'USER'
      };

      // Act
      service.getProfile().subscribe((user: User) => {
        expect(user).toEqual(mockUser);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockUser);
    });

    it('should handle unauthorized access', () => {
      // Act
      service.getProfile().subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(401);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me`);
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle server error', () => {
      // Act
      service.getProfile().subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(500);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me`);
      req.flush('Internal server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('updateProfile()', () => {
    it('should update user profile', () => {
      // Arrange
      const updatePayload: Partial<User> = {
        username: 'updateduser',
        email: 'updated@example.com'
      };
      const updatedUser: User = {
        id: 1,
        username: 'updateduser',
        email: 'updated@example.com',
        role: 'USER'
      };

      // Act
      service.updateProfile(updatePayload).subscribe((user: User) => {
        expect(user).toEqual(updatedUser);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updatePayload);
      expect(req.request.withCredentials).toBeTrue();
      req.flush(updatedUser);
    });

    it('should handle validation errors', () => {
      // Arrange
      const invalidPayload: Partial<User> = {
        email: 'invalid-email'
      };

      // Act
      service.updateProfile(invalidPayload).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(400);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me`);
      req.flush('Validation failed', { status: 400, statusText: 'Bad Request' });
    });

    it('should handle username conflict', () => {
      // Arrange
      const conflictPayload: Partial<User> = {
        username: 'existinguser'
      };

      // Act
      service.updateProfile(conflictPayload).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(409);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me`);
      req.flush('Username already exists', { status: 409, statusText: 'Conflict' });
    });
  });

  describe('getUsers()', () => {
    it('should fetch all users (admin endpoint)', () => {
      // Arrange
      const mockUsers: User[] = [
        {
          id: 1,
          username: 'user1',
          email: 'user1@example.com',
          role: 'USER'
        },
        {
          id: 2,
          username: 'admin1',
          email: 'admin1@example.com',
          role: 'ADMIN'
        }
      ];

      // Act
      service.getUsers().subscribe((users: User[]) => {
        expect(users).toEqual(mockUsers);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockUsers);
    });

    it('should handle forbidden access for non-admin', () => {
      // Act
      service.getUsers().subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(403);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users`);
      req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
    });

    it('should handle empty user list', () => {
      // Act
      service.getUsers().subscribe((users: User[]) => {
        expect(users).toEqual([]);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users`);
      req.flush([]);
    });
  });

  describe('addFavoriteSummoner()', () => {
    it('should add summoner to favorites', () => {
      // Arrange
      const summonerId = 'summoner123';
      const mockResponse = {
        success: true,
        message: 'Summoner added to favorites'
      };

      // Act
      service.addFavoriteSummoner(summonerId).subscribe((response: any) => {
        expect(response).toEqual(mockResponse);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me/favorites`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ summonerId });
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockResponse);
    });

    it('should handle summoner already in favorites', () => {
      // Arrange
      const summonerId = 'summoner123';

      // Act
      service.addFavoriteSummoner(summonerId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(409);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me/favorites`);
      req.flush('Summoner already in favorites', { status: 409, statusText: 'Conflict' });
    });

    it('should handle invalid summoner ID', () => {
      // Arrange
      const invalidSummonerId = '';

      // Act
      service.addFavoriteSummoner(invalidSummonerId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(400);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me/favorites`);
      req.flush('Invalid summoner ID', { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('removeFavoriteSummoner()', () => {
    it('should remove summoner from favorites', () => {
      // Arrange
      const summonerId = 'summoner123';
      const mockResponse = {
        success: true,
        message: 'Summoner removed from favorites'
      };

      // Act
      service.removeFavoriteSummoner(summonerId).subscribe((response: any) => {
        expect(response).toEqual(mockResponse);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me/favorites/${summonerId}`);
      expect(req.request.method).toBe('DELETE');
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockResponse);
    });

    it('should handle summoner not in favorites', () => {
      // Arrange
      const summonerId = 'summoner123';

      // Act
      service.removeFavoriteSummoner(summonerId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(404);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me/favorites/${summonerId}`);
      req.flush('Summoner not found in favorites', { status: 404, statusText: 'Not Found' });
    });

    it('should handle unauthorized access', () => {
      // Arrange
      const summonerId = 'summoner123';

      // Act
      service.removeFavoriteSummoner(summonerId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(401);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me/favorites/${summonerId}`);
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });
  });
});