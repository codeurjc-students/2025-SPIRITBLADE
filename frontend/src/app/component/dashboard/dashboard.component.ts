import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../service/dashboard.service';

@Component({
	selector: 'app-dashboard',
	standalone: true,
	imports: [CommonModule],
	templateUrl: './dashboard.component.html',
	styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent {
	private dashboardService = inject(DashboardService);

	loading = false;
	stats: any = null;
	favorites: any[] = [];
	favoritesLoading = false;
	favoritesError: string | null = null;
	error: string | null = null;

	refresh() {
		this.loading = true;
		this.error = null;
		this.dashboardService.getPersonalStats().subscribe({
			next: (res) => {
				this.stats = res;
				this.loading = false;
			},
			error: (err: any) => {
				console.error('Failed to refresh dashboard', err);
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
				console.error('Failed to load favorites', err);
				this.favoritesError = 'No se pudieron obtener los favoritos.';
				this.favoritesLoading = false;
			}
		});
	}

	ngOnInit() {
		this.refresh();
	}
}
