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
    mockAuthService = jasmine.createSpyObj('AuthService', ['login', 'register']);
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

    it('should login successfully and redirect', (done) => {
      // Arrange
      const mockResponse = { token: 'mock-jwt-token' };
      mockAuthService.login.and.returnValue(of(mockResponse));

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
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/profile']);
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

    it('should not login with invalid form', () => {
      // Arrange
      component.loginForm.patchValue({
        username: '',
        password: ''
      });

      // Act
      component.onLogin();

      // Assert
      expect(mockAuthService.login).not.toHaveBeenCalled();
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

    it('should register successfully and auto-login', (done) => {
      // Arrange
      const mockRegisterResponse = { id: 1, username: 'newuser' };
      const mockLoginResponse = { token: 'mock-jwt-token' };
      
      mockAuthService.register.and.returnValue(of(mockRegisterResponse));
      mockAuthService.login.and.returnValue(of(mockLoginResponse));

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
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/profile']);
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
      const mockRegisterResponse = { id: 1, username: 'newuser' };
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

    it('should not register with invalid form', () => {
      // Arrange
      component.registerForm.patchValue({
        username: '',
        email: 'invalid-email',
        password: '',
        confirmPassword: ''
      });

      // Act
      component.onRegister();

      // Assert
      expect(mockAuthService.register).not.toHaveBeenCalled();
    });
  });
});