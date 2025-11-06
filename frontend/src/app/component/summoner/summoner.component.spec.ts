import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SummonerComponent } from './summoner.component';
import { SummonerService } from '../../service/summoner.service';
import { Summoner } from '../../dto/summoner.model';

describe('SummonerComponent - Unit Tests', () => {
  let component: SummonerComponent;
  let fixture: ComponentFixture<SummonerComponent>;
  let mockSummonerService: jasmine.SpyObj<SummonerService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockActivatedRoute: any;

  beforeEach(async () => {
    // Create spy objects
    mockSummonerService = jasmine.createSpyObj('SummonerService', [
      'getByName',
      'getTopChampions',
      'getRecentMatches'
    ]);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockActivatedRoute = {
      paramMap: of(new Map([['name', 'TestSummoner']]))
    };

    // Set default return values for all service methods
    mockSummonerService.getByName.and.returnValue(of({} as any));
    mockSummonerService.getTopChampions.and.returnValue(of([]));
    mockSummonerService.getRecentMatches.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [SummonerComponent],
      providers: [
        { provide: SummonerService, useValue: mockSummonerService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SummonerComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.searchQuery).toBe('');
    expect(component.summoner).toBeNull();
    expect(component.loading).toBeFalse();
    expect(component.error).toBeNull();
  });

  describe('ngOnInit()', () => {
    it('should load summoner from route params', () => {
      // Arrange
      const mockSummoner: Summoner = {
        id: '1',
        name: 'TestSummoner',
        level: 100,
        profileIconId: 123,
        tier: 'GOLD',
        rank: 'II',
        lp: 85
      };

      mockSummonerService.getByName.and.returnValue(of(mockSummoner));
      spyOn(component, 'loadSummoner');

      // Act
      component.ngOnInit();

      // Assert
      expect(component.searchQuery).toBe('TestSummoner');
      expect(component.loadSummoner).toHaveBeenCalledWith('TestSummoner');
    });

    it('should not load summoner when no name in route params', () => {
      // Arrange
      mockActivatedRoute.paramMap = of(new Map());
      spyOn(component, 'loadSummoner');

      // Act
      component.ngOnInit();

      // Assert
      expect(component.loadSummoner).not.toHaveBeenCalled();
    });
  });

  describe('loadSummoner()', () => {
    it('should load summoner successfully', () => {
      // Arrange
      const mockSummoner: Summoner = {
        id: '1',
        name: 'TestSummoner',
        level: 100,
        profileIconId: 123,
        tier: 'GOLD',
        rank: 'II',
        lp: 85
      };

      mockSummonerService.getByName.and.returnValue(of(mockSummoner));

      // Act
      component.loadSummoner('TestSummoner');

      // Assert
      expect(component.loading).toBeFalse();
      expect(component.summoner).toEqual(mockSummoner);
      expect(component.error).toBeNull();
      expect(mockSummonerService.getByName).toHaveBeenCalledWith('TestSummoner');
    });

    it('should handle summoner loading error', () => {
      // Arrange
      mockSummonerService.getByName.and.returnValue(
        throwError(() => new Error('Summoner not found'))
      );

      // Act
      component.loadSummoner('NonExistentSummoner');

      // Assert
      expect(component.loading).toBeFalse();
      expect(component.summoner).toBeNull();
      expect(component.error).toBe('Unable to load summoner. Please try again later.');
    });

    it('should reset states before loading', () => {
      // Arrange
      component.summoner = { 
        id: '1', 
        name: 'OldSummoner', 
        level: 50, 
        profileIconId: 100 
      } as Summoner;
      component.error = 'Previous error';
      mockSummonerService.getByName.and.returnValue(of({} as Summoner));

      // Act
      component.loadSummoner('NewSummoner');

      // Assert
      expect(component.error).toBeNull();
      // summoner should be updated with new data
    });
  });

  describe('onSearch()', () => {
    it('should navigate to summoner route with valid name', () => {
      // Arrange
      component.searchQuery = 'NewSummoner#EUW';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'NewSummoner#EUW']);
      expect(component.error).toBeNull();
    });

    it('should not navigate with empty search query', () => {
      // Arrange
      component.searchQuery = '';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).not.toHaveBeenCalled();
      expect(component.error).toBe('Please enter a summoner name');
    });

    it('should not navigate with whitespace-only search query', () => {
      // Arrange
      component.searchQuery = '   ';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).not.toHaveBeenCalled();
      expect(component.error).toBe('Please enter a summoner name');
    });

    it('should trim search query before navigation', () => {
      // Arrange
      component.searchQuery = '  TrimmedSummoner#TAG  ';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'TrimmedSummoner#TAG']);
      expect(component.error).toBeNull();
    });

    it('should show error for invalid format without #', () => {
      // Arrange
      component.searchQuery = 'InvalidSummoner';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).not.toHaveBeenCalled();
      expect(component.error).toBe('Invalid format. Please use: gameName#tagLine (e.g., Player#EUW)');
    });
  });
});