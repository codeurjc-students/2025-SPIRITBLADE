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
        name: 'TestSummoner#EUW',
        riotId: 'TestSummoner#EUW',
        level: 100,
        profileIconId: 123,
        profileIconUrl: 'http://example.com/icon.png',
        tier: 'GOLD',
        rank: 'II',
        lp: 85
      };
      const summonerName = 'TestSummoner#EUW';

      // Act
      service.getByName(summonerName).subscribe(summoner => {
        expect(summoner).toEqual(mockSummoner);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/summoners/name/${encodeURIComponent(summonerName)}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockSummoner);
    });

    it('should handle summoner name with special characters', () => {
      // Arrange
      const summonerName = 'Test Summoner#TAG';
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
});
