import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { API_URL } from './api.config';

describe('AuthService - Unit Tests', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('login()', () => {
    it('should login successfully', () => {
      // Arrange
      const mockPayload = { username: 'testuser', password: 'testpass' };
      const mockResponse = { token: 'mock-jwt-token', username: 'testuser', role: 'USER' };

      // Act
      service.login(mockPayload).subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(service.isAuthenticated()).toBeTrue();
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/auth/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockPayload);
      expect(req.request.withCredentials).toBeTrue();
      
      req.flush(mockResponse);
    });

    it('should handle login error', () => {
      // Arrange
      const mockPayload = { username: 'testuser', password: 'wrongpass' };

      // Act
      service.login(mockPayload).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(401);
          expect(service.isAuthenticated()).toBeFalse();
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/auth/login`);
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });

    it('should update authentication state observable', () => {
      // Arrange
      const mockPayload = { username: 'testuser', password: 'testpass' };
      const mockResponse = { token: 'mock-jwt-token' };
      let authState: boolean | undefined;

      service.isAuthenticated$.subscribe(state => authState = state);

      // Act
      service.login(mockPayload).subscribe();

      // Assert
      const req = httpMock.expectOne(`${API_URL}/auth/login`);
      req.flush(mockResponse);
      
      expect(authState).toBeTrue();
    });
  });

  describe('register()', () => {
    it('should register successfully', () => {
      // Arrange
      const mockPayload = { 
        name: 'newuser', 
        email: 'newuser@test.com', 
        password: 'newpass' 
      };
      const mockResponse = { id: 1, username: 'newuser' };

      // Act
      service.register(mockPayload).subscribe(response => {
        expect(response).toEqual(mockResponse);
        expect(service.isAuthenticated()).toBeTrue();
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/auth/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockPayload);
      expect(req.request.withCredentials).toBeTrue();
      
      req.flush(mockResponse);
    });

    it('should handle registration conflict error', () => {
      // Arrange
      const mockPayload = { 
        name: 'existinguser', 
        email: 'existing@test.com', 
        password: 'pass' 
      };

      // Act
      service.register(mockPayload).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(409);
          expect(service.isAuthenticated()).toBeFalse();
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/auth/register`);
      req.flush('User already exists', { status: 409, statusText: 'Conflict' });
    });
  });

  describe('logout()', () => {
    it('should logout and clear auth state', () => {
      // Arrange
      service.login({ username: 'test', password: 'test' }).subscribe();
      let loginReq = httpMock.expectOne(`${API_URL}/auth/login`);
      loginReq.flush({ token: 'token' });
      
      expect(service.isAuthenticated()).toBeTrue();

      // Act
      service.logout();

      // Assert
      expect(service.isAuthenticated()).toBeFalse();
      
      const logoutReq = httpMock.expectOne(`${API_URL}/auth/logout`);
      expect(logoutReq.request.method).toBe('POST');
      expect(logoutReq.request.withCredentials).toBeTrue();
      logoutReq.flush({});
    });

    it('should clear auth state even if server logout fails', () => {
      // Arrange
      service.login({ username: 'test', password: 'test' }).subscribe();
      let loginReq = httpMock.expectOne(`${API_URL}/auth/login`);
      loginReq.flush({ token: 'token' });

      // Act
      service.logout();

      // Assert
      expect(service.isAuthenticated()).toBeFalse();
      
      const logoutReq = httpMock.expectOne(`${API_URL}/auth/logout`);
      logoutReq.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('checkSession()', () => {
    it('should validate session successfully', () => {
      // Arrange
      const mockResponse = { 
        username: 'testuser', 
        roles: ['USER', 'ADMIN'] 
      };

      // Act
      service.checkSession().subscribe(isValid => {
        expect(isValid).toBeTrue();
        expect(service.isAuthenticated()).toBeTrue();
        expect(service.getCurrentUser()).toEqual({
          username: 'testuser',
          roles: ['USER', 'ADMIN']
        });
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/auth/me`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockResponse);
    });

    it('should handle invalid session', () => {
      // Act
      service.checkSession().subscribe(isValid => {
        expect(isValid).toBeFalse();
        expect(service.isAuthenticated()).toBeFalse();
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/auth/me`);
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('isAdmin()', () => {
    it('should return true for admin user', () => {
      // Arrange
      const mockResponse = { 
        username: 'admin', 
        roles: ['USER', 'ADMIN'] 
      };

      service.checkSession().subscribe();
      const req = httpMock.expectOne(`${API_URL}/auth/me`);
      req.flush(mockResponse);

      // Act & Assert
      expect(service.isAdmin()).toBeTrue();
    });

    it('should return false for non-admin user', () => {
      // Arrange
      const mockResponse = { 
        username: 'user', 
        roles: ['USER'] 
      };

      service.checkSession().subscribe();
      const req = httpMock.expectOne(`${API_URL}/auth/me`);
      req.flush(mockResponse);

      // Act & Assert
      expect(service.isAdmin()).toBeFalse();
    });

    it('should return false when no user is logged in', () => {
      // Act & Assert
      expect(service.isAdmin()).toBeFalse();
    });
  });

  describe('isAuthenticated()', () => {
    it('should return false initially', () => {
      expect(service.isAuthenticated()).toBeFalse();
    });

    it('should return true after successful login', () => {
      service.login({ username: 'test', password: 'test' }).subscribe();
      const req = httpMock.expectOne(`${API_URL}/auth/login`);
      req.flush({ token: 'token' });

      expect(service.isAuthenticated()).toBeTrue();
    });
  });

  describe('getCurrentUser()', () => {
    it('should return null initially', () => {
      expect(service.getCurrentUser()).toBeNull();
    });

    it('should return user after successful session check', () => {
      const mockResponse = { 
        username: 'testuser', 
        roles: ['USER'] 
      };

      service.checkSession().subscribe();
      const req = httpMock.expectOne(`${API_URL}/auth/me`);
      req.flush(mockResponse);

      expect(service.getCurrentUser()).toEqual({
        username: 'testuser',
        roles: ['USER']
      });
    });
  });
});