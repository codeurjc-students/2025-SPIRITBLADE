import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { AdminGuard } from './admin.guard';
import { AuthService } from '../auth.service';

describe('AdminGuard', () => {
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['checkSession', 'isAdmin']);
    mockRouter = jasmine.createSpyObj('Router', ['parseUrl']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });
  });

  it('should allow access for authenticated admin users', (done) => {
    mockAuthService.checkSession.and.returnValue(of(true));
    mockAuthService.isAdmin.and.returnValue(true);

    TestBed.runInInjectionContext(() => {
      const result = AdminGuard(null as any, null as any) as Observable<any>;
      
      result.subscribe((canActivate: any) => {
        expect(canActivate).toBe(true);
        expect(mockAuthService.checkSession).toHaveBeenCalled();
        expect(mockAuthService.isAdmin).toHaveBeenCalled();
        done();
      });
    });
  });

  it('should redirect to login for unauthenticated users', (done) => {
    const loginUrl = '/login';
    mockAuthService.checkSession.and.returnValue(of(false));
    mockRouter.parseUrl.and.returnValue(loginUrl as any);

    TestBed.runInInjectionContext(() => {
      const result = AdminGuard(null as any, null as any) as Observable<any>;
      
      result.subscribe((canActivate: any) => {
        expect(canActivate).toBe(loginUrl as any);
        expect(mockRouter.parseUrl).toHaveBeenCalledWith('/login');
        expect(mockAuthService.isAdmin).not.toHaveBeenCalled();
        done();
      });
    });
  });

  it('should redirect to dashboard for authenticated non-admin users', (done) => {
    const dashboardUrl = '/dashboard';
    mockAuthService.checkSession.and.returnValue(of(true));
    mockAuthService.isAdmin.and.returnValue(false);
    mockRouter.parseUrl.and.returnValue(dashboardUrl as any);

    TestBed.runInInjectionContext(() => {
      const result = AdminGuard(null as any, null as any) as Observable<any>;
      
      result.subscribe((canActivate: any) => {
        expect(canActivate).toBe(dashboardUrl as any);
        expect(mockAuthService.checkSession).toHaveBeenCalled();
        expect(mockAuthService.isAdmin).toHaveBeenCalled();
        expect(mockRouter.parseUrl).toHaveBeenCalledWith('/dashboard');
        done();
      });
    });
  });
});
