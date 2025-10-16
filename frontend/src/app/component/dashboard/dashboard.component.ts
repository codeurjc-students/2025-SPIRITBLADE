import { Component, inject, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardService, RankHistoryEntry } from '../../service/dashboard.service';
import { UserService } from '../../service/user.service';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import { API_URL } from '../../service/api.config';

// Register Chart.js components
Chart.register(...registerables);

@Component({
	selector: 'app-dashboard',
	standalone: true,
	imports: [CommonModule, FormsModule],
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
				this.error = 'No se pudieron obtener las estadísticas del dashboard.';
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
				this.favoritesError = 'No se pudieron obtener los favoritos.';
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
	 * Load rank history for LP chart
	 */
	loadRankHistory() {
		this.chartLoading = true;
		this.chartError = null;
		
		this.dashboardService.getRankHistory().subscribe({
			next: (history) => {
				this.rankHistory = history;
				this.chartLoading = false;
				
				// Initialize chart after data is loaded
				setTimeout(() => this.initializeLPChart(), 100);
			},
			error: (err) => {
				console.error('Failed to load rank history', err);
				this.chartError = 'No se pudo cargar el historial de LP';
				this.chartLoading = false;
			}
		});
	}

	/**
	 * Initialize the LP progression chart
	 */
	private initializeLPChart() {
		if (!this.lpChartCanvas || this.rankHistory.length === 0) {
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

		// Prepare data
		const labels = this.rankHistory.map(entry => {
			const date = new Date(entry.date);
			return date.toLocaleDateString('es-ES', { month: 'short', day: 'numeric' });
		});

		const lpData = this.rankHistory.map(entry => {
			// Convert tier and rank to total LP for visualization
			return this.calculateTotalLP(entry.tier, entry.rank, entry.leaguePoints);
		});

		const winrateData = this.rankHistory.map(entry => {
			const total = entry.wins + entry.losses;
			return total > 0 ? (entry.wins / total) * 100 : 0;
		});

		const config: ChartConfiguration = {
			type: 'line',
			data: {
				labels: labels,
				datasets: [
					{
						label: 'League Points',
						data: lpData,
						borderColor: 'rgb(75, 192, 192)',
						backgroundColor: 'rgba(75, 192, 192, 0.2)',
						tension: 0.3,
						fill: true,
						yAxisID: 'y'
					},
					{
						label: 'Win Rate (%)',
						data: winrateData,
						borderColor: 'rgb(255, 99, 132)',
						backgroundColor: 'rgba(255, 99, 132, 0.2)',
						tension: 0.3,
						fill: false,
						yAxisID: 'y1'
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
						text: 'LP Progression & Win Rate',
						color: '#e0e0e0',
						font: {
							size: 16,
							weight: 'bold'
						}
					},
					legend: {
						display: true,
						position: 'top',
						labels: {
							color: '#e0e0e0',
							usePointStyle: true,
							padding: 15
						}
					},
					tooltip: {
						callbacks: {
							label: (context) => {
								const label = context.dataset?.label || '';
								const parsed: any = context.parsed;
								let valueNum = 0;
								if (typeof parsed === 'number') {
									valueNum = parsed as number;
								} else if (parsed && typeof parsed === 'object' && typeof parsed.y === 'number') {
									valueNum = parsed.y as number;
								}

								if (context.datasetIndex === 1) {
									return `${label}: ${valueNum.toFixed(1)}%`;
								}

								return `${label}: ${Math.round(valueNum)} LP`;
							},
							afterLabel: (context) => {
								const index = context.dataIndex;
								const entry = this.rankHistory?.[index];
								if (!entry) return '';
								return `${entry.tier} ${entry.rank} - ${entry.wins}W ${entry.losses}L`;
							}
						}
					}
				},
				scales: {
					x: {
						display: true,
						title: {
							display: true,
							text: 'Date',
							color: '#e0e0e0'
						},
						ticks: {
							color: '#b0b0b0'
						},
						grid: {
							color: 'rgba(255, 255, 255, 0.1)'
						}
					},
					y: {
						type: 'linear',
						display: true,
						position: 'left',
						title: {
							display: true,
							text: 'League Points',
							color: 'rgb(75, 192, 192)'
						},
						ticks: {
							color: 'rgb(75, 192, 192)'
						},
						grid: {
							color: 'rgba(75, 192, 192, 0.2)'
						}
					},
					y1: {
						type: 'linear',
						display: true,
						position: 'right',
						title: {
							display: true,
							text: 'Win Rate (%)',
							color: 'rgb(255, 99, 132)'
						},
						ticks: {
							color: 'rgb(255, 99, 132)',
							callback: (value) => `${value}%`
						},
						grid: {
							drawOnChartArea: false,
						},
						min: 0,
						max: 100
					}
				}
			}
		};

		this.lpChart = new Chart(ctx, config);
	}

	/**
	 * Calculate total LP for a given tier, rank, and LP
	 * This helps visualize progression across divisions
	 */
	private calculateTotalLP(tier: string, rank: string, lp: number): number {
		const tierValues: { [key: string]: number } = {
			'IRON': 0,
			'BRONZE': 400,
			'SILVER': 800,
			'GOLD': 1200,
			'PLATINUM': 1600,
			'EMERALD': 2000,
			'DIAMOND': 2400,
			'MASTER': 2800,
			'GRANDMASTER': 3200,
			'CHALLENGER': 3600
		};

		const rankValues: { [key: string]: number } = {
			'IV': 0,
			'III': 100,
			'II': 200,
			'I': 300
		};

		const tierLP = tierValues[tier.toUpperCase()] || 0;
		const rankLP = rankValues[rank.toUpperCase()] || 0;

		return tierLP + rankLP + lp;
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
			this.linkError = 'Por favor, introduce un nombre de invocador';
			return;
		}

		this.linkLoading = true;
		this.linkError = null;
		this.linkSuccess = null;

		this.userService.linkSummoner(this.summonerName.trim(), this.selectedRegion).subscribe({
			next: (res) => {
				this.linkLoading = false;
				if (res.success) {
					this.linkSuccess = res.message || 'Cuenta vinculada correctamente';
					// Reload linked summoner (which will also load rank history)
					this.loadLinkedSummoner();
					// Close modal after 2 seconds
					setTimeout(() => {
						this.closeLinkModal();
					}, 2000);
				} else {
					this.linkError = res.message || 'Error al vincular la cuenta';
				}
			},
			error: (err) => {
				this.linkLoading = false;
				this.linkError = err.error?.message || 'Error al vincular la cuenta. Verifica el nombre del invocador.';
			}
		});
	}

	/**
	 * Unlink the current League of Legends account
	 */
	unlinkAccount() {
		if (!confirm('¿Estás seguro de que quieres desvincular tu cuenta de League of Legends?')) {
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
					if (this.lpChart) {
						this.lpChart.destroy();
						this.lpChart = null;
					}
					alert('Cuenta desvinculada correctamente');
				}
			},
			error: (err) => {
				this.linkedSummonerLoading = false;
				alert('Error al desvincular la cuenta');
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
			this.avatarError = 'El archivo es demasiado grande. Máximo 5MB.';
			setTimeout(() => this.avatarError = null, 3000);
			return;
		}

		// Validate file type (only PNG and JPG)
		const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg'];
		if (!allowedTypes.includes(file.type.toLowerCase())) {
			this.avatarError = 'Por favor selecciona solo archivos PNG o JPG.';
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
					this.avatarError = 'Avatar subido pero URL no disponible';
				}
			},
			error: (err) => {
				this.avatarUploading = false;
				this.avatarError = err.error?.message || 'Error al subir la imagen';
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
		this.avatarError = 'No se pudo cargar la imagen del avatar';
		setTimeout(() => this.avatarError = null, 3000);
	}
}
