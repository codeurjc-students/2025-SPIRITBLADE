import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../service/auth.service';

describe('LoginComponent - Unit Tests', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    // Create spy objects
    mockAuthService = jasmine.createSpyObj('AuthService', ['login', 'register', 'checkSession', 'isAdmin']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize in login mode', () => {
    expect(component.isLoginMode).toBeTrue();
    expect(component.message).toBeNull();
  });

  it('should initialize forms with validators', () => {
    // Login form
    expect(component.loginForm.get('username')?.hasError('required')).toBeTrue();
    expect(component.loginForm.get('password')?.hasError('required')).toBeTrue();

    // Register form
    expect(component.registerForm.get('username')?.hasError('required')).toBeTrue();
    expect(component.registerForm.get('email')?.hasError('required')).toBeTrue();
    expect(component.registerForm.get('password')?.hasError('required')).toBeTrue();
    expect(component.registerForm.get('confirmPassword')?.hasError('required')).toBeTrue();
  });

  describe('Mode switching', () => {
    it('should switch to login mode', () => {
      component.isLoginMode = false;
      component.showLogin();
      expect(component.isLoginMode).toBeTrue();
    });

    it('should switch to register mode', () => {
      component.isLoginMode = true;
      component.showRegister();
      expect(component.isLoginMode).toBeFalse();
    });
  });

  describe('onLogin()', () => {
    beforeEach(() => {
      component.loginForm.patchValue({
        username: 'testuser',
        password: 'testpass'
      });
    });

    it('should login successfully and redirect to dashboard for regular user', (done) => {
      // Arrange
      const mockResponse = { 
        status: 'success',
        message: 'Login successful',
        accessToken: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token'
      };
      mockAuthService.login.and.returnValue(of(mockResponse));
      mockAuthService.checkSession.and.returnValue(of(true));
      mockAuthService.isAdmin.and.returnValue(false);

      // Act
      component.onLogin();

      // Assert
      expect(mockAuthService.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'testpass'
      });
      expect(component.message).toEqual({
        type: 'success',
        text: 'Login successful. Redirecting...'
      });

      // Check redirect after timeout
      setTimeout(() => {
        expect(mockAuthService.checkSession).toHaveBeenCalled();
        expect(mockAuthService.isAdmin).toHaveBeenCalled();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
        done();
      }, 650);
    });

    it('should login successfully and redirect to admin for admin user', (done) => {
      // Arrange
      const mockResponse = { 
        status: 'success',
        message: 'Login successful',
        accessToken: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token'
      };
      mockAuthService.login.and.returnValue(of(mockResponse));
      mockAuthService.checkSession.and.returnValue(of(true));
      mockAuthService.isAdmin.and.returnValue(true);

      // Act
      component.onLogin();

      // Assert
      expect(mockAuthService.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'testpass'
      });

      // Check redirect after timeout
      setTimeout(() => {
        expect(mockAuthService.checkSession).toHaveBeenCalled();
        expect(mockAuthService.isAdmin).toHaveBeenCalled();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin']);
        done();
      }, 650);
    });

    it('should handle 401 authentication error', () => {
      // Arrange
      const error = { status: 401 };
      mockAuthService.login.and.returnValue(throwError(() => error));

      // Act
      component.onLogin();

      // Assert
      expect(component.message).toEqual({
        type: 'error',
        text: 'Invalid credentials. Please check your username/password.'
      });
    });

    it('should handle 403 forbidden error', () => {
      // Arrange
      const error = { status: 403 };
      mockAuthService.login.and.returnValue(throwError(() => error));

      // Act
      component.onLogin();

      // Assert
      expect(component.message).toEqual({
        type: 'error',
        text: 'Invalid credentials. Please check your username/password.'
      });
    });

    it('should handle connection error', () => {
      // Arrange
      const error = { status: 0 };
      mockAuthService.login.and.returnValue(throwError(() => error));

      // Act
      component.onLogin();

      // Assert
      expect(component.message).toEqual({
        type: 'error',
        text: 'Could not connect to the server. Please ensure the backend is running.'
      });
    });

    it('should handle unexpected error', () => {
      // Arrange
      const error = { status: 500 };
      mockAuthService.login.and.returnValue(throwError(() => error));

      // Act
      component.onLogin();

      // Assert
      expect(component.message).toEqual({
        type: 'error',
        text: 'Unexpected error during login.'
      });
    });

    it('should handle checkSession error after successful login', (done) => {
      // Arrange
      const mockResponse = { 
        status: 'success',
        message: 'Login successful',
        accessToken: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token'
      };
      mockAuthService.login.and.returnValue(of(mockResponse));
      mockAuthService.checkSession.and.returnValue(throwError(() => new Error('Session check failed')));

      // Act
      component.onLogin();

      // Assert
      expect(mockAuthService.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'testpass'
      });

      // Check fallback redirect after timeout
      setTimeout(() => {
        expect(mockAuthService.checkSession).toHaveBeenCalled();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
        done();
      }, 650);
    });

    it('should handle nullish coalescing when form values are null', () => {
      // Arrange
      // Remove validators to allow null values
      component.loginForm.get('username')?.clearValidators();
      component.loginForm.get('password')?.clearValidators();
      component.loginForm.get('username')?.setValue(null);
      component.loginForm.get('password')?.setValue(null);
      component.loginForm.get('username')?.updateValueAndValidity();
      component.loginForm.get('password')?.updateValueAndValidity();
      
      const mockResponse = { 
        status: 'success',
        message: 'Login successful',
        accessToken: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token'
      };
      mockAuthService.login.and.returnValue(of(mockResponse));
      mockAuthService.checkSession.and.returnValue(of(true));
      mockAuthService.isAdmin.and.returnValue(false);

      // Act
      component.onLogin();

      // Assert
      expect(mockAuthService.login).toHaveBeenCalledWith({
        username: '',
        password: ''
      });
    });
  });

  describe('onRegister()', () => {
    beforeEach(() => {
      component.registerForm.patchValue({
        username: 'newuser',
        email: 'newuser@test.com',
        password: 'newpass',
        confirmPassword: 'newpass'
      });
    });

    it('should register successfully and auto-login for regular user', (done) => {
      // Arrange
      const mockRegisterResponse = {
        success: true,
        message: 'Registration successful',
        user: { id: 1, name: 'newuser', email: 'newuser@test.com' }
      };
      const mockLoginResponse = { 
        status: 'success',
        message: 'Login successful',
        accessToken: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token'
      };
      
      mockAuthService.register.and.returnValue(of(mockRegisterResponse));
      mockAuthService.login.and.returnValue(of(mockLoginResponse));
      mockAuthService.checkSession.and.returnValue(of(true));
      mockAuthService.isAdmin.and.returnValue(false);

      // Act
      component.onRegister();

      // Assert
      expect(mockAuthService.register).toHaveBeenCalledWith({
        name: 'newuser',
        email: 'newuser@test.com',
        password: 'newpass'
      });

      expect(mockAuthService.login).toHaveBeenCalledWith({
        username: 'newuser',
        password: 'newpass'
      });

      // Check redirect after timeout
      setTimeout(() => {
        expect(mockAuthService.checkSession).toHaveBeenCalled();
        expect(mockAuthService.isAdmin).toHaveBeenCalled();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
        done();
      }, 650);
    });

    it('should handle password mismatch', () => {
      // Arrange
      component.registerForm.patchValue({
        password: 'pass1',
        confirmPassword: 'pass2'
      });

      // Act
      component.onRegister();

      // Assert
      expect(mockAuthService.register).not.toHaveBeenCalled();
    });

    it('should handle registration conflict error', () => {
      // Arrange
      const error = { status: 409 };
      mockAuthService.register.and.returnValue(throwError(() => error));

      // Act
      component.onRegister();

      // Assert
      expect(component.message).toEqual({
        type: 'error',
        text: 'User already exists.'
      });
    });

    it('should handle registration validation error', () => {
      // Arrange
      const error = { status: 400 };
      mockAuthService.register.and.returnValue(throwError(() => error));

      // Act
      component.onRegister();

      // Assert
      expect(component.message).toEqual({
        type: 'error',
        text: 'Invalid registration data.'
      });
    });

    it('should handle auto-login failure after successful registration', () => {
      // Arrange
      const mockRegisterResponse = {
        success: true,
        message: 'Registration successful',
        user: { id: 1, name: 'newuser', email: 'newuser@test.com' }
      };
      const loginError = { status: 401 };
      
      mockAuthService.register.and.returnValue(of(mockRegisterResponse));
      mockAuthService.login.and.returnValue(throwError(() => loginError));

      // Act
      component.onRegister();

      // Assert
      expect(component.message).toEqual({
        type: 'info',
        text: 'Registration successful, but automatic login failed. Please try logging in manually.'
      });
    });

    it('should register successfully and auto-login for admin user', (done) => {
      // Arrange
      const mockRegisterResponse = {
        success: true,
        message: 'Registration successful',
        user: { id: 1, name: 'newuser', email: 'newuser@test.com' }
      };
      const mockLoginResponse = { 
        status: 'success',
        message: 'Login successful',
        accessToken: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token'
      };
      
      mockAuthService.register.and.returnValue(of(mockRegisterResponse));
      mockAuthService.login.and.returnValue(of(mockLoginResponse));
      mockAuthService.checkSession.and.returnValue(of(true));
      mockAuthService.isAdmin.and.returnValue(true);

      // Act
      component.onRegister();

      // Assert
      expect(mockAuthService.register).toHaveBeenCalledWith({
        name: 'newuser',
        email: 'newuser@test.com',
        password: 'newpass'
      });

      // Check redirect after timeout
      setTimeout(() => {
        expect(mockAuthService.checkSession).toHaveBeenCalled();
        expect(mockAuthService.isAdmin).toHaveBeenCalled();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/admin']);
        done();
      }, 650);
    });

    it('should handle checkSession error after successful registration and auto-login', (done) => {
      // Arrange
      const mockRegisterResponse = {
        success: true,
        message: 'Registration successful',
        user: { id: 1, name: 'newuser', email: 'newuser@test.com' }
      };
      const mockLoginResponse = { 
        status: 'success',
        message: 'Login successful',
        accessToken: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token'
      };
      
      mockAuthService.register.and.returnValue(of(mockRegisterResponse));
      mockAuthService.login.and.returnValue(of(mockLoginResponse));
      mockAuthService.checkSession.and.returnValue(throwError(() => new Error('Session check failed')));

      // Act
      component.onRegister();

      // Assert
      expect(mockAuthService.register).toHaveBeenCalledWith({
        name: 'newuser',
        email: 'newuser@test.com',
        password: 'newpass'
      });

      // Check fallback redirect after timeout
      setTimeout(() => {
        expect(mockAuthService.checkSession).toHaveBeenCalled();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
        done();
      }, 650);
    });

    it('should handle unexpected registration error', () => {
      // Arrange
      const error = { status: 500 };
      mockAuthService.register.and.returnValue(throwError(() => error));

      // Act
      component.onRegister();

      // Assert
      expect(component.message).toEqual({
        type: 'error',
        text: 'Error registering user.'
      });
    });

    it('should handle nullish coalescing when register form values are null', () => {
      // Arrange
      // Remove validators to allow null values
      component.registerForm.get('username')?.clearValidators();
      component.registerForm.get('email')?.clearValidators();
      component.registerForm.get('password')?.clearValidators();
      component.registerForm.get('confirmPassword')?.clearValidators();
      component.registerForm.get('username')?.setValue(null);
      component.registerForm.get('email')?.setValue(null);
      component.registerForm.get('password')?.setValue(null);
      component.registerForm.get('confirmPassword')?.setValue(null);
      component.registerForm.get('username')?.updateValueAndValidity();
      component.registerForm.get('email')?.updateValueAndValidity();
      component.registerForm.get('password')?.updateValueAndValidity();
      component.registerForm.get('confirmPassword')?.updateValueAndValidity();
      
      const mockRegisterResponse = {
        success: true,
        message: 'Registration successful',
        user: { id: 1, name: 'newuser', email: 'newuser@test.com' }
      };
      const mockLoginResponse = { 
        status: 'success',
        message: 'Login successful',
        accessToken: 'mock-jwt-token',
        refreshToken: 'mock-refresh-token'
      };
      
      mockAuthService.register.and.returnValue(of(mockRegisterResponse));
      mockAuthService.login.and.returnValue(of(mockLoginResponse));
      mockAuthService.checkSession.and.returnValue(of(true));
      mockAuthService.isAdmin.and.returnValue(false);

      // Act
      component.onRegister();

      // Assert
      expect(mockAuthService.register).toHaveBeenCalledWith({
        name: '',
        email: '',
        password: ''
      });
    });
  });
});