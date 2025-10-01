import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { DashboardService } from '../../service/dashboard.service';

describe('DashboardComponent - Unit Tests', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let mockDashboardService: jasmine.SpyObj<DashboardService>;

  beforeEach(async () => {
    // Create spy object for DashboardService
    mockDashboardService = jasmine.createSpyObj('DashboardService', [
      'getPersonalStats',
      'getFavoritesOverview'
    ]);

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: DashboardService, useValue: mockDashboardService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.loading).toBeFalse();
    expect(component.stats).toBeNull();
    expect(component.favorites).toEqual([]);
    expect(component.favoritesLoading).toBeFalse();
    expect(component.favoritesError).toBeNull();
    expect(component.error).toBeNull();
  });

  describe('refresh()', () => {
    it('should load personal stats successfully', () => {
      // Arrange
      const mockStats = { gamesPlayed: 100, winRate: 0.65 };
      const mockFavorites = [{ name: 'TestPlayer', rank: 'Gold' }];
      
      mockDashboardService.getPersonalStats.and.returnValue(of(mockStats));
      mockDashboardService.getFavoritesOverview.and.returnValue(of(mockFavorites));

      // Act
      component.refresh();

      // Assert
      expect(component.loading).toBeFalse();
      expect(component.stats).toEqual(mockStats);
      expect(component.error).toBeNull();
      expect(component.favorites).toEqual(mockFavorites);
      expect(component.favoritesLoading).toBeFalse();
      expect(component.favoritesError).toBeNull();
    });

    it('should handle personal stats error', () => {
      // Arrange
      const mockFavorites = [{ name: 'TestPlayer', rank: 'Gold' }];
      
      mockDashboardService.getPersonalStats.and.returnValue(
        throwError(() => new Error('Stats API error'))
      );
      mockDashboardService.getFavoritesOverview.and.returnValue(of(mockFavorites));

      // Act
      component.refresh();

      // Assert
      expect(component.loading).toBeFalse();
      expect(component.stats).toBeNull();
      expect(component.error).toBe('No se pudieron obtener las estadísticas del dashboard.');
      expect(component.favorites).toEqual(mockFavorites);
    });

    it('should handle favorites error', () => {
      // Arrange
      const mockStats = { gamesPlayed: 100, winRate: 0.65 };
      
      mockDashboardService.getPersonalStats.and.returnValue(of(mockStats));
      mockDashboardService.getFavoritesOverview.and.returnValue(
        throwError(() => new Error('Favorites API error'))
      );

      // Act
      component.refresh();

      // Assert
      expect(component.stats).toEqual(mockStats);
      expect(component.favoritesLoading).toBeFalse();
      expect(component.favoritesError).toBe('No se pudieron obtener los favoritos.');
      expect(component.favorites).toEqual([]);
    });

    it('should set loading states correctly during requests', () => {
      // Arrange
      mockDashboardService.getPersonalStats.and.returnValue(of({}));
      mockDashboardService.getFavoritesOverview.and.returnValue(of([]));

      // Act & Assert - Initial loading states
      expect(component.loading).toBeFalse();
      expect(component.favoritesLoading).toBeFalse();

      component.refresh();

      // Should finish loading after synchronous observable completion
      expect(component.loading).toBeFalse();
      expect(component.favoritesLoading).toBeFalse();
    });
  });

  describe('ngOnInit()', () => {
    it('should call refresh on initialization', () => {
      // Arrange
      spyOn(component, 'refresh');
      mockDashboardService.getPersonalStats.and.returnValue(of({}));
      mockDashboardService.getFavoritesOverview.and.returnValue(of([]));

      // Act
      component.ngOnInit();

      // Assert
      expect(component.refresh).toHaveBeenCalled();
    });
  });
});