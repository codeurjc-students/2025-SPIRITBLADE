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
        name: 'testuser',
        email: 'test@example.com',
        roles: ['USER'],
        active: true
      };

      // Act
      service.getProfile().subscribe((user: User) => {
        expect(user).toEqual(mockUser);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/users/me`);
      expect(req.request.method).toBe('GET');
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

  describe('getUsers()', () => {
    it('should fetch all users (admin endpoint)', () => {
      // Arrange
      const mockUsers: User[] = [
        {
          id: 1,
          name: 'user1',
          email: 'user1@example.com',
          roles: ['USER'],
          active: true
        },
        {
          id: 2,
          name: 'admin1',
          email: 'admin1@example.com',
          roles: ['ADMIN'],
          active: true
        }
      ];

      // Act
      service.getUsers().subscribe((users: User[]) => {
        expect(users).toEqual(mockUsers);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users`);
      expect(req.request.method).toBe('GET');
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
      const encodedId = encodeURIComponent(summonerId);
      const mockResponse = {
        success: true,
        message: 'Summoner added to favorites'
      };

      // Act
      service.addFavoriteSummoner(summonerId).subscribe((response: any) => {
        expect(response).toEqual(mockResponse);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/favorites/${encodedId}`);
      expect(req.request.method).toBe('POST');
      req.flush(mockResponse);
    });

    it('should handle summoner already in favorites', () => {
      // Arrange
      const summonerId = 'summoner123';
      const encodedId = encodeURIComponent(summonerId);

      // Act
      service.addFavoriteSummoner(summonerId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(409);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/favorites/${encodedId}`);
      req.flush('Summoner already in favorites', { status: 409, statusText: 'Conflict' });
    });

    it('should handle invalid summoner ID', () => {
      // Arrange
      const invalidSummonerId = '';
      const encodedId = encodeURIComponent(invalidSummonerId);

      // Act
      service.addFavoriteSummoner(invalidSummonerId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(400);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/favorites/${encodedId}`);
      req.flush('Invalid summoner ID', { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('removeFavoriteSummoner()', () => {
    it('should remove summoner from favorites', () => {
      // Arrange
      const summonerId = 'summoner123';
      const encodedId = encodeURIComponent(summonerId);
      const mockResponse = {
        success: true,
        message: 'Summoner removed from favorites'
      };

      // Act
      service.removeFavoriteSummoner(summonerId).subscribe((response: any) => {
        expect(response).toEqual(mockResponse);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/favorites/${encodedId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });

    it('should handle summoner not in favorites', () => {
      // Arrange
      const summonerId = 'summoner123';
      const encodedId = encodeURIComponent(summonerId);

      // Act
      service.removeFavoriteSummoner(summonerId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(404);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/favorites/${encodedId}`);
      req.flush('Summoner not found in favorites', { status: 404, statusText: 'Not Found' });
    });

    it('should handle unauthorized access', () => {
      // Arrange
      const summonerId = 'summoner123';
      const encodedId = encodeURIComponent(summonerId);

      // Act
      service.removeFavoriteSummoner(summonerId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(401);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/favorites/${encodedId}`);
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });
  });
});
