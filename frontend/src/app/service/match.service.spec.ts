import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MatchService } from './match.service';
import { API_URL } from './api.config';

describe('MatchService - Unit Tests', () => {
  let service: MatchService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [MatchService]
    });
    service = TestBed.inject(MatchService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getMatch()', () => {
    it('should fetch match details', () => {
      // Arrange
      const matchId = 'EUW1_1234567890';
      const mockMatch = {
        id: matchId,
        gameMode: 'CLASSIC',
        gameType: 'MATCHED_GAME',
        gameDuration: 1876,
        participants: [
          {
            summonerId: 'summoner1',
            championName: 'Jinx',
            kills: 12,
            deaths: 3,
            assists: 8,
            win: true
          },
          {
            summonerId: 'summoner2',
            championName: 'Ashe',
            kills: 8,
            deaths: 5,
            assists: 12,
            win: false
          }
        ],
        teams: [
          { teamId: 100, win: true, kills: 45, objectives: { baron: 1, dragon: 3 } },
          { teamId: 200, win: false, kills: 32, objectives: { baron: 0, dragon: 1 } }
        ]
      };

      // Act
      service.getMatch(matchId).subscribe((match: any) => {
        expect(match).toEqual(mockMatch);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${matchId}`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeFalsy(); // This endpoint doesn't use credentials
      req.flush(mockMatch);
    });

    it('should handle invalid match ID', () => {
      // Arrange
      const invalidMatchId = 'invalid-match-id';

      // Act
      service.getMatch(invalidMatchId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(400);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${invalidMatchId}`);
      req.flush('Invalid match ID format', { status: 400, statusText: 'Bad Request' });
    });

    it('should handle match not found', () => {
      // Arrange
      const nonExistentMatchId = 'EUW1_9999999999';

      // Act
      service.getMatch(nonExistentMatchId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(404);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${nonExistentMatchId}`);
      req.flush('Match not found', { status: 404, statusText: 'Not Found' });
    });

    it('should handle server error', () => {
      // Arrange
      const matchId = 'EUW1_1234567890';

      // Act
      service.getMatch(matchId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(500);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${matchId}`);
      req.flush('Internal server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('addNote()', () => {
    it('should add note to match', () => {
      // Arrange
      const matchId = 'EUW1_1234567890';
      const noteData = { text: 'Great comeback game! Amazing teamfight at Baron.' };
      const mockResponse = {
        id: 1,
        matchId: matchId,
        text: noteData.text,
        userId: 123,
        createdAt: new Date(),
        updatedAt: new Date()
      };

      // Act
      service.addNote(matchId, noteData).subscribe((response: any) => {
        expect(response.text).toBe(noteData.text);
        expect(response.matchId).toBe(matchId);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${matchId}/notes`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(noteData);
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockResponse);
    });

    it('should handle empty note text', () => {
      // Arrange
      const matchId = 'EUW1_1234567890';
      const emptyNote = { text: '' };

      // Act
      service.addNote(matchId, emptyNote).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(400);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${matchId}/notes`);
      req.flush('Note text cannot be empty', { status: 400, statusText: 'Bad Request' });
    });

    it('should handle unauthorized access', () => {
      // Arrange
      const matchId = 'EUW1_1234567890';
      const noteData = { text: 'Test note' };

      // Act
      service.addNote(matchId, noteData).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(401);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${matchId}/notes`);
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle note text too long', () => {
      // Arrange
      const matchId = 'EUW1_1234567890';
      const longNote = { text: 'A'.repeat(1001) }; // Assuming 1000 character limit

      // Act
      service.addNote(matchId, longNote).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(400);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${matchId}/notes`);
      req.flush('Note text too long', { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('getNotes()', () => {
    it('should fetch notes for match', () => {
      // Arrange
      const matchId = 'EUW1_1234567890';
      const mockNotes = [
        {
          id: 1,
          matchId: matchId,
          text: 'First note about this match',
          userId: 123,
          username: 'TestUser1',
          createdAt: new Date('2025-01-01T10:00:00Z'),
          updatedAt: new Date('2025-01-01T10:00:00Z')
        },
        {
          id: 2,
          matchId: matchId,
          text: 'Second note with different insights',
          userId: 456,
          username: 'TestUser2',
          createdAt: new Date('2025-01-01T11:00:00Z'),
          updatedAt: new Date('2025-01-01T11:00:00Z')
        }
      ];

      // Act
      service.getNotes(matchId).subscribe((notes: any) => {
        expect(notes).toEqual(mockNotes);
        expect(notes.length).toBe(2);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${matchId}/notes`);
      expect(req.request.method).toBe('GET');
      expect(req.request.withCredentials).toBeTrue();
      req.flush(mockNotes);
    });

    it('should handle empty notes list', () => {
      // Arrange
      const matchId = 'EUW1_1234567890';

      // Act
      service.getNotes(matchId).subscribe((notes: any) => {
        expect(notes).toEqual([]);
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${matchId}/notes`);
      req.flush([]);
    });

    it('should handle unauthorized access', () => {
      // Arrange
      const matchId = 'EUW1_1234567890';

      // Act
      service.getNotes(matchId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(401);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${matchId}/notes`);
      req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle match not found', () => {
      // Arrange
      const invalidMatchId = 'INVALID_MATCH_ID';

      // Act
      service.getNotes(invalidMatchId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(404);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${invalidMatchId}/notes`);
      req.flush('Match not found', { status: 404, statusText: 'Not Found' });
    });

    it('should handle server error', () => {
      // Arrange
      const matchId = 'EUW1_1234567890';

      // Act
      service.getNotes(matchId).subscribe({
        next: () => fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(500);
        }
      });

      // Assert
      const req = httpMock.expectOne(`${API_URL}/matches/${matchId}/notes`);
      req.flush('Internal server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });
});