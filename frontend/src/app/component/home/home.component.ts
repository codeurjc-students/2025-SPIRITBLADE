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
    if (this.searchQuery && this.searchQuery.trim()) {
      this.router.navigate(['/summoner', this.searchQuery.trim()]);
    }
  }

  searchSummoner(summoner: Summoner) {
    this.router.navigate(['/summoner', summoner.name]);
  }
}