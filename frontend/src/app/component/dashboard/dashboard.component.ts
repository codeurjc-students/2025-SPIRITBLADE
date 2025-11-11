import { Component, inject, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DashboardService, RankHistoryEntry } from '../../service/dashboard.service';
import { UserService } from '../../service/user.service';
import { MatchHistory } from '../../dto/match-history.model';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import { API_URL } from '../../service/api.config';
import { MarkdownToHtmlPipe } from '../../pipes/markdown-to-html.pipe';

// Register Chart.js components
Chart.register(...registerables);

@Component({
	selector: 'app-dashboard',
	standalone: true,
	imports: [CommonModule, FormsModule, RouterLink, MarkdownToHtmlPipe],
	templateUrl: './dashboard.component.html',
	styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy, AfterViewInit {
	private dashboardService = inject(DashboardService);
	private userService = inject(UserService);

	@ViewChild('lpChartCanvas', { static: false }) lpChartCanvas!: ElementRef<HTMLCanvasElement>;
	
	private lpChart: Chart | null = null;

	loading = false;
	stats: any = null;
	favorites: any[] = [];
	favoritesLoading = false;
	favoritesError: string | null = null;
	error: string | null = null;
	
	// Match history data
	allMatches: MatchHistory[] = [];
	rankedMatches: MatchHistory[] = [];
	matchesLoading = false;
	matchesError: string | null = null;
	
	// Queue filter (420 = Solo/Duo, 440 = Flex, null = All)
	selectedQueue: number | null = 420; // Default to Solo/Duo
	
	// Chart data
	rankHistory: RankHistoryEntry[] = [];
	chartLoading = false;
	chartError: string | null = null;

	// Link account modal state
	showLinkModal = false;
	linkLoading = false;
	linkError: string | null = null;
	linkSuccess: string | null = null;
	summonerName = '';
	selectedRegion = 'EUW';
	
	// Linked summoner info
	linkedSummoner: any = null;
	linkedSummonerLoading = false;

	// Avatar upload
	avatarUrl: string | null = null;
	avatarUploading = false;
	avatarError: string | null = null;

	// AI Analysis
	showAiModal = false;
	aiAnalysis: string | null = null;
	aiAnalysisLoading = false;
	aiAnalysisError: string | null = null;
	aiMatchCount = 10;
	aiGeneratedAt: string | null = null;
	aiMatchesAnalyzed = 0;

	refresh() {
		this.loading = true;
		this.error = null;
		this.dashboardService.getPersonalStats().subscribe({
			next: (res) => {
				this.stats = res;
				this.loading = false;
			},
			error: (err: any) => {
				console.debug('Failed to refresh dashboard', err);
				this.error = 'Failed to load dashboard statistics.';
				this.loading = false;
			}
		});

		// also fetch favorites (static for now from backend)
		this.favoritesLoading = true;
		this.favoritesError = null;
		this.dashboardService.getFavoritesOverview().subscribe({
			next: (res) => {
				this.favorites = res || [];
				this.favoritesLoading = false;
			},
			error: (err: any) => {
				console.debug('Failed to load favorites', err);
				this.favoritesError = 'Failed to load favorites.';
				this.favoritesLoading = false;
			}
		});
	}

	ngOnInit() {
		this.refresh();
		this.loadLinkedSummoner();
		this.loadUserProfile();
		// Don't load rank history on init - it will be loaded after checking linked summoner
	}

	ngAfterViewInit() {
		// Chart will be initialized after data is loaded
	}

	ngOnDestroy() {
		// Destroy chart instance to prevent memory leaks
		if (this.lpChart) {
			this.lpChart.destroy();
		}
	}

	/**
	 * Load match history for the linked summoner and filter ranked matches
	 */
	loadRankHistory() {
		if (!this.linkedSummoner || !this.linkedSummoner.name) {
			return;
		}

		this.chartLoading = true;
		this.chartError = null;
		this.matchesLoading = true;
		
		this.dashboardService.getRankedMatches(0, 30, this.selectedQueue).subscribe({
			next: (matches) => {
				this.rankedMatches = matches;
				this.allMatches = matches;
				this.matchesLoading = false;
				this.chartLoading = false;
				this.initializeLPChart();
				console.log('✅ Loaded ranked match history:', matches);
			},
			error: (err) => {
				console.error('Failed to load ranked match history', err);
				this.chartError = 'Failed to load ranked match history';
				this.matchesLoading = false;
				this.chartLoading = false;
			}
		});
	}

	/**
	 * Change queue filter and reload matches
	 */
	onQueueChange(queueId: number | null) {
		this.selectedQueue = queueId;
		this.loadRankHistory();
	}

	/**
	 * Initialize the LP progression chart using match history
	 */
	private initializeLPChart() {
		if (!this.lpChartCanvas || this.rankedMatches.length === 0) {
			return;
		}

		// Destroy existing chart if any
		if (this.lpChart) {
			this.lpChart.destroy();
		}

		const ctx = this.lpChartCanvas.nativeElement.getContext('2d');
		if (!ctx) {
			return;
		}

		// Sort matches by timestamp (oldest first for progression)
		const sortedMatches = [...this.rankedMatches].sort((a, b) => {
			return (a.gameTimestamp || 0) - (b.gameTimestamp || 0);
		});

		// Prepare data from matches
		const labels = sortedMatches.map(match => {
			const date = new Date((match.gameTimestamp || 0) * 1000); // Convert from seconds to milliseconds
			if (isNaN(date.getTime())) {
				return 'Unknown';
			}
			return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
		});

		// Calculate win/loss streak for visual context
		let wins = 0;
		let losses = 0;
		const winrateData = sortedMatches.map(match => {
			if (match.win) {
				wins++;
			} else {
				losses++;
			}
			const total = wins + losses;
			return total > 0 ? (wins / total) * 100 : 0;
		});

		// Create LP data from match history
		// Convert relative LP (0-100 per division) to cumulative LP for smooth visualization
		let cumulativeLP = 0;
		const lpData = sortedMatches.map((match, index) => {
			if (index === 0) {
				// First match: use its LP as baseline
				cumulativeLP = match.lpAtMatch || 0;
				return cumulativeLP;
			}
			
			const prevMatch = sortedMatches[index - 1];
			const prevLP = prevMatch.lpAtMatch || 0;
			const currentLP = match.lpAtMatch || 0;
			
			// Calculate the raw LP change
			let lpChange = currentLP - prevLP;
			
			// Detect division changes by checking for large jumps (>50 LP difference)
			// This happens when someone gets promoted or demoted between divisions
			if (Math.abs(lpChange) > 50) {
				// Division change detected
				if (lpChange < 0) {
					// Negative large jump: Demotion (e.g., 5 LP -> 95 LP going backwards in time)
					// or Promotion being undone when going forward
					// Adjust by adding 100 to make it continuous
					lpChange += 100;
				} else {
					// Positive large jump: Promotion (e.g., 95 LP -> 5 LP going forward)
					// or Demotion being undone when going backwards
					// Adjust by subtracting 100 to make it continuous
					lpChange -= 100;
				}
			}
			
			cumulativeLP += lpChange;
			return cumulativeLP;
		});

		const config: ChartConfiguration = {
			type: 'line',
			data: {
				labels: labels,
				datasets: [
					{
						label: 'Win Rate (%)',
						data: winrateData,
						borderColor: 'rgb(75, 192, 192)',
						backgroundColor: 'rgba(75, 192, 192, 0.2)',
						tension: 0.3,
						fill: true,
						yAxisID: 'y'
					},
					{
						label: 'LP',
						data: lpData,
						borderColor: 'rgb(255, 205, 86)',
						backgroundColor: 'rgba(255, 205, 86, 0.1)',
						tension: 0.3,
						fill: false,
						yAxisID: 'y1',
						pointRadius: 4,
						pointHoverRadius: 6
					}
				]
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				interaction: {
					mode: 'index',
					intersect: false,
				},
				plugins: {
					title: {
						display: true,
						text: 'Match History & Win Rate Progression',
						color: '#e0e0e0',
						font: {
							size: 16,
							weight: 'bold'
						}
					},
					legend: {
						display: true,
						labels: {
							color: '#e0e0e0',
							font: {
								size: 12
							}
						}
					},
					tooltip: {
						callbacks: {
							title: (context) => {
								const index = context[0].dataIndex;
								const match = sortedMatches[index];
								const date = new Date((match.gameTimestamp || 0) * 1000);
								return date.toLocaleString('en-US');
							},
							label: (context) => {
								const index = context.dataIndex;
								const match = sortedMatches[index];
								
								if (context.datasetIndex === 0) {
									// Win rate dataset
									return `Win Rate: ${(context.parsed.y || 0).toFixed(1)}%`;
								} else {
									// LP dataset - show ORIGINAL LP (0-100), not cumulative
									const result = match.win ? 'Victory' : 'Defeat';
									const kda = `${match.kills}/${match.deaths}/${match.assists}`;
									const originalLP = match.lpAtMatch || 0; // Original LP in current division
									return [
										`${result} - ${match.championName}`,
										`KDA: ${kda}`,
										`LP: ${originalLP}`, // Show original LP, not cumulative
										`Duration: ${Math.floor((match.gameDuration || 0) / 60)}m`
									];
								}
							}
						}
					}
				},
				scales: {
					y: {
						type: 'linear',
						display: true,
						position: 'left',
						title: {
							display: true,
							text: 'Win Rate (%)',
							color: '#e0e0e0'
						},
						ticks: {
							color: '#b0b0b0'
						},
						grid: {
							color: 'rgba(255, 255, 255, 0.1)'
						},
						min: 0,
						max: 100
					},
					y1: {
						type: 'linear',
						display: true,
						position: 'right',
						title: {
							display: true,
							text: 'LP Progression',
							color: '#e0e0e0'
						},
						ticks: {
							color: '#b0b0b0'
						},
						grid: {
							drawOnChartArea: false
						}
						// Let the scale be automatic based on data (cumulative LP)
					},
					x: {
						ticks: {
							color: '#b0b0b0',
							maxRotation: 45,
							minRotation: 45
						},
						grid: {
							color: 'rgba(255, 255, 255, 0.05)'
						}
					}
				}
			}
		};

		this.lpChart = new Chart(ctx, config);
		console.log('✅ Chart initialized with', sortedMatches.length, 'matches');
	}

	/**
	 * Calculate total LP for a given tier, rank, and LP
	 * This helps visualize progression across divisions
	 * Returns LP in range 0-100 (actual League Points per division)
	 */
	private calculateTotalLP(tier: string, rank: string, lp: number): number {
		// Simply return the actual LP value (0-100)
		// The tier/rank info is shown in the tooltip
		return Math.min(Math.max(lp, 0), 100);
	}

	/**
	 * Load the linked summoner information
	 */
	loadLinkedSummoner() {
		this.linkedSummonerLoading = true;
		this.userService.getLinkedSummoner().subscribe({
			next: (res) => {
				if (res.linked) {
					this.linkedSummoner = {
						name: res.summonerName,
						region: res.region,
						puuid: res.puuid
					};
					// Load rank history only if there's a linked account
					this.loadRankHistory();
				} else {
					this.linkedSummoner = null;
					// Clear any existing chart data
					this.rankHistory = [];
					if (this.lpChart) {
						this.lpChart.destroy();
						this.lpChart = null;
					}
				}
				this.linkedSummonerLoading = false;
			},
			error: (err) => {
				console.error('Failed to load linked summoner', err);
				this.linkedSummonerLoading = false;
			}
		});
	}

	/**
	 * Open the link account modal
	 */
	openLinkModal() {
		this.showLinkModal = true;
		this.linkError = null;
		this.linkSuccess = null;
		this.summonerName = '';
		this.selectedRegion = 'EUW';
	}

	/**
	 * Close the link account modal
	 */
	closeLinkModal() {
		this.showLinkModal = false;
		this.linkError = null;
		this.linkSuccess = null;
	}

	/**
	 * Submit the link account form
	 */
	submitLinkAccount() {
		if (!this.summonerName.trim()) {
			this.linkError = 'Please enter a summoner name';
			return;
		}

		const input = this.summonerName.trim();
		
		// Validate format: nombre#región
		if (!input.includes('#')) {
			this.linkError = 'Please use format: name#region (e.g., jae9104#EUW)';
			return;
		}
		
		const parts = input.split('#');
		const name = parts[0].trim();
		const region = parts[1].trim().toUpperCase();
		
		// Validate region (only EUW supported for now)
		if (region !== 'EUW') {
			this.linkError = 'Currently, only EUW region is supported';
			return;
		}

		if (!name) {
			this.linkError = 'Please enter a valid summoner name';
			return;
		}

		this.linkLoading = true;
		this.linkError = null;
		this.linkSuccess = null;

		// Send the full Riot ID to the backend
		this.userService.linkSummoner(input, region).subscribe({
			next: (res) => {
				this.linkLoading = false;
				if (res.success) {
					this.linkSuccess = res.message || 'Account linked successfully';
					// Reload personal stats (current rank, LP, main role, fav champion)
					this.refresh();
					// Reload linked summoner (which will also load rank history)
					this.loadLinkedSummoner();
					// Close modal after 2 seconds
					setTimeout(() => {
						this.closeLinkModal();
					}, 1000);
				} else {
					this.linkError = res.message || 'Error linking account. Please check the summoner name.';
				}
			},
			error: (err) => {
				this.linkLoading = false;
				this.linkError = err.error?.message || 'Error linking account. Please check the summoner name.';
			}
		});
	}

	/**
	 * Unlink the current League of Legends account
	 */
	unlinkAccount() {
		if (!confirm('Are you sure you want to unlink your League of Legends account?')) {
			return;
		}

		this.linkedSummonerLoading = true;
		this.userService.unlinkSummoner().subscribe({
			next: (res) => {
				this.linkedSummonerLoading = false;
				if (res.success) {
					this.linkedSummoner = null;
					// Clear rank history and chart
					this.rankHistory = [];
					this.rankedMatches = [];
					this.allMatches = [];
					if (this.lpChart) {
						this.lpChart.destroy();
						this.lpChart = null;
					}
					// Refresh stats to show "Unranked" state
					this.refresh();
					alert('Account unlinked successfully');
				}
			},
			error: (err) => {
				this.linkedSummonerLoading = false;
				alert('Error unlinking the account');
				console.error(err);
			}
		});
	}

	/**
	 * Trigger file input click to select avatar image
	 */
	openAvatarPicker() {
		const input = document.createElement('input');
		input.type = 'file';
		input.accept = 'image/png';
		input.onchange = (event: any) => {
			const file = event.target.files?.[0];
			if (file) {
				this.uploadAvatar(file);
			}
		};
		input.click();
	}

	/**
	 * Upload the selected avatar image
	 */
	uploadAvatar(file: File) {
		// Validate file size (max 5MB)
		const maxSize = 5 * 1024 * 1024; // 5MB
		if (file.size > maxSize) {
			this.avatarError = 'The file is too large. Maximum 5MB.';
			setTimeout(() => this.avatarError = null, 3000);
			return;
		}

		// Validate file type (only PNG)
		const allowedTypes = ['image/png'];
		if (!allowedTypes.includes(file.type.toLowerCase())) {
			this.avatarError = 'Please select a PNG file.';
			setTimeout(() => this.avatarError = null, 3000);
			return;
		}

		this.avatarUploading = true;
		this.avatarError = null;

		this.userService.uploadAvatar(file).subscribe({
			next: (res) => {
				console.log('Avatar upload response:', res);
				this.avatarUploading = false;
				if (res.success && res.avatarUrl) {
					// If the URL is relative, prepend the base URL
					let fullAvatarUrl = res.avatarUrl;
					if (fullAvatarUrl.startsWith('/api/')) {
						// Extract the base URL without the /api/v1 part
						const baseUrl = API_URL.replace('/api/v1', '');
						fullAvatarUrl = baseUrl + fullAvatarUrl;
					}
					console.log('Setting avatarUrl to:', fullAvatarUrl);
					this.avatarUrl = fullAvatarUrl;
					// Update stats if they contain user info
					if (this.stats) {
						this.stats.avatarUrl = fullAvatarUrl;
					}
				} else {
					console.error('Upload succeeded but no avatarUrl in response:', res);
					this.avatarError = 'Avatar uploaded but URL not available';
				}
			},
			error: (err) => {
				this.avatarUploading = false;
				this.avatarError = err.error?.message || 'Error uploading image';
				console.error('Avatar upload error:', err);
				setTimeout(() => this.avatarError = null, 3000);
			}
		});
	}

	/**
	 * Load user profile to get avatar URL
	 */
	loadUserProfile() {
		this.userService.getProfile().subscribe({
			next: (user) => {
				console.log('User profile loaded:', user);
				if (user.avatarUrl) {
					// If the URL is relative, prepend the base URL
					let fullAvatarUrl = user.avatarUrl;
					if (fullAvatarUrl.startsWith('/api/')) {
						// Extract the base URL without the /api/v1 part
						const baseUrl = API_URL.replace('/api/v1', '');
						fullAvatarUrl = baseUrl + fullAvatarUrl;
					}
					console.log('Setting avatarUrl from profile:', fullAvatarUrl);
					this.avatarUrl = fullAvatarUrl;
				} else {
					console.log('No avatarUrl in user profile');
				}
			},
			error: (err) => {
				console.error('Failed to load user profile:', err);
			}
		});
	}

	/**
	 * Avatar image loaded successfully
	 */
	onAvatarLoad() {
		console.log('Avatar image loaded successfully:', this.avatarUrl);
	}

	/**
	 * Avatar image failed to load
	 */
	onAvatarError(event: any) {
		console.error('Failed to load avatar image:', this.avatarUrl, event);
		this.avatarError = 'Failed to load avatar image';
		setTimeout(() => this.avatarError = null, 3000);
	}

	// ============================================
	// FAVORITE SUMMONERS MANAGEMENT
	// ============================================

	showAddFavoriteModal = false;
	addFavoriteName = '';
	addFavoriteLoading = false;
	addFavoriteError: string | null = null;

	/**
	 * Open modal to add a new favorite summoner
	 */
	openAddFavoriteModal() {
		this.showAddFavoriteModal = true;
		this.addFavoriteName = '';
		this.addFavoriteError = null;
	}

	/**
	 * Close add favorite modal
	 */
	closeAddFavoriteModal() {
		this.showAddFavoriteModal = false;
		this.addFavoriteName = '';
		this.addFavoriteError = null;
	}

	/**
	 * Add a summoner to favorites
	 */
	addFavorite() {
		if (!this.addFavoriteName || this.addFavoriteName.trim() === '') {
			this.addFavoriteError = 'Please enter a summoner name';
			return;
		}

		const input = this.addFavoriteName.trim();
		
		// Validate format: nombre#región
		if (!input.includes('#')) {
			this.addFavoriteError = 'Please use format: name#region (e.g., jae9104#EUW)';
			return;
		}
		
		const parts = input.split('#');
		const summonerName = parts[0].trim();
		const region = parts[1].trim().toUpperCase();
		
		// Validate region (only EUW supported for now)
		if (region !== 'EUW') {
			this.addFavoriteError = 'Currently only EUW region is supported';
			return;
		}

		if (!summonerName) {
			this.addFavoriteError = 'Please enter a valid summoner name';
			return;
		}

		this.addFavoriteLoading = true;
		this.addFavoriteError = null;

		// Send the full Riot ID (name#region) to the backend
		const riotId = `${summonerName}#${region}`;
		this.userService.addFavoriteSummoner(riotId).subscribe({
			next: (response) => {
				console.log('Summoner added to favorites:', response);
				this.addFavoriteLoading = false;
				this.closeAddFavoriteModal();
				// Reload favorites list
				this.loadFavorites();
			},
			error: (err) => {
				console.error('Failed to add favorite:', err);
				this.addFavoriteError = err.error?.message || 'Failed to add summoner to favorites';
				this.addFavoriteLoading = false;
			}
		});
	}

	/**
	 * Remove a summoner from favorites
	 */
	removeFavorite(summonerName: string) {
		if (!confirm(`Are you sure you want to remove ${summonerName} from your favorites?`)) {
			return;
		}

		this.userService.removeFavoriteSummoner(summonerName).subscribe({
			next: (response) => {
				console.log('Summoner removed from favorites:', response);
				// Reload favorites list
				this.loadFavorites();
			},
			error: (err) => {
				console.error('Failed to remove favorite:', err);
				this.favoritesError = err.error?.message || 'Failed to remove summoner from favorites';
				setTimeout(() => this.favoritesError = null, 3000);
			}
		});
	}

	/**
	 * Load favorites list from backend
	 */
	loadFavorites() {
		this.favoritesLoading = true;
		this.favoritesError = null;
		this.dashboardService.getFavoritesOverview().subscribe({
			next: (res) => {
				this.favorites = res || [];
				this.favoritesLoading = false;
			},
			error: (err: any) => {
				console.error('Failed to load favorites', err);
				this.favoritesError = 'Failed to load favorites.';
				this.favoritesLoading = false;
			}
		});
	}

	/**
	 * Open AI Analysis Modal
	 */
	openAiAnalysisModal() {
		this.showAiModal = true;
		this.aiAnalysis = null;
		this.aiAnalysisError = null;
	}

	/**
	 * Close AI Analysis Modal
	 */
	closeAiAnalysisModal() {
		this.showAiModal = false;
	}

	/**
	 * Generate AI-powered performance analysis
	 */
	generateAiAnalysis() {
		if (!this.linkedSummoner || !this.linkedSummoner.name) {
			this.aiAnalysisError = 'You must link your League of Legends account first';
			return;
		}

		if (this.aiMatchCount < 10 || this.aiMatchCount > 10) {
			this.aiAnalysisError = 'The number of matches must be 10';
			return;
		}

		this.aiAnalysisLoading = true;
		this.aiAnalysisError = null;
		this.aiAnalysis = null;

		this.dashboardService.getAiAnalysis(this.aiMatchCount).subscribe({
			next: (response) => {
				this.aiAnalysis = response.analysis;
				this.aiGeneratedAt = response.generatedAt;
				this.aiMatchesAnalyzed = response.matchesAnalyzed;
				this.aiAnalysisLoading = false;
				console.log('✅ AI Analysis generated successfully');
			},
			error: (err) => {
				console.error('Failed to generate AI analysis:', err);
				this.aiAnalysisError = err.error?.message || 'Failed to generate AI analysis. Please make sure you have enough matches played.';
				this.aiAnalysisLoading = false;
			}
		});
	}
}
