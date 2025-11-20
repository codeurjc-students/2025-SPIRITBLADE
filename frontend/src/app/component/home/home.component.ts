import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { SummonerService } from '../../service/summoner.service';
import { Summoner } from '../../dto/summoner.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})

export class HomeComponent implements OnInit {
  searchQuery = '';
  recentSearches: Summoner[] = [];
  loadingRecentSearches = false;
  searchError: string | null = null;

  constructor(
    private router: Router,
    private summonerService: SummonerService
  ) {}

  ngOnInit(): void {
    this.loadRecentSearches();
  }

  loadRecentSearches() {
    this.loadingRecentSearches = true;
    this.summonerService.getRecentSearches().subscribe({
      next: searches => {
        this.recentSearches = searches;
        this.loadingRecentSearches = false;
      },
      error: err => {
        console.debug('Error loading recent searches:', err);
        this.loadingRecentSearches = false;
      }
    });
  }

  onSearch() {
    this.searchError = null;
    
    if (!this.searchQuery || !this.searchQuery.trim()) {
      this.searchError = 'Please enter a summoner name';
      return;
    }

    const input = this.searchQuery.trim();
    
    // Validate format: nombre#región
    if (!input.includes('#')) {
      this.searchError = 'Please use format: name#region (e.g., jae9104#NA1)';
      return;
    }
    
    const parts = input.split('#');
    const summonerName = parts[0].trim();
    const region = parts[1].trim().toUpperCase();
    
    if (!summonerName) {
      this.searchError = 'Please enter a valid summoner name';
      return;
    }

    // Navigate with the full Riot ID
    this.router.navigate(['/summoner', input]);
  }

  searchSummoner(summoner: Summoner) {
    this.router.navigate(['/summoner', summoner.name]);
  }
}