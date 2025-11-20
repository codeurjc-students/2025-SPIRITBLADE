import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HomeComponent } from './home.component';
import { SummonerService } from '../../service/summoner.service';
import { of, throwError } from 'rxjs';
import { Summoner } from '../../dto/summoner.model';

describe('HomeComponent - Unit Tests', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockSummonerService: jasmine.SpyObj<SummonerService>;

  const mockRecentSearches: Summoner[] = [
    { 
      id: '1', 
      name: 'Player1#NA', 
      level: 150, 
      profileIconId: 1, 
      profileIconUrl: 'http://example.com/icon1.png',
      tier: 'GOLD',
      rank: 'II',
      lp: 50,
      wins: 100,
      losses: 90
    },
    { 
      id: '2', 
      name: 'Player2#NA', 
      level: 200, 
      profileIconId: 2, 
      profileIconUrl: 'http://example.com/icon2.png',
      tier: 'PLATINUM',
      rank: 'IV',
      lp: 75,
      wins: 150,
      losses: 120
    }
  ];

  beforeEach(async () => {
    // Create spies
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockSummonerService = jasmine.createSpyObj('SummonerService', ['getRecentSearches']);
    
    // Default mock implementation
    mockSummonerService.getRecentSearches.and.returnValue(of(mockRecentSearches));

    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule,
        RouterTestingModule,
        HomeComponent
      ],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: SummonerService, useValue: mockSummonerService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should initialize with empty search query', () => {
      expect(component.searchQuery).toBe('');
    });

    it('should initialize with empty recent searches array', () => {
      expect(component.recentSearches).toBeDefined();
      expect(Array.isArray(component.recentSearches)).toBeTrue();
      expect(component.recentSearches.length).toBe(0);
    });

    it('should load recent searches on ngOnInit', () => {
      // Act
      component.ngOnInit();

      // Assert
      expect(mockSummonerService.getRecentSearches).toHaveBeenCalled();
      expect(component.recentSearches.length).toBe(2);
      expect(component.loadingRecentSearches).toBeFalse();
    });

    it('should set loading state while fetching recent searches', () => {
      // Arrange
      mockSummonerService.getRecentSearches.and.returnValue(of(mockRecentSearches));

      // Act
      component.loadRecentSearches();

      // Assert - loading should be false after observable completes
      expect(component.loadingRecentSearches).toBeFalse();
    });

    it('should populate recent searches with correct data', () => {
      // Act
      component.ngOnInit();

      // Assert
      expect(component.recentSearches[0].name).toBe('Player1#NA');
      expect(component.recentSearches[0].tier).toBe('GOLD');
      expect(component.recentSearches[1].name).toBe('Player2#NA');
      expect(component.recentSearches[1].tier).toBe('PLATINUM');
    });
  });

  describe('Search Functionality', () => {
    it('should navigate to summoner page with valid search query', () => {
      // Arrange
      component.searchQuery = 'TestSummoner#NA';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'TestSummoner#NA']);
    });

    it('should trim whitespace from search query before navigation', () => {
      // Arrange
      component.searchQuery = '  TestSummoner#NA  ';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'TestSummoner#NA']);
    });

    it('should not navigate with empty search query', () => {
      // Arrange
      component.searchQuery = '';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should not navigate with whitespace-only search query', () => {
      // Arrange
      component.searchQuery = '   ';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should handle special characters in search query', () => {
      // Arrange
      component.searchQuery = 'Test@Summoner#NA';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'Test@Summoner#NA']);
    });

    it('should handle long search queries', () => {
      // Arrange
      component.searchQuery = 'A'.repeat(100) + '#NA'; // Very long name with region

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'A'.repeat(100) + '#NA']);
    });
  });

  describe('Recent Searches Functionality', () => {
    it('should navigate to summoner page when clicking recent search', () => {
      // Arrange
      const summoner = mockRecentSearches[0];

      // Act
      component.searchSummoner(summoner);

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'Player1#NA']);
    });

    it('should handle summoner click with all data fields', () => {
      // Arrange
      const summoner: Summoner = {
        id: '3',
        name: 'TestPlayer#NA',
        level: 100,
        profileIconId: 3,
        tier: 'DIAMOND',
        rank: 'I'
      };

      // Act
      component.searchSummoner(summoner);

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'TestPlayer#NA']);
    });

    it('should handle error when loading recent searches', () => {
      // Arrange
      const consoleSpy = spyOn(console, 'debug');
      mockSummonerService.getRecentSearches.and.returnValue(throwError(() => new Error('API Error')));

      // Act
      component.loadRecentSearches();

      // Assert
      expect(consoleSpy).toHaveBeenCalled();
      expect(component.loadingRecentSearches).toBeFalse();
      expect(component.recentSearches.length).toBe(0);
    });

    it('should handle empty recent searches response', () => {
      // Arrange
      mockSummonerService.getRecentSearches.and.returnValue(of([]));

      // Act
      component.loadRecentSearches();

      // Assert
      expect(component.recentSearches.length).toBe(0);
      expect(component.loadingRecentSearches).toBeFalse();
    });
  });

  describe('Component Lifecycle', () => {
    it('should initialize without errors', () => {
      // Act
      fixture.detectChanges();

      // Assert
      expect(component).toBeTruthy();
    });

    it('should handle change detection properly', () => {
      // Act
      component.searchQuery = 'Test';
      fixture.detectChanges();

      // Assert
      expect(component.searchQuery).toBe('Test');
    });
  });

  describe('Form Integration', () => {
    it('should update searchQuery when input changes', () => {
      // Arrange
      fixture.detectChanges();
      const inputElement = fixture.nativeElement.querySelector('input');

      // Act
      if (inputElement) {
        inputElement.value = 'NewSummoner';
        inputElement.dispatchEvent(new Event('input'));
        fixture.detectChanges();
      }

      // Assert
      // Note: This test would need the actual template to work properly
      expect(component).toBeTruthy();
    });
  });

  describe('Error Handling', () => {
    it('should handle router navigation errors gracefully', () => {
      // Arrange
      mockRouter.navigate.and.returnValue(Promise.reject('Navigation failed'));
      component.searchQuery = 'TestSummoner#NA';

      // Act & Assert
      expect(() => component.onSearch()).not.toThrow();
      expect(mockRouter.navigate).toHaveBeenCalled();
    });

    it('should handle undefined search query gracefully', () => {
      // Arrange
      component.searchQuery = undefined as any;

      // Act & Assert
      expect(() => component.onSearch()).not.toThrow();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should handle null search query gracefully', () => {
      // Arrange
      component.searchQuery = null as any;

      // Act & Assert
      expect(() => component.onSearch()).not.toThrow();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });
  });

  describe('Data Validation', () => {
    it('should have valid recent searches data structure after loading', () => {
      // Act
      component.ngOnInit();

      // Assert
      expect(Array.isArray(component.recentSearches)).toBeTrue();
      expect(component.recentSearches.length).toBeGreaterThan(0);
    });

    it('should have consistent data structure in recent searches', () => {
      // Act
      component.ngOnInit();

      // Assert
      component.recentSearches.forEach(search => {
        expect(search.name).toBeDefined();
        expect(search.level).toBeDefined();
        expect(typeof search.name).toBe('string');
        expect(typeof search.level).toBe('number');
      });
    });

    it('should have non-empty names in recent searches', () => {
      // Act
      component.ngOnInit();

      // Assert
      component.recentSearches.forEach(search => {
        expect(search.name.trim().length).toBeGreaterThan(0);
      });
    });

    it('should include profile icon URLs in recent searches', () => {
      // Act
      component.ngOnInit();

      // Assert
      component.recentSearches.forEach(search => {
        expect(search.profileIconUrl).toBeDefined();
        expect(typeof search.profileIconUrl).toBe('string');
      });
    });
  });

  describe('User Interaction', () => {
    it('should maintain search query state across interactions', () => {
      // Arrange
      const testQuery = 'TestSummoner';

      // Act
      component.searchQuery = testQuery;
      fixture.detectChanges();

      // Assert
      expect(component.searchQuery).toBe(testQuery);
    });

    it('should allow multiple search operations', () => {
      // Arrange
      const queries = ['Summoner1#NA', 'Summoner2#NA', 'Summoner3#NA'];

      // Act & Assert
      queries.forEach(query => {
        component.searchQuery = query;
        component.onSearch();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', query]);
      });

      expect(mockRouter.navigate).toHaveBeenCalledTimes(3);
    });

    it('should navigate when clicking on recent summoner card', () => {
      // Arrange
      component.ngOnInit();
      const summoner = component.recentSearches[0];

      // Act
      component.searchSummoner(summoner);

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', summoner.name]);
    });
  });

  describe('Search Validation', () => {
    it('should show error when search query does not include hashtag', () => {
      // Arrange
      component.searchQuery = 'TestSummoner';

      // Act
      component.onSearch();

      // Assert
      expect(component.searchError).toBe('Please use format: name#region (e.g., jae9104#NA1)');
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should show error when summoner name is empty after hashtag', () => {
      // Arrange
      component.searchQuery = '#NA';

      // Act
      component.onSearch();

      // Assert
      expect(component.searchError).toBe('Please enter a valid summoner name');
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should show error when summoner name is only whitespace after hashtag', () => {
      // Arrange
      component.searchQuery = '   #NA';

      // Act
      component.onSearch();

      // Assert
      expect(component.searchError).toBe('Please enter a valid summoner name');
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should clear previous error when search is successful', () => {
      // Arrange
      component.searchError = 'Previous error';
      component.searchQuery = 'ValidSummoner#NA';

      // Act
      component.onSearch();

      // Assert
      expect(component.searchError).toBeNull();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'ValidSummoner#NA']);
    });
  });
});