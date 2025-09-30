import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SummonerService } from './summoner.service';
import { Summoner } from '../dto/summoner.model';
import { API_URL } from './api.config';

describe('SummonerService - Unit Tests', () => {
  let service: SummonerService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SummonerService]
    });
    service = TestBed.inject(SummonerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getByName()', () => {
    it('should fetch summoner by name', () => {
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
      const summonerName = 'TestSummoner';

      // Act
      service.getByName(summonerName).subscribe(summoner => {
        expect(summoner).toEqual(mockSummoner);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/name/${encodeURIComponent(summonerName)}`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockSummoner);
    });

    it('should handle summoner name with special characters', () => {
      // Arrange
      const summonerName = 'Test Summoner@#$';
      const encodedName = encodeURIComponent(summonerName);

      // Act
      service.getByName(summonerName).subscribe();

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/name/${encodedName}`);
      expect(req.request.url).toContain(encodedName);
      req.flush({});
    });

    it('should handle 404 error when summoner not found', () => {
      // Arrange
      const summonerName = 'NonExistentSummoner';

      // Act
      service.getByName(summonerName).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/name/${encodeURIComponent(summonerName)}`);
      req.flush('Summoner not found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('getById()', () => {
    it('should fetch summoner by id', () => {
      // Arrange
      const mockSummoner: Summoner = {
        id: '123',
        name: 'TestSummoner',
        level: 50,
        profileIconId: 456,
        tier: 'SILVER',
        rank: 'III',
        lp: 42
      };
      const summonerId = '123';

      // Act
      service.getById(summonerId).subscribe(summoner => {
        expect(summoner).toEqual(mockSummoner);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/${summonerId}`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockSummoner);
    });

    it('should handle invalid summoner id', () => {
      // Arrange
      const invalidId = 'invalid-id';

      // Act
      service.getById(invalidId).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(400);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/${invalidId}`);
      req.flush('Invalid summoner ID', { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('getChampionStats()', () => {
    it('should fetch champion stats for summoner', () => {
      // Arrange
      const mockChampionStats = [
        { championName: 'Jinx', gamesPlayed: 25, winRate: 0.68 },
        { championName: 'Ashe', gamesPlayed: 15, winRate: 0.73 }
      ];
      const summonerId = '123';

      // Act
      service.getChampionStats(summonerId).subscribe(stats => {
        expect(stats).toEqual(mockChampionStats);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/${summonerId}/champion-stats`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockChampionStats);
    });

    it('should handle empty champion stats', () => {
      // Arrange
      const summonerId = '123';

      // Act
      service.getChampionStats(summonerId).subscribe(stats => {
        expect(stats).toEqual([]);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/${summonerId}/champion-stats`);
      req.flush([]);
    });

    it('should handle summoner with no champion stats', () => {
      // Arrange
      const summonerId = '456';

      // Act
      service.getChampionStats(summonerId).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(404);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/${summonerId}/champion-stats`);
      req.flush('No champion stats found', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('getMatchHistory()', () => {
    it('should fetch match history with default pagination', () => {
      // Arrange
      const mockMatches = {
        content: [
          { id: 1, champion: 'Jinx', result: 'WIN', duration: 1800 },
          { id: 2, champion: 'Ashe', result: 'LOSS', duration: 2100 }
        ],
        totalElements: 50,
        totalPages: 3
      };
      const summonerId = '123';

      // Act
      service.getMatchHistory(summonerId).subscribe(matches => {
        expect(matches).toEqual(mockMatches);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/${summonerId}/matches?page=0&size=20`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockMatches);
    });

    it('should fetch match history with custom pagination', () => {
      // Arrange
      const summonerId = '123';
      const page = 2;
      const size = 10;

      // Act
      service.getMatchHistory(summonerId, page, size).subscribe();

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/${summonerId}/matches?page=${page}&size=${size}`);
      expect(req.request.method).toBe('GET');
      req.flush({ content: [], totalElements: 0 });
    });

    it('should handle empty match history', () => {
      // Arrange
      const mockEmptyResponse = {
        content: [],
        totalElements: 0,
        totalPages: 0
      };
      const summonerId = '123';

      // Act
      service.getMatchHistory(summonerId).subscribe(matches => {
        expect(matches).toEqual(mockEmptyResponse);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/${summonerId}/matches?page=0&size=20`);
      req.flush(mockEmptyResponse);
    });

    it('should handle server error for match history', () => {
      // Arrange
      const summonerId = '123';

      // Act
      service.getMatchHistory(summonerId).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(500);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/${summonerId}/matches?page=0&size=20`);
      req.flush('Internal server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });
});