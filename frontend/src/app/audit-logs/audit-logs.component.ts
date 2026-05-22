import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { apiMessage } from '../core/api-error';
import { AuditLogService } from '../core/audit-log.service';
import { AuditLog } from '../models/audit-log';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toolbar">
      <div>
        <h1 class="h3 mb-1">Audit Logs</h1>
        <p class="text-secondary mb-0">Admin activity and circulation events.</p>
      </div>
      <button class="btn btn-outline-primary" type="button" (click)="load()" [disabled]="loading">Refresh</button>
    </div>

    <div *ngIf="error" class="alert alert-danger">{{ error }}</div>
    <div *ngIf="loading" class="alert alert-info">Loading audit logs...</div>

    <div class="panel">
      <div class="table-responsive">
        <table class="table table-hover">
          <thead>
            <tr><th>Time</th><th>Actor</th><th>Action</th><th>Entity</th><th>Details</th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let log of logs">
              <td>{{ log.createdAt | date:'short' }}</td>
              <td>{{ log.actorEmail }}</td>
              <td><span class="status-pill bg-primary-subtle">{{ log.action }}</span></td>
              <td>{{ log.entityType }} #{{ log.entityId || '-' }}</td>
              <td>{{ log.details || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class AuditLogsComponent implements OnInit {
  logs: AuditLog[] = [];
  loading = false;
  error = '';

  constructor(private readonly service: AuditLogService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.service.list().subscribe({
      next: (page) => {
        this.logs = page.content;
        this.loading = false;
      },
      error: (err) => {
        this.error = apiMessage(err);
        this.loading = false;
      }
    });
  }
}
