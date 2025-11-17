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
      'getRecentMatches',
      'getMatchDetails'
    ]);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockActivatedRoute = {
      paramMap: of(new Map([['name', 'TestSummoner']]))
    };

    // Set default return values for all service methods
    mockSummonerService.getByName.and.returnValue(of({} as any));
    mockSummonerService.getTopChampions.and.returnValue(of([]));
    mockSummonerService.getRecentMatches.and.returnValue(of([]));
    mockSummonerService.getMatchDetails.and.returnValue(of({} as any));

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

  describe('Data Loading', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should load summoner data on init', () => {
      const summoner: Summoner = { id: '123', name: 'TestSummoner', level: 50, profileIconId: 1 };
      mockSummonerService.getByName.and.returnValue(of(summoner));

      component.ngOnInit();

      expect(mockSummonerService.getByName).toHaveBeenCalledWith('TestSummoner');
      expect(component.summoner).toEqual(summoner);
    });

    it('should load top champions', () => {
      const champions = [{ championId: 1, championName: 'Ahri' }];
      mockSummonerService.getTopChampions.and.returnValue(of(champions));

      component.loadSummoner('TestSummoner');

      expect(mockSummonerService.getTopChampions).toHaveBeenCalledWith('TestSummoner');
      expect(component.championMasteries).toEqual(champions);
    });

    it('should load recent matches', () => {
      const matches = [{ matchId: 'match1' }];
      mockSummonerService.getRecentMatches.and.returnValue(of(matches));

      component.loadSummoner('TestSummoner');

      expect(mockSummonerService.getRecentMatches).toHaveBeenCalledWith('TestSummoner', 0, 5);
      expect(component.matchHistory).toEqual(matches);
    });

    it('should handle summoner not found', () => {
      mockSummonerService.getByName.and.returnValue(throwError(() => ({ status: 404 })));
      component.searchQuery = 'InvalidSummoner#EUW';

      component.loadSummoner('InvalidSummoner#EUW');

      expect(component.error).toBe('Summoner "InvalidSummoner#EUW" not found. Make sure to use the format: gameName#tagLine');
      expect(component.loading).toBeFalse();
    });
  });

  describe('Navigation', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should navigate to summoner on valid search', () => {
      component.searchQuery = 'ValidSummoner#1234';

      component.onSearch();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'ValidSummoner#1234']);
    });

    it('should not navigate on invalid search', () => {
      component.searchQuery = 'invalid';

      component.onSearch();

      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });
  });

  describe('Pagination', () => {
    beforeEach(() => {
      fixture.detectChanges();
      component.summoner = { id: '123', name: 'TestSummoner', level: 50, profileIconId: 1 };
    });

    it('should load more matches', () => {
      component.hasMoreMatches = true;
      component.currentMatchPage = 0;
      component.searchQuery = 'TestSummoner';
      mockSummonerService.getRecentMatches.and.returnValue(of([]));

      component.loadNextMatchPage();

      expect(component.currentMatchPage).toBe(1);
      expect(mockSummonerService.getRecentMatches).toHaveBeenCalledWith('TestSummoner', 1, 5);
    });

    it('should not load more if no more matches', () => {
      component.hasMoreMatches = false;
      mockSummonerService.getRecentMatches.calls.reset();

      component.loadNextMatchPage();

      expect(mockSummonerService.getRecentMatches).not.toHaveBeenCalled();
    });
  });

  describe('Match Details', () => {
    beforeEach(() => {
      fixture.detectChanges();
      component.summoner = { id: '123', name: 'TestSummoner', level: 50, profileIconId: 1 };
    });

    it('should toggle match expansion', () => {
      component.toggleMatchDetails('match1');
      expect(component.isMatchExpanded('match1')).toBeTrue();

      component.toggleMatchDetails('match1');
      expect(component.isMatchExpanded('match1')).toBeFalse();
    });

    it('should load match details when expanding', () => {
      const matchDetail = { matchId: 'match1', participants: [] };
      mockSummonerService.getMatchDetails.and.returnValue(of(matchDetail));

      component.toggleMatchDetails('match1');

      expect(component.isMatchExpanded('match1')).toBeTrue();
      expect(component.getMatchDetail('match1')).toEqual(matchDetail);
    });

    it('should handle match details error', () => {
      mockSummonerService.getMatchDetails.and.returnValue(throwError(() => new Error('Error')));

      component.toggleMatchDetails('match1');

      expect(component.isMatchExpanded('match1')).toBeTrue();
      expect(component.getMatchDetail('match1')).toBeNull();
    });
  });

  describe('Search Validation', () => {
    it('should validate search format with missing #', () => {
      component.searchQuery = 'SummonerWithoutTag';

      component.onSearch();

      expect(component.error).toBe('Invalid format. Please use: gameName#tagLine (e.g., Player#EUW)');
    });

    it('should validate search format with empty parts', () => {
      component.searchQuery = '#EUW';

      component.onSearch();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', '#EUW']);
    });

    it('should validate search format with empty tag', () => {
      component.searchQuery = 'Summoner#';

      component.onSearch();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'Summoner#']);
    });
  });
});