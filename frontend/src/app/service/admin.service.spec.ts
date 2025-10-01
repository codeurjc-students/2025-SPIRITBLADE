import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
import { User } from '../dto/user.dto';
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
        },
        {
          id: 3,
          username: 'user2',
          email: 'user2@example.com',
          role: 'USER'
        }
      ];

      // Act
      service.getUsers().subscribe((users: User[]) => {
        expect(users).toEqual(mockUsers);
        expect(users.length).toBe(3);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockUsers);
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

    it('should handle unauthorized access', () => {
      // Act
      service.getUsers().subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(401);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users`);
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('setUserActive()', () => {
    it('should activate user', () => {
      // Arrange
      const userId = 123;
      const active = true;
      const mockResponse = {
        success: true,
        message: 'User activated successfully',
        userId: userId
      };

      // Act
      service.setUserActive(userId, active).subscribe((response: any) => {
        expect(response.success).toBeTrue();
        expect(response.userId).toBe(userId);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users/${userId}`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual({ active });
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockResponse);
    });

    it('should deactivate user', () => {
      // Arrange
      const userId = 456;
      const active = false;
      const mockResponse = {
        success: true,
        message: 'User deactivated successfully',
        userId: userId
      };

      // Act
      service.setUserActive(userId, active).subscribe((response: any) => {
        expect(response.success).toBeTrue();
        expect(response.userId).toBe(userId);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users/${userId}`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual({ active });
      req.flush(mockResponse);
    });

    it('should handle user not found', () => {
      // Arrange
      const nonExistentUserId = 999;
      const active = true;

      // Act
      service.setUserActive(nonExistentUserId, active).subscribe({
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
      const active = true;

      // Act
      service.setUserActive(userId, active).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(403);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users/${userId}`);
      req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
    });

    it('should handle attempt to modify own account', () => {
      // Arrange
      const ownUserId = 1; // Current admin user
      const active = false;

      // Act
      service.setUserActive(ownUserId, active).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(400);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/users/${ownUserId}`);
      req.flush('Cannot modify your own account', { status: 400, statusText: 'Bad Request' });
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
      expect(req.request.withCredentials).toBeTrue();
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

  describe('getSystemStats()', () => {
    it('should fetch system statistics', () => {
      // Arrange
      const mockSystemStats = {
        totalUsers: 1542,
        activeUsers: 1234,
        totalSummoners: 15420,
        totalMatches: 892340,
        systemHealth: {
          status: 'HEALTHY',
          uptime: 2592000, // 30 days in seconds
          memoryUsage: 0.65,
          cpuUsage: 0.42
        },
        recentActivity: {
          newUsers24h: 23,
          matchesLast24h: 5678,
          peakConcurrentUsers: 456
        },
        databaseStats: {
          totalConnections: 45,
          activeConnections: 12,
          averageResponseTime: 125 // milliseconds
        }
      };

      // Act
      service.getSystemStats().subscribe((stats: any) => {
        expect(stats).toEqual(mockSystemStats);
        expect(stats.totalUsers).toBe(1542);
        expect(stats.systemHealth.status).toBe('HEALTHY');
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/stats`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockSystemStats);
    });

    it('should handle system stats with warnings', () => {
      // Arrange
      const mockWarningStats = {
        totalUsers: 1542,
        activeUsers: 1234,
        systemHealth: {
          status: 'WARNING',
          uptime: 2592000,
          memoryUsage: 0.85, // High memory usage
          cpuUsage: 0.72     // High CPU usage
        },
        warnings: [
          'High memory usage detected',
          'CPU usage above normal threshold'
        ]
      };

      // Act
      service.getSystemStats().subscribe((stats: any) => {
        expect(stats.systemHealth.status).toBe('WARNING');
        expect(stats.warnings).toBeDefined();
        expect(stats.warnings.length).toBe(2);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/stats`);
      req.flush(mockWarningStats);
    });

    it('should handle forbidden access for non-admin', () => {
      // Act
      service.getSystemStats().subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(403);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/stats`);
      req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
    });

    it('should handle unauthorized access', () => {
      // Act
      service.getSystemStats().subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(401);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/stats`);
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle server error', () => {
      // Act
      service.getSystemStats().subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(500);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/admin/stats`);
      req.flush('Internal server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });
});