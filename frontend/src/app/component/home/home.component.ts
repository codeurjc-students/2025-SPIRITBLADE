import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})

export class HomeComponent {
  searchQuery = '';

  constructor(private router: Router) {}

  recentSearches = [
    { name: 'Example Player 1', rank: 'Gold II' },
    { name: 'Example Player 2', rank: 'Platinum IV' },
    { name: 'Example Player 3', rank: 'Diamond I' },
    { name: 'Example Player 4', rank: 'Master' }
  ];

  onSearch() {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/summoner', this.searchQuery.trim()]);
    }
  }

  searchSummoner(summonerName: string) {
    console.log('Searching for recent summoner:', summonerName);
    // TODO: Navegar al perfil del invocador
  }
}