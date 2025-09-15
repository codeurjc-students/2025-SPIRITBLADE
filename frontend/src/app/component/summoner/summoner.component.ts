import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
	selector: 'app-summoner',
	standalone: true,
	imports: [CommonModule, FormsModule, RouterModule],
	templateUrl: './summoner.component.html',
	styleUrls: ['./summoner.component.scss']
})
export class SummonerComponent {
	searchQuery: string = '';
	private router: Router;

	constructor(router: Router) {
		this.router = router;
	}

	onSearch(): void {
		const name = (this.searchQuery || '').trim();
		if (!name) return;
		// Navigate to the summoner route. Adjust the route param name if different in your app.
		this.router.navigate(['/summoner', name]);
	}

}
