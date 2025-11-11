import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { DashboardService, RankHistoryEntry } from '../../service/dashboard.service';
import { UserService } from '../../service/user.service';

describe('DashboardComponent - Unit Tests', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let mockDashboardService: jasmine.SpyObj<DashboardService>;
  let mockUserService: jasmine.SpyObj<UserService>;

  beforeEach(async () => {
    // Create spy objects for services
    mockDashboardService = jasmine.createSpyObj('DashboardService', [
      'getPersonalStats',
      'getFavoritesOverview',
      'getRankHistory',
      'getRankedMatches'
    ]);

    mockUserService = jasmine.createSpyObj('UserService', [
      'getProfile',
      'getLinkedSummoner',
      'linkSummoner',
      'unlinkSummoner',
      'uploadAvatar'
    ]);

    // Set default return values for all mocks to prevent errors during ngOnInit
    mockDashboardService.getPersonalStats.and.returnValue(of({} as any));
    mockDashboardService.getFavoritesOverview.and.returnValue(of([]));
    mockDashboardService.getRankedMatches.and.returnValue(of([]));
    mockUserService.getProfile.and.returnValue(of({} as any));
    mockUserService.getLinkedSummoner.and.returnValue(of({} as any));
    mockUserService.linkSummoner.and.returnValue(of({} as any));
    mockUserService.unlinkSummoner.and.returnValue(of({} as any));
    mockUserService.uploadAvatar.and.returnValue(of({ success: true, message: '' }));

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: DashboardService, useValue: mockDashboardService },
        { provide: UserService, useValue: mockUserService }
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
    expect(component.linkedSummoner).toBeNull();
    expect(component.avatarUrl).toBeNull();
    expect(component.rankHistory).toEqual([]);
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
  expect(component.error).toBe('Failed to load dashboard statistics.');
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
  expect(component.favoritesError).toBe('Failed to load favorites.');
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
    it('should call refresh and load linked summoner on initialization', () => {
      // Arrange
      spyOn(component, 'refresh');
      spyOn(component, 'loadLinkedSummoner');
      spyOn(component, 'loadUserProfile');
      mockDashboardService.getPersonalStats.and.returnValue(of({}));
      mockDashboardService.getFavoritesOverview.and.returnValue(of([]));
      mockUserService.getLinkedSummoner.and.returnValue(of({ linked: false }));
      mockUserService.getProfile.and.returnValue(of({} as any));

      // Act
      component.ngOnInit();

      // Assert
      expect(component.refresh).toHaveBeenCalled();
      expect(component.loadLinkedSummoner).toHaveBeenCalled();
      expect(component.loadUserProfile).toHaveBeenCalled();
    });
  });

  describe('Avatar Upload', () => {
    it('should upload avatar successfully', () => {
      // Arrange
      const mockFile = new File(['test'], 'test.png', { type: 'image/png' });
      const mockResponse = { 
        success: true, 
        message: 'Avatar uploaded', 
        avatarUrl: '/api/v1/files/avatars/test.png' 
      };
      mockUserService.uploadAvatar.and.returnValue(of(mockResponse));

      // Act
      component.uploadAvatar(mockFile);

      // Assert
      expect(component.avatarUploading).toBeFalse();
      expect(component.avatarUrl).toContain('/api/v1/files/avatars/test.png');
      expect(mockUserService.uploadAvatar).toHaveBeenCalledWith(mockFile);
    });

    it('should reject files larger than 5MB', () => {
      // Arrange
      const largeFile = new File(['x'.repeat(6 * 1024 * 1024)], 'large.png', { type: 'image/png' });

      // Act
      component.uploadAvatar(largeFile);

      // Assert
      expect(component.avatarError).toBe('The file is too large. Maximum 5MB.');
      expect(mockUserService.uploadAvatar).not.toHaveBeenCalled();
    });

    it('should reject non-PNG files', () => {
      // Arrange
      const invalidFile = new File(['test'], 'test.gif', { type: 'image/gif' });

      // Act
      component.uploadAvatar(invalidFile);

      // Assert
      expect(component.avatarError).toBe('Please select a PNG file.');
      expect(mockUserService.uploadAvatar).not.toHaveBeenCalled();
    });

    it('should handle upload error', () => {
      // Arrange
      const mockFile = new File(['test'], 'test.png', { type: 'image/png' });
      mockUserService.uploadAvatar.and.returnValue(
        throwError(() => ({ error: { message: 'Upload failed' } }))
      );

      // Act
      component.uploadAvatar(mockFile);

      // Assert
      expect(component.avatarUploading).toBeFalse();
      expect(component.avatarError).toBe('Upload failed');
    });
  });

  describe('Linked Account Management', () => {
    it('should load linked summoner successfully', () => {
      // Arrange
      const mockLinkedResponse = {
        linked: true,
        summonerName: 'TestPlayer',
        region: 'EUW',
        puuid: 'test-puuid'
      };
      const mockMatches = [
        { matchId: 'match1', queueId: 420, participantPuuid: 'test-puuid' }
      ];
      mockUserService.getLinkedSummoner.and.returnValue(of(mockLinkedResponse));
      mockDashboardService.getRankedMatches.and.returnValue(of(mockMatches));

      // Act
      component.loadLinkedSummoner();

      // Assert
      expect(component.linkedSummoner).toEqual({
        name: 'TestPlayer',
        region: 'EUW',
        puuid: 'test-puuid'
      });
      expect(mockDashboardService.getRankedMatches).toHaveBeenCalled();
    });

    it('should not load ranked matches if no linked account', () => {
      // Arrange
      mockUserService.getLinkedSummoner.and.returnValue(of({ linked: false }));

      // Act
      component.loadLinkedSummoner();

      // Assert
      expect(component.linkedSummoner).toBeNull();
      expect(mockDashboardService.getRankedMatches).not.toHaveBeenCalled();
    });

    it('should link summoner account successfully', () => {
      // Arrange
      const mockLinkResponse = { success: true, message: 'Account linked' };
      mockUserService.linkSummoner.and.returnValue(of(mockLinkResponse));
      mockUserService.getLinkedSummoner.and.returnValue(of({ 
        linked: true, 
        summonerName: 'NewPlayer#EUW', 
        region: 'EUW' 
      }));
      mockDashboardService.getRankedMatches.and.returnValue(of([]));
      
      component.summonerName = 'NewPlayer#EUW';
      component.selectedRegion = 'EUW';

      // Act
      component.submitLinkAccount();

      // Assert
      expect(component.linkSuccess).toBe('Account linked');
      expect(mockUserService.linkSummoner).toHaveBeenCalledWith('NewPlayer#EUW', 'EUW');
    });

    it('should handle link summoner error', () => {
      // Arrange
      mockUserService.linkSummoner.and.returnValue(
        throwError(() => ({ error: { message: 'Summoner not found' } }))
      );
      component.summonerName = 'InvalidPlayer#EUW';
      component.selectedRegion = 'EUW';

      // Act
      component.submitLinkAccount();

      // Assert
      expect(component.linkError).toBe('Summoner not found');
      expect(component.linkLoading).toBeFalse();
    });

    it('should unlink account and clear rank history', () => {
      // Arrange
      const mockUnlinkResponse = { success: true, message: 'Account unlinked' };
      mockUserService.unlinkSummoner.and.returnValue(of(mockUnlinkResponse));
      component.linkedSummoner = { name: 'TestPlayer', region: 'EUW', puuid: 'test-puuid' };
      component.rankHistory = [
        { date: '2025-01-01', tier: 'GOLD', rank: 'II', leaguePoints: 50, wins: 10, losses: 5 }
      ];
      spyOn(window, 'confirm').and.returnValue(true);

      // Act
      component.unlinkAccount();

      // Assert
      expect(component.linkedSummoner).toBeNull();
      expect(component.rankHistory).toEqual([]);
      expect(mockUserService.unlinkSummoner).toHaveBeenCalled();
    });
  });

  describe('Rank History Chart', () => {
    it('should load ranked matches when linked account exists', () => {
      // Arrange
      component.linkedSummoner = { 
        name: 'TestPlayer', 
        region: 'EUW', 
        puuid: 'test-puuid' 
      };
      const mockMatches = [
        { matchId: 'match1', queueId: 420, participantPuuid: 'test-puuid' },
        { matchId: 'match2', queueId: 420, participantPuuid: 'test-puuid' }
      ];
      mockDashboardService.getRankedMatches.and.returnValue(of(mockMatches));

      // Act
      component.loadRankHistory();

      // Assert
      expect(component.chartLoading).toBeFalse();
      expect(component.rankedMatches).toEqual(mockMatches);
    });

    it('should handle rank history error', () => {
      // Arrange
      component.linkedSummoner = { 
        name: 'TestPlayer', 
        region: 'EUW', 
        puuid: 'test-puuid' 
      };
      mockDashboardService.getRankedMatches.and.returnValue(
        throwError(() => new Error('Failed to load ranked matches'))
      );

      // Act
      component.loadRankHistory();

      // Assert
      expect(component.chartLoading).toBeFalse();
    expect(component.chartError).toBe('Failed to load ranked match history');
    });
  });
});