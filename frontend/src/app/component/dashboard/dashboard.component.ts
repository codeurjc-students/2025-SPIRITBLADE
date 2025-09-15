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
	}

	ngOnInit() {
		this.refresh();
	}
}
