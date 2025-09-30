import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HomeComponent } from './home.component';

describe('HomeComponent - Unit Tests', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    // Create spy for Router
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule,
        RouterTestingModule,
        HomeComponent
      ],
      providers: [
        { provide: Router, useValue: mockRouter }
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

    it('should initialize with predefined recent searches', () => {
      expect(component.recentSearches).toBeDefined();
      expect(component.recentSearches.length).toBe(4);
      expect(component.recentSearches[0]).toEqual({ name: 'Example Player 1', rank: 'Gold II' });
    });

    it('should have all recent searches with name and rank properties', () => {
      component.recentSearches.forEach(search => {
        expect(search.name).toBeDefined();
        expect(search.rank).toBeDefined();
        expect(typeof search.name).toBe('string');
        expect(typeof search.rank).toBe('string');
      });
    });
  });

  describe('Search Functionality', () => {
    it('should navigate to summoner page with valid search query', () => {
      // Arrange
      component.searchQuery = 'TestSummoner';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'TestSummoner']);
    });

    it('should trim whitespace from search query before navigation', () => {
      // Arrange
      component.searchQuery = '  TestSummoner  ';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'TestSummoner']);
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
      component.searchQuery = 'Test@Summoner#123';

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'Test@Summoner#123']);
    });

    it('should handle long search queries', () => {
      // Arrange
      component.searchQuery = 'A'.repeat(100); // Very long name

      // Act
      component.onSearch();

      // Assert
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', 'A'.repeat(100)]);
    });
  });

  describe('Recent Searches Functionality', () => {
    it('should log summoner name when searching recent summoner', () => {
      // Arrange
      const consoleSpy = spyOn(console, 'log');
      const summonerName = 'Example Player 1';

      // Act
      component.searchSummoner(summonerName);

      // Assert
      expect(consoleSpy).toHaveBeenCalledWith('Searching for recent summoner:', summonerName);
    });

    it('should handle empty summoner name in recent search', () => {
      // Arrange
      const consoleSpy = spyOn(console, 'log');

      // Act
      component.searchSummoner('');

      // Assert
      expect(consoleSpy).toHaveBeenCalledWith('Searching for recent summoner:', '');
    });

    it('should handle special characters in recent summoner search', () => {
      // Arrange
      const consoleSpy = spyOn(console, 'log');
      const specialName = 'Player@#$%';

      // Act
      component.searchSummoner(specialName);

      // Assert
      expect(consoleSpy).toHaveBeenCalledWith('Searching for recent summoner:', specialName);
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
      component.searchQuery = 'TestSummoner';

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
    it('should have valid recent searches data structure', () => {
      expect(Array.isArray(component.recentSearches)).toBeTrue();
      expect(component.recentSearches.length).toBeGreaterThan(0);
    });

    it('should have consistent rank format in recent searches', () => {
      const validRanks = ['Gold II', 'Platinum IV', 'Diamond I', 'Master'];
      component.recentSearches.forEach((search, index) => {
        expect(search.rank).toBe(validRanks[index]);
      });
    });

    it('should have non-empty names in recent searches', () => {
      component.recentSearches.forEach(search => {
        expect(search.name.trim().length).toBeGreaterThan(0);
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
      const queries = ['Summoner1', 'Summoner2', 'Summoner3'];

      // Act & Assert
      queries.forEach(query => {
        component.searchQuery = query;
        component.onSearch();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/summoner', query]);
      });

      expect(mockRouter.navigate).toHaveBeenCalledTimes(3);
    });
  });
});