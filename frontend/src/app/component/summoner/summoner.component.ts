import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { SummonerService } from '../../service/summoner.service';
import { Summoner } from '../../dto/summoner.model';
import { ChampionMastery } from '../../dto/champion-mastery.model';
import { MatchHistory } from '../../dto/match-history.model';
import { MatchDetailDTO } from '../../dto/match-detail.model';
import { ParticipantDTO } from '../../dto/participant.model';

@Component({
	selector: 'app-summoner',
	standalone: true,
	imports: [CommonModule, FormsModule, RouterModule],
	templateUrl: './summoner.component.html',
	styleUrls: ['./summoner.component.scss']
})
export class SummonerComponent implements OnInit {
	searchQuery: string = '';
	summoner: Summoner | null = null;
	championMasteries: ChampionMastery[] = [];
	matchHistory: MatchHistory[] = [];
	loading = false;
	loadingMasteries = false;
	loadingMatches = false;
	error: string | null = null;
	
	// Pagination for matches
	currentMatchPage = 0;
	matchPageSize = 5;
	hasMoreMatches = true;
	
	// Track expanded matches for toggle functionality
	expandedMatches: Set<string> = new Set<string>();
	
	// Store loaded match details
	matchDetails: Map<string, MatchDetailDTO> = new Map<string, MatchDetailDTO>();
	loadingMatchDetails: Set<string> = new Set<string>();

	private router: Router;
	private route: ActivatedRoute;
	private summonerService: SummonerService;

	constructor(router: Router, route: ActivatedRoute, summonerService: SummonerService) {
		this.router = router;
		this.route = route;
		this.summonerService = summonerService;
	}

	ngOnInit(): void {
		// Read the route param and load the summoner
		this.route.paramMap.subscribe(params => {
			const name = params.get('name');
			if (name) {
				this.searchQuery = name;
				this.loadSummoner(name);
			}
		});
	}

	loadSummoner(name: string) {
		this.loading = true;
		this.error = null;
		this.summoner = null;
		this.championMasteries = [];
		this.matchHistory = [];
		this.currentMatchPage = 0;
		this.hasMoreMatches = true;
		
		this.summonerService.getByName(name).subscribe({
			next: s => {
				this.summoner = s;
				this.loading = false;
				// Load additional data
				this.loadChampionMasteries(name);
				this.loadMatchHistory(name);
			},
			error: err => {
				console.debug('Error loading summoner:', err);
				if (err.status === 404) {
					this.error = `Summoner "${name}" not found. Make sure to use the format: gameName#tagLine`;
				} else if (err.status === 401) {
					this.error = 'Riot API authentication error. Please check the API key.';
				} else {
					this.error = 'Unable to load summoner. Please try again later.';
				}
				this.loading = false;
			}
		});
	}
	
	loadChampionMasteries(name: string) {
		this.loadingMasteries = true;
		this.summonerService.getTopChampions(name).subscribe({
			next: masteries => {
				this.championMasteries = masteries;
				this.loadingMasteries = false;
			},
			error: err => {
				console.debug('Error loading champion masteries:', err);
				// Keep UI informative but non-blocking
				this.loadingMasteries = false;
			}
		});
	}
	
	loadMatchHistory(name: string, page: number = 0) {
		this.loadingMatches = true;
		this.summonerService.getRecentMatches(name, page, this.matchPageSize).subscribe({
			next: matches => {
				if (page === 0) {
					// First page - replace matches
					this.matchHistory = matches;
				} else {
					// Subsequent pages - append matches
					this.matchHistory = [...this.matchHistory, ...matches];
				}
				
				// Check if there are more matches
				this.hasMoreMatches = matches.length === this.matchPageSize;
				this.currentMatchPage = page;
				this.loadingMatches = false;
				console.log(`Loaded match history for summoner ${name}:`, this.matchHistory);
			},
			error: err => {
				console.debug('Error loading match history:', err);
				this.loadingMatches = false;
			}
		});
	}
	
	loadNextMatchPage() {
		if (!this.loadingMatches && this.hasMoreMatches && this.summoner) {
			this.loadMatchHistory(this.searchQuery, this.currentMatchPage + 1);
		}
	}
	
	loadPreviousMatchPage() {
		if (!this.loadingMatches && this.currentMatchPage > 0 && this.summoner) {
			// Reset to first page for previous functionality
			this.currentMatchPage = 0;
			this.loadMatchHistory(this.searchQuery, 0);
		}
	}

	onSearch(): void {
		const name = (this.searchQuery || '').trim();
		if (!name) {
			this.error = 'Please enter a summoner name';
			return;
		}
		
		// Validate Riot ID format (gameName#tagLine)
		if (!name.includes('#')) {
			this.error = 'Invalid format. Please use: gameName#tagLine (e.g., Player#NA)';
			return;
		}
		
		this.error = null;
		this.router.navigate(['/summoner', name]);
	}

	getWinRate(): string {
		if (!this.summoner || !this.summoner.wins || !this.summoner.losses) {
			return '0%';
		}
		const total = this.summoner.wins + this.summoner.losses;
		if (total === 0) return '0%';
		const rate = (this.summoner.wins / total) * 100;
		return rate.toFixed(1) + '%';
	}

