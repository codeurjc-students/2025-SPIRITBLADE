import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-error',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.scss']
})
export class ErrorComponent implements OnInit {
  errorCode: string = '500';
  errorMessage: string = 'An unexpected error occurred';
  errorDetails: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Get error data from query params
    this.route.queryParams.subscribe(params => {
      this.errorCode = params['code'] || '500';
      this.errorMessage = params['message'] || 'An unexpected error occurred';
      this.errorDetails = params['details'] || '';
    });
  }

  goHome(): void {
    this.router.navigate(['/']);
  }

  goBack(): void {
    window.history.back();
  }
}
