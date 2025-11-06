import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ErrorComponent } from './error.component';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';

describe('ErrorComponent', () => {
  let component: ErrorComponent;
  let fixture: ComponentFixture<ErrorComponent>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockActivatedRoute: any;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockActivatedRoute = {
      queryParams: of({
        code: '404',
        message: 'Not Found',
        details: 'The requested resource was not found'
      })
    };

    await TestBed.configureTestingModule({
      imports: [ErrorComponent],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load error data from query params', () => {
    expect(component.errorCode).toBe('404');
    expect(component.errorMessage).toBe('Not Found');
    expect(component.errorDetails).toBe('The requested resource was not found');
  });

  it('should navigate to home when goHome is called', () => {
    component.goHome();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should use default values when no query params are provided', () => {
    mockActivatedRoute.queryParams = of({});
    const newComponent = new ErrorComponent(mockActivatedRoute, mockRouter);
    newComponent.ngOnInit();
    
    expect(newComponent.errorCode).toBe('500');
    expect(newComponent.errorMessage).toBe('An unexpected error occurred');
  });
});