	getTotalGames(): number {
		if (!this.summoner || !this.summoner.wins || !this.summoner.losses) {
			return 0;
		}
		return this.summoner.wins + this.summoner.losses;
	}

	getKda(match: MatchHistory): string {
		return `${match.kills || 0}/${match.deaths || 0}/${match.assists || 0}`;
	}

	getGameDuration(match: MatchHistory): string {
		if (!match.gameDuration) return '0m';
		const minutes = Math.floor(match.gameDuration / 60);
		return `${minutes}m`;
	}

	getChampionName(mastery: ChampionMastery): string {
		return mastery.championName || 'Unknown Champion';
	}

	getChampionPoints(mastery: ChampionMastery): string {
		return `${mastery.championPoints || 0} points`;
	}

	getChampionLevel(mastery: ChampionMastery): string {
		return `Level ${mastery.championLevel || 0}`;
	}

	/**
	 * Toggle the expanded state of a match card
	 * Loads match details on first expansion
	 */
	toggleMatchDetails(matchId: string): void {
		if (this.expandedMatches.has(matchId)) {
			// Collapse
			this.expandedMatches.delete(matchId);
		} else {
			// Expand
			this.expandedMatches.add(matchId);
			
			// Load details if not already loaded
			if (!this.matchDetails.has(matchId) && !this.loadingMatchDetails.has(matchId)) {
				this.loadMatchDetails(matchId);
			}
		}
	}
	
	/**
	 * Load complete match details from API
	 */
	private loadMatchDetails(matchId: string): void {
		this.loadingMatchDetails.add(matchId);
		
		this.summonerService.getMatchDetails(matchId).subscribe({
			next: details => {
				this.matchDetails.set(matchId, details);
				this.loadingMatchDetails.delete(matchId);
				console.log(`Loaded details for match ${matchId}:`, details);
			},
			error: err => {
				console.debug('Error loading match details:', err);
				this.loadingMatchDetails.delete(matchId);
			}
		});
	}

	/**
	 * Check if a match is currently expanded
	 */
	isMatchExpanded(matchId: string): boolean {
		return this.expandedMatches.has(matchId);
	}

	/**
	 * Get formatted timestamp for a match
	 */
	getMatchTimestamp(match: MatchHistory): string {
		if (!match.gameTimestamp) return 'Unknown';
		const date = new Date(match.gameTimestamp * 1000); // Convert from seconds to milliseconds
		return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
	}

	/**
	 * Calculate KDA ratio
	 */
	getKdaRatio(match: MatchHistory): string {
		const kills = match.kills || 0;
		const deaths = match.deaths || 0;
		const assists = match.assists || 0;
		
		if (deaths === 0) {
			return 'Perfect';
		}
		
		const ratio = (kills + assists) / deaths;
		return ratio.toFixed(2);
	}
	
	/**
	 * Get loaded match details for a match
	 */
	getMatchDetail(matchId: string): MatchDetailDTO | null {
		return this.matchDetails.get(matchId) || null;
	}
	
	/**
	 * Check if match details are loading
	 */
	isLoadingMatchDetails(matchId: string): boolean {
		return this.loadingMatchDetails.has(matchId);
	}
	
	/**
	 * Get formatted gold amount with K suffix
	 */
	getFormattedGold(gold: number | undefined): string {
		if (!gold) return '0';
		return gold >= 1000 ? (gold / 1000).toFixed(1) + 'K' : gold.toString();
	}
	
	/**
	 * Get formatted damage with K suffix
	 */
	getFormattedDamage(damage: number | undefined): string {
		if (!damage) return '0';
		return damage >= 1000 ? (damage / 1000).toFixed(1) + 'K' : damage.toString();
	}
	
	/**
	 * Get participant KDA ratio
	 */
	getParticipantKdaRatio(participant: ParticipantDTO): string {
		const kills = participant.kills || 0;
		const deaths = participant.deaths || 0;
		const assists = participant.assists || 0;
		
		if (deaths === 0) {
			return 'Perfect';
		}
		
		const ratio = (kills + assists) / deaths;
		return ratio.toFixed(2);
	}

	/**
	 * Format a participant into the route parameter used by the Summoner page.
	 * Preferred: use Riot ID (gameName#tagLine) when available. Fallback to summonerName.
	 */
	formatSummonerParam(participant: ParticipantDTO): string {
		if (!participant) return '';
		// If Riot ID components exist, prefer them (gameName + # + tagline)
		if (participant.riotIdGameName) {
			const tag = participant.riotIdTagline ? participant.riotIdTagline : '';
			return tag ? `${participant.riotIdGameName}#${tag}` : participant.riotIdGameName;
		}
		// If summonerName already contains a hashtag, assume it's correctly formatted
		if (participant.summonerName && participant.summonerName.includes('#')) {
			return participant.summonerName;
		}
		// Otherwise return summonerName
		return participant.summonerName || '';
	}
	
	/**
	 * Get team name (Blue/Red)
	 */
	getTeamName(teamId: number | undefined): string {
		return teamId === 100 ? 'Blue Team' : 'Red Team';
	}

}
