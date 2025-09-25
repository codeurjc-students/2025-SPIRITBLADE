import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { SummonerService } from '../../service/summoner.service';
import { Summoner } from '../../dto/summoner.model';

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
	loading = false;
	error: string | null = null;

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
		this.summonerService.getByName(name).subscribe({
			next: s => {
				this.summoner = s;
				this.loading = false;
			},
			error: err => {
				this.error = 'Unable to load summoner';
				this.loading = false;
			}
		});
	}

	onSearch(): void {
		const name = (this.searchQuery || '').trim();
		if (!name) return;
		this.router.navigate(['/summoner', name]);
	}

}
