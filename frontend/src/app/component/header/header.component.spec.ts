import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { HeaderComponent } from './header.component';
import { AuthService } from '../../service/auth.service';
import { CommonModule } from '@angular/common';

describe('HeaderComponent - Unit Tests', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    // Create spies for dependencies
    mockAuthService = jasmine.createSpyObj('AuthService', ['isAuthenticated', 'isAdmin', 'logout']);

    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        RouterTestingModule.withRoutes([]), // Configuración correcta del router
        HeaderComponent
      ],
      providers: [
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    
    // Spy en el router después de la inyección
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Authentication State', () => {
    it('should return true when user is authenticated', () => {
      // Arrange
      mockAuthService.isAuthenticated.and.returnValue(true);

      // Act
      const result = component.isAuthenticated;

      // Assert
      expect(result).toBeTrue();
      expect(mockAuthService.isAuthenticated).toHaveBeenCalled();
    });

    it('should return false when user is not authenticated', () => {
      // Arrange
      mockAuthService.isAuthenticated.and.returnValue(false);

      // Act
      const result = component.isAuthenticated;

      // Assert
      expect(result).toBeFalse();
      expect(mockAuthService.isAuthenticated).toHaveBeenCalled();
    });

    it('should return true when user is admin', () => {
      // Arrange
      mockAuthService.isAdmin.and.returnValue(true);

      // Act
      const result = component.isAdmin;

      // Assert
      expect(result).toBeTrue();
      expect(mockAuthService.isAdmin).toHaveBeenCalled();
    });

    it('should return false when user is not admin', () => {
      // Arrange
      mockAuthService.isAdmin.and.returnValue(false);

      // Act
      const result = component.isAdmin;

      // Assert
      expect(result).toBeFalse();
      expect(mockAuthService.isAdmin).toHaveBeenCalled();
    });
  });

  describe('Logout Functionality', () => {
    it('should call auth service logout and navigate to home', () => {
      // Act
      component.logout();

      // Assert
      expect(mockAuthService.logout).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should handle logout without navigation errors', () => {
      // Arrange
      (router.navigate as jasmine.Spy).and.returnValue(Promise.resolve(true));

      // Act
      expect(() => component.logout()).not.toThrow();

      // Assert
      expect(mockAuthService.logout).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should handle logout even if navigation fails', () => {
      // Arrange
      (router.navigate as jasmine.Spy).and.returnValue(Promise.reject('Navigation failed'));

      // Act
      expect(() => component.logout()).not.toThrow();

      // Assert
      expect(mockAuthService.logout).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/']);
    });
  });

  describe('Component Initialization', () => {
    it('should initialize without errors', () => {
      // Act
      fixture.detectChanges();

      // Assert
      expect(component).toBeTruthy();
    });

    it('should not call authentication methods during basic initialization', () => {
      // Act
      // Solo crear el componente sin detectChanges ni acceder a getters
      
      // Assert
      // Los getters no deberían haberse llamado todavía
      expect(mockAuthService.isAuthenticated).not.toHaveBeenCalled();
      expect(mockAuthService.isAdmin).not.toHaveBeenCalled();
    });
  });

  describe('Template Integration', () => {
    it('should trigger authentication check when isAuthenticated is accessed', () => {
      // Arrange
      mockAuthService.isAuthenticated.and.returnValue(true);

      // Act
      const result = component.isAuthenticated;
      fixture.detectChanges();

      // Assert
      expect(result).toBeTrue();
      expect(mockAuthService.isAuthenticated).toHaveBeenCalled();
    });

    it('should trigger admin check when isAdmin is accessed', () => {
      // Arrange
      mockAuthService.isAdmin.and.returnValue(true);

      // Act
      const result = component.isAdmin;
      fixture.detectChanges();

      // Assert
      expect(result).toBeTrue();
      expect(mockAuthService.isAdmin).toHaveBeenCalled();
    });
  });

  describe('Edge Cases', () => {
    it('should handle undefined authentication state gracefully', () => {
      // Arrange
      mockAuthService.isAuthenticated.and.returnValue(undefined as any);

      // Act
      const result = component.isAuthenticated;

      // Assert
      expect(result).toBeFalsy();
    });

    it('should handle undefined admin state gracefully', () => {
      // Arrange
      mockAuthService.isAdmin.and.returnValue(undefined as any);

      // Act
      const result = component.isAdmin;

      // Assert
      expect(result).toBeFalsy();
    });

    it('should handle auth service errors gracefully', () => {
      // Arrange
      mockAuthService.isAuthenticated.and.throwError('Auth service error');

      // Act & Assert
      expect(() => component.isAuthenticated).toThrow();
    });

    it('should handle logout service errors gracefully', () => {
      // Arrange
      mockAuthService.logout.and.throwError('Logout error');

      // Act & Assert
      expect(() => component.logout()).toThrow();
    });
  });
});