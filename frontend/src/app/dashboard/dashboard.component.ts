import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoanService } from '../core/loan.service';
import { Dashboard } from '../models/loan';
import { apiMessage } from '../core/api-error';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toolbar">
      <div>
        <h1 class="h3 mb-1">Dashboard</h1>
        <p class="text-secondary mb-0">Library stock, active circulation, overdue risk.</p>
      </div>
      <button class="btn btn-outline-primary" type="button" (click)="load()" [disabled]="loading">Refresh</button>
    </div>

    <div *ngIf="error" class="alert alert-danger">{{ error }}</div>
    <div *ngIf="loading" class="alert alert-info">Loading dashboard...</div>

    <div class="row g-3" *ngIf="dashboard">
      <div class="col-md-3" *ngFor="let item of cards">
        <div class="panel">
          <div class="text-secondary small text-uppercase fw-bold">{{ item.label }}</div>
          <div class="display-6 fw-bold">{{ item.value }}</div>
        </div>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  dashboard?: Dashboard;
  loading = false;
  error = '';

  constructor(private readonly loans: LoanService) {}

  get cards() {
    return [
      { label: 'Books', value: this.dashboard?.books ?? 0 },
      { label: 'Members', value: this.dashboard?.members ?? 0 },
      { label: 'Active loans', value: this.dashboard?.activeLoans ?? 0 },
      { label: 'Overdue', value: this.dashboard?.overdueLoans ?? 0 },
      { label: 'Outstanding fines', value: `₹${(this.dashboard?.outstandingFines ?? 0).toFixed(2)}` }
    ];
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.loans.dashboard().subscribe({
      next: (dashboard) => {
        this.dashboard = dashboard;
        this.loading = false;
      },
      error: (err) => {
        this.error = apiMessage(err);
        this.loading = false;
      }
    });
  }
}
