import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
import { User, PagedResponse } from '../dto/user.dto';
import { API_URL } from './api.config';

describe('AdminService - Unit Tests', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AdminService]
    });
    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getUsers()', () => {
    it('should fetch all users', () => {
      // Arrange
      const mockResponse: PagedResponse<User> = {
        content: [
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
          },
          {
            id: 3,
            name: 'user2',
            email: 'user2@example.com',
            roles: ['USER'],
            active: true
          }
        ],
        totalElements: 3,
        totalPages: 1,
        size: 20,
        number: 0,
        first: true,
        last: true,
        empty: false
      };

      // Act
      service.getUsers().subscribe((response: PagedResponse<User>) => {
        expect(response).toEqual(mockResponse);
        expect(response.content.length).toBe(3);
      });

      // Assert
      const req = httpMock.expectOne((request) => request.url.includes(`${API_URL}/admin/users`));
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should handle empty user list', () => {
      const emptyResponse: PagedResponse<User> = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 20,
        number: 0,
        first: true,
        last: true,
        empty: true
      };

      // Act
      service.getUsers().subscribe((response: PagedResponse<User>) => {
        expect(response.content).toEqual([]);
        expect(response.empty).toBeTrue();
      });

      // Assert
      const req = httpMock.expectOne((request) => request.url.includes(`${API_URL}/admin/users`));
      req.flush(emptyResponse);
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
      const req = httpMock.expectOne((request) => request.url.includes(`${API_URL}/admin/users`));
      req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
    });

    it('should handle unauthorized access', () => {
      // Act
      service.getUsers().subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(401);
        }
      });

      // Assert
      const req = httpMock.expectOne((request) => request.url.includes(`${API_URL}/admin/users`));
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('deleteUser()', () => {
    it('should delete user', () => {
      // Arrange
      const userId = 123;
      const mockResponse = {
        success: true,
        message: 'User deleted successfully',
        deletedUserId: userId
      };

      // Act
      service.deleteUser(userId).subscribe((response: any) => {
        expect(response.success).toBeTrue();
        expect(response.deletedUserId).toBe(userId);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users/${userId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });

    it('should handle user not found', () => {
      // Arrange
      const nonExistentUserId = 999;

      // Act
      service.deleteUser(nonExistentUserId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(404);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users/${nonExistentUserId}`);
      req.flush('User not found', { status: 404, statusText: 'Not Found' });
    });

    it('should handle forbidden access', () => {
      // Arrange
      const userId = 123;

      // Act
      service.deleteUser(userId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(403);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users/${userId}`);
      req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
    });

    it('should handle attempt to delete own account', () => {
      // Arrange
      const ownUserId = 1; // Current admin user

      // Act
      service.deleteUser(ownUserId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(400);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users/${ownUserId}`);
      req.flush('Cannot delete your own account', { status: 400, statusText: 'Bad Request' });
    });

    it('should handle attempt to delete other admin account', () => {
      // Arrange
      const otherAdminUserId = 2;

      // Act
      service.deleteUser(otherAdminUserId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(400);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users/${otherAdminUserId}`);
      req.flush('Cannot delete other admin accounts', { status: 400, statusText: 'Bad Request' });
    });
  });
});
