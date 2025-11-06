import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DashboardService } from './dashboard.service';
import { API_URL } from './api.config';

describe('DashboardService - Unit Tests', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DashboardService]
    });
    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getPersonalStats()', () => {
    it('should fetch personal dashboard statistics', () => {
      // Arrange
      const mockPersonalStats = {
        totalMatches: 234,
        winRate: 0.68,
        currentRank: 'Gold II',
        lp: 85,
        mainChampions: [
          { name: 'Jinx', gamesPlayed: 45, winRate: 0.73 },
          { name: 'Ashe', gamesPlayed: 32, winRate: 0.65 }
        ],
        recentMatches: [
          { id: 1, champion: 'Jinx', result: 'WIN', kda: '12/3/8' },
          { id: 2, champion: 'Ashe', result: 'LOSS', kda: '8/5/12' }
        ]
      };

      // Act
      service.getPersonalStats().subscribe((stats: any) => {
        expect(stats).toEqual(mockPersonalStats);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/stats`);
      expect(req.request.method).toBe('GET');
      req.flush(mockPersonalStats);
    });

    it('should handle empty personal stats', () => {
      // Arrange
      const mockEmptyStats = {
        totalMatches: 0,
        winRate: 0,
        currentRank: null,
        lp: 0,
        mainChampions: [],
        recentMatches: []
      };

      // Act
      service.getPersonalStats().subscribe((stats: any) => {
        expect(stats).toEqual(mockEmptyStats);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/stats`);
      req.flush(mockEmptyStats);
    });

    it('should handle unauthorized access', () => {
      // Act
      service.getPersonalStats().subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(401);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/stats`);
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle server error', () => {
      // Act
      service.getPersonalStats().subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(500);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/stats`);
      req.flush('Internal server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('getFavoritesOverview()', () => {
    it('should fetch favorites overview', () => {
      // Arrange
      const mockFavorites = {
        favoriteSummoners: [
          { id: '1', name: 'ProPlayer1', rank: 'Master', lp: 234 },
          { id: '2', name: 'ProPlayer2', rank: 'Grandmaster', lp: 567 }
        ],
        favoriteChampions: [
          { name: 'Jinx', playCount: 45, lastPlayed: new Date() },
          { name: 'Ashe', playCount: 32, lastPlayed: new Date() }
        ],
        watchedMatches: [
          { id: 123, summoner: 'ProPlayer1', champion: 'Jinx', timestamp: new Date() }
        ]
      };

      // Act
      service.getFavoritesOverview().subscribe((favorites: any) => {
        expect(favorites).toEqual(mockFavorites);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/favorites`);
      expect(req.request.method).toBe('GET');
      req.flush(mockFavorites);
    });

    it('should handle empty favorites', () => {
      // Arrange
      const mockEmptyFavorites = {
        favoriteSummoners: [],
        favoriteChampions: [],
        watchedMatches: []
      };

      // Act
      service.getFavoritesOverview().subscribe((favorites: any) => {
        expect(favorites).toEqual(mockEmptyFavorites);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/favorites`);
      req.flush(mockEmptyFavorites);
    });

    it('should handle unauthorized access for favorites', () => {
      // Act
      service.getFavoritesOverview().subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(401);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/favorites`);
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle server error for favorites', () => {
      // Act
      service.getFavoritesOverview().subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(500);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/dashboard/me/favorites`);
      req.flush('Internal server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });
});
