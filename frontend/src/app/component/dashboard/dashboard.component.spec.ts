import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { DashboardService } from '../../service/dashboard.service';
import { UserService } from '../../service/user.service';
import { User } from '../../dto/user.dto';
import { MatchHistory } from '../../dto/match-history.model';
import { AiAnalysisResponseDto } from '../../dto/dashboard-responses.dto';
import { ElementRef } from '@angular/core';
import { Chart } from 'chart.js';

// Mock Chart.js globally
(window as any).Chart = jasmine.createSpy('Chart').and.callFake(() => ({
  destroy: jasmine.createSpy('destroy'),
  update: jasmine.createSpy('update')
}));

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let mockDashboardService: jasmine.SpyObj<DashboardService>;
  let mockUserService: jasmine.SpyObj<UserService>;

  beforeEach(async () => {
    mockDashboardService = jasmine.createSpyObj('DashboardService', [
      'getPersonalStats', 'getFavoritesOverview', 'getRankedMatches', 'getAiAnalysis'
    ]);
    mockUserService = jasmine.createSpyObj('UserService', [
      'getProfile', 'getLinkedSummoner', 'linkSummoner', 'unlinkSummoner',
      'uploadAvatar', 'addFavoriteSummoner', 'removeFavoriteSummoner', 'updateProfile'
    ]);

    // Default mocks
    mockDashboardService.getPersonalStats.and.returnValue(of({
      username: 'testuser',
      linkedSummoner: 'TestSummoner',
      currentRank: 'Gold I',
      lp7days: 25,
      mainRole: 'ADC',
      favoriteChampion: 'Jinx',
      averageKda: '3/2/5'
    }));
    mockDashboardService.getFavoritesOverview.and.returnValue(of([]));
    mockDashboardService.getRankedMatches.and.returnValue(of([]));
    mockUserService.getProfile.and.returnValue(of({ id: 1, name: 'test', email: 'test@test.com', roles: ['USER'], active: true } as User));
    mockUserService.getLinkedSummoner.and.returnValue(of({ linked: false }));
    mockUserService.linkSummoner.and.returnValue(of({ success: true, message: 'Account linked successfully' }));
    mockUserService.unlinkSummoner.and.returnValue(of({ success: true, message: 'Unlinked' }));
    mockUserService.uploadAvatar.and.returnValue(of({ success: true, message: 'Uploaded', avatarUrl: '/test.png' }));
    mockUserService.addFavoriteSummoner.and.returnValue(of({ success: true, message: 'Added to favorites' }));
    mockUserService.removeFavoriteSummoner.and.returnValue(of({ success: true, message: 'Removed from favorites' }));

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: DashboardService, useValue: mockDashboardService },
        { provide: UserService, useValue: mockUserService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;

    // Mock lpChartCanvas for chart tests
    const mockContext = {
      canvas: document.createElement('canvas'),
      clearRect: jasmine.createSpy('clearRect'),
      fillRect: jasmine.createSpy('fillRect'),
      setTransform: jasmine.createSpy('setTransform'),
      resetTransform: jasmine.createSpy('resetTransform'),
      save: jasmine.createSpy('save'),
      restore: jasmine.createSpy('restore'),
      beginPath: jasmine.createSpy('beginPath'),
      moveTo: jasmine.createSpy('moveTo'),
      lineTo: jasmine.createSpy('lineTo'),
      closePath: jasmine.createSpy('closePath'),
      stroke: jasmine.createSpy('stroke'),
      fill: jasmine.createSpy('fill')
    };
    Object.defineProperty(component, 'lpChartCanvas', {
      get: () => ({ nativeElement: { getContext: () => mockContext } })
    });
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Initialization', () => {
    it('should initialize with default values', () => {
      expect(component.loading).toBeFalse();
      expect(component.stats).toBeNull();
      expect(component.favorites).toEqual([]);
      expect(component.error).toBeNull();
      expect(component.linkedSummoner).toBeNull();
      expect(component.avatarUrl).toBeNull();
      expect(component.selectedQueue).toBe(420);
    });

    it('should call loadLinkedSummoner and loadUserProfile on ngOnInit', () => {
      spyOn(component, 'loadLinkedSummoner');
      spyOn(component, 'loadUserProfile');

      component.ngOnInit();

      expect(component.loadLinkedSummoner).toHaveBeenCalled();
      expect(component.loadUserProfile).toHaveBeenCalled();
    });

    it('should destroy chart on ngOnDestroy if exists', () => {
      const mockChart = { destroy: jasmine.createSpy() };
      (component as any).lpChart = mockChart;

      component.ngOnDestroy();

      expect(mockChart.destroy).toHaveBeenCalled();
    });
  });

  describe('refresh()', () => {
    it('should load stats and favorites successfully', () => {
      const mockStats = {
        username: 'test',
        linkedSummoner: 'TestSummoner',
        currentRank: 'Gold I',
        lp7days: 25,
        mainRole: 'ADC',
        favoriteChampion: 'Jinx',
        averageKda: '3/2/5'
      };
      const mockFavorites = [{ name: 'fav' }];
      mockDashboardService.getPersonalStats.and.returnValue(of(mockStats));
      mockDashboardService.getFavoritesOverview.and.returnValue(of(mockFavorites));

      component.refresh();

      expect(component.stats).toEqual(mockStats);
      expect(component.favorites).toEqual(mockFavorites);
      expect(component.loading).toBeFalse();
      expect(component.error).toBeNull();
    });

    it('should handle stats error', () => {
      mockDashboardService.getPersonalStats.and.returnValue(throwError(() => new Error()));

      component.refresh();

      expect(component.error).toBe('Failed to load dashboard statistics.');
      expect(component.loading).toBeFalse();
    });

    it('should handle favorites error', () => {
      mockDashboardService.getFavoritesOverview.and.returnValue(throwError(() => new Error()));

      component.refresh();

      expect(component.favoritesError).toBe('Failed to load favorites.');
      expect(component.favoritesLoading).toBeFalse();
    });
  });

  describe('Linked Summoner', () => {
    it('should load linked summoner successfully', () => {
      const mockResponse = { linked: true, summonerName: 'test', region: 'NA', puuid: 'puuid' };
      mockUserService.getLinkedSummoner.and.returnValue(of(mockResponse));
      spyOn(component, 'loadRankHistory');

      component.loadLinkedSummoner();

      expect(component.linkedSummoner).toEqual({ name: 'test', region: 'NA', puuid: 'puuid' });
      expect(component.loadRankHistory).toHaveBeenCalled();
    });

    it('should clear data if not linked', () => {
      mockUserService.getLinkedSummoner.and.returnValue(of({ linked: false }));

      component.loadLinkedSummoner();

      expect(component.linkedSummoner).toBeNull();
    });

    it('should handle error', () => {
      mockUserService.getLinkedSummoner.and.returnValue(throwError(() => new Error()));

      component.loadLinkedSummoner();

      expect(component.linkedSummonerLoading).toBeFalse();
    });
  });

  describe('Rank History & Chart', () => {
    beforeEach(() => {
      component.linkedSummoner = { name: 'test', region: 'NA' };
    });

    it('should load rank history successfully', () => {
      const mockMatches: MatchHistory[] = [{ gameTimestamp: 1, win: true, lpAtMatch: 50 }];
      mockDashboardService.getRankedMatches.and.returnValue(of(mockMatches));

      component.loadRankHistory();

      expect(component.rankedMatches).toEqual(mockMatches);
      expect(component.allMatches).toEqual(mockMatches);
      expect(component.chartLoading).toBeFalse();
      expect(component.matchesLoading).toBeFalse();
    });

    it('should handle load error', () => {
      mockDashboardService.getRankedMatches.and.returnValue(throwError(() => new Error()));

      component.loadRankHistory();

      expect(component.chartError).toBe('Failed to load ranked match history');
      expect(component.chartLoading).toBeFalse();
    });

    it('should not load if no linked summoner', () => {
      component.linkedSummoner = null;

      component.loadRankHistory();

      expect(mockDashboardService.getRankedMatches).not.toHaveBeenCalled();
    });

    it('should change queue and reload', () => {
      component.onQueueChange(440);

      expect(component.selectedQueue).toBe(440);
      expect(mockDashboardService.getRankedMatches).toHaveBeenCalledWith(0, 30, 440);
    });

    it('should not change queue if loading', () => {
      component.chartLoading = true;
      component.selectedQueue = 420;

      component.onQueueChange(440);

      expect(component.selectedQueue).toBe(420);
    });

    it('should initialize chart with matches', () => {
      component.rankedMatches = [{ gameTimestamp: 1, win: true, lpAtMatch: 50 }];

      component['initializeLPChart']();

      expect((component as any).lpChart).toBeDefined();
    });

    it('should destroy existing chart before new one', () => {
      const mockChart = { destroy: jasmine.createSpy() };
      (component as any).lpChart = mockChart;
      component.rankedMatches = [{ gameTimestamp: 1, win: true, lpAtMatch: 50 }];

      component['initializeLPChart']();

      expect(mockChart.destroy).toHaveBeenCalled();
      expect((component as any).lpChart).toBeDefined();
    });

    it('should handle empty matches or no canvas', () => {
      component.rankedMatches = [];

      component['initializeLPChart']();

      expect((component as any).lpChart).toBeNull();
    });
  });

  describe('Link Account', () => {
    it('should open and close modal', () => {
      component.openLinkModal();
      expect(component.showLinkModal).toBeTrue();

      component.closeLinkModal();
      expect(component.showLinkModal).toBeFalse();
    });

    it('should submit link successfully', () => {
      component.summonerName = 'test#NA';
      spyOn(component, 'refresh');
      spyOn(component, 'loadLinkedSummoner');

      component.submitLinkAccount();

      expect(component.linkSuccess).toBe('Account linked successfully');
      expect(component.refresh).toHaveBeenCalled();
      expect(component.loadLinkedSummoner).toHaveBeenCalled();
    });

    it('should handle link error', () => {
      mockUserService.linkSummoner.and.returnValue(throwError(() => ({ error: { message: 'error' } })));
      component.summonerName = 'test#NA';

      component.submitLinkAccount();

      expect(component.linkError).toBe('error');
    });

    it('should validate input', () => {
      component.summonerName = '';
      component.submitLinkAccount();
      expect(component.linkError).toBe('Please enter a summoner name');

      component.summonerName = 'test';
      component.submitLinkAccount();
      expect(component.linkError).toBe('Please use format: name#region (e.g., jae9104#NA)');
    });

    it('should unlink account', () => {
      component.linkedSummoner = { name: 'test' };
      spyOn(window, 'confirm').and.returnValue(true);
      spyOn(component, 'refresh');

      component.unlinkAccount();

      expect(component.linkedSummoner).toBeNull();
      expect(component.refresh).toHaveBeenCalled();
    });
  });

  describe('Avatar', () => {
    it('should upload avatar successfully', () => {
      const file = new File([''], 'test.png', { type: 'image/png' });

      component.uploadAvatar(file);

      expect(component.avatarUrl).toContain('/test.png');
      expect(component.avatarUploading).toBeFalse();
    });

    it('should reject large file', () => {
      const largeFile = new File(['x'.repeat(6e6)], 'large.png', { type: 'image/png' });

      component.uploadAvatar(largeFile);

      expect(component.avatarError).toBe('The file is too large. Maximum 5MB.');
    });

    it('should reject non-PNG', () => {
      const file = new File([''], 'test.jpg', { type: 'image/jpeg' });

      component.uploadAvatar(file);

      expect(component.avatarError).toBe('Please select a PNG file.');
    });

    it('should handle upload error', () => {
      mockUserService.uploadAvatar.and.returnValue(throwError(() => ({ error: { message: 'error' } })));
      const file = new File([''], 'test.png', { type: 'image/png' });

      component.uploadAvatar(file);

      expect(component.avatarError).toBe('error');
    });

    it('should load user profile', () => {
      const user: User = { id: 1, name: 'test', email: 'test@test.com', roles: ['USER'], active: true, avatarUrl: '/avatar.png' };
      mockUserService.getProfile.and.returnValue(of(user));

      component.loadUserProfile();

      expect(component.avatarUrl).toContain('/avatar.png');
    });

    it('should handle avatar load and error', () => {
      component.onAvatarLoad();
      expect(component).toBeTruthy(); // No specific behavior

      component.onAvatarError(new Event('error'));
      expect(component.avatarError).toBe('Failed to load avatar image');
    });
  });

  describe('Favorites', () => {
    it('should open and close add favorite modal', () => {
      component.openAddFavoriteModal();
      expect(component.showAddFavoriteModal).toBeTrue();

      component.closeAddFavoriteModal();
      expect(component.showAddFavoriteModal).toBeFalse();
    });

    it('should add favorite successfully', () => {
      component.addFavoriteName = 'test#NA';
      spyOn(component, 'loadFavorites');

      component.addFavorite();

      expect(mockUserService.addFavoriteSummoner).toHaveBeenCalledWith('test#NA');
      expect(component.loadFavorites).toHaveBeenCalled();
    });

    it('should handle add favorite error', () => {
      mockUserService.addFavoriteSummoner.and.returnValue(throwError(() => ({ error: { message: 'error' } })));
      component.addFavoriteName = 'test#NA';

      component.addFavorite();

      expect(component.addFavoriteError).toBe('error');
    });

    it('should validate favorite input', () => {
      component.addFavoriteName = '';
      component.addFavorite();
      expect(component.addFavoriteError).toBe('Please enter a summoner name');

      component.addFavoriteName = 'test';
      component.addFavorite();
      expect(component.addFavoriteError).toBe('Please use format: name#region (e.g., jae9104#NA)');
    });

    it('should remove favorite', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      spyOn(component, 'loadFavorites');

      component.removeFavorite('test');

      expect(mockUserService.removeFavoriteSummoner).toHaveBeenCalledWith('test');
      expect(component.loadFavorites).toHaveBeenCalled();
    });

    it('should load favorites', () => {
      const mockFavorites = [{ name: 'fav' }];
      mockDashboardService.getFavoritesOverview.and.returnValue(of(mockFavorites));

      component.loadFavorites();

      expect(component.favorites).toEqual(mockFavorites);
      expect(component.favoritesLoading).toBeFalse();
    });
  });

  describe('AI Analysis', () => {
    it('should open and close AI modal', () => {
      component.openAiAnalysisModal();
      expect(component.showAiModal).toBeTrue();
      expect(component.aiAnalysis).toBeNull();

      component.closeAiAnalysisModal();
      expect(component.showAiModal).toBeFalse();
    });

    it('should generate AI analysis successfully', () => {
      component.linkedSummoner = { name: 'test' };
      component.aiMatchCount = 10;
      const response: AiAnalysisResponseDto = { analysis: 'analysis', generatedAt: 'now', matchesAnalyzed: 10, summonerName: 'test' };
      mockDashboardService.getAiAnalysis.and.returnValue(of(response));

      component.generateAiAnalysis();

      expect(component.aiAnalysis).toBe('analysis');
      expect(component.aiMatchesAnalyzed).toBe(10);
      expect(component.aiAnalysisLoading).toBeFalse();
    });

    it('should handle AI analysis error', () => {
      component.linkedSummoner = { name: 'test' };
      component.aiMatchCount = 10;
      mockDashboardService.getAiAnalysis.and.returnValue(throwError(() => ({ error: { message: 'error' } })));

      component.generateAiAnalysis();

      expect(component.aiAnalysisError).toBe('error');
    });

    it('should validate AI analysis input', () => {
      component.linkedSummoner = null;
      component.aiMatchCount = 10;
      component.generateAiAnalysis();
      expect(component.aiAnalysisError).toBe('You must link your League of Legends account first');

      component.linkedSummoner = { name: 'test' };
      component.aiMatchCount = 5;
      component.generateAiAnalysis();
      expect(component.aiAnalysisError).toBe('The number of matches must be 10');
    });
  });

  describe('Profile Management', () => {
    it('should open profile modal with current email', () => {
      component.stats = { email: 'current@test.com' };
      component.openProfileModal();
      expect(component.showProfileModal).toBeTrue();
      expect(component.profileForm.email).toBe('current@test.com');
      expect(component.profileForm.password).toBe('');
    });

    it('should close profile modal', () => {
      component.showProfileModal = true;
      component.closeProfileModal();
      expect(component.showProfileModal).toBeFalse();
    });

    it('should validate email format', () => {
      component.profileForm.email = 'invalid-email';
      component.updateProfile();
      expect(component.profileError).toBe('Please enter a valid email address');
      expect(mockUserService.updateProfile).not.toHaveBeenCalled();
    });

    it('should validate password length', () => {
      component.profileForm.email = 'valid@test.com';
      component.profileForm.password = '12345';
      component.updateProfile();
      expect(component.profileError).toBe('Password must be at least 6 characters long');
      expect(mockUserService.updateProfile).not.toHaveBeenCalled();
    });

    it('should validate password mismatch', () => {
      component.profileForm.email = 'valid@test.com';
      component.profileForm.password = 'password123';
      component.profileForm.confirmPassword = 'password456';
      component.updateProfile();
      expect(component.profileError).toBe('Passwords do not match');
      expect(mockUserService.updateProfile).not.toHaveBeenCalled();
    });

    it('should update profile successfully', fakeAsync(() => {
      component.profileForm.email = 'new@test.com';
      component.profileForm.password = 'newpassword';
      component.profileForm.confirmPassword = 'newpassword';
      
      mockUserService.updateProfile.and.returnValue(of({ id: 1, name: 'test', email: 'new@test.com' } as User));
      spyOn(component, 'refresh');

      component.updateProfile();

      expect(mockUserService.updateProfile).toHaveBeenCalledWith({
        email: 'new@test.com',
        password: 'newpassword'
      });
      expect(component.profileSuccess).toBe('Profile updated successfully');
      expect(component.refresh).toHaveBeenCalled();

      tick(1500);
      expect(component.showProfileModal).toBeFalse();
    }));

    it('should handle profile update error', () => {
      component.profileForm.email = 'new@test.com';
      mockUserService.updateProfile.and.returnValue(throwError(() => ({ error: { message: 'Update failed' } })));

      component.updateProfile();

      expect(component.profileError).toBe('Update failed');
      expect(component.profileLoading).toBeFalse();
    });
  });
});
