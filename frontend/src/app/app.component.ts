import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar navbar-expand-lg bg-white border-bottom" *ngIf="auth.isAuthenticated()">
      <div class="container-fluid page-shell py-0">
        <a class="navbar-brand fw-bold" routerLink="/">Library Core</a>
        <div class="navbar-nav flex-row gap-2">
          <a class="nav-link" routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Dashboard</a>
          <a class="nav-link" routerLink="/books" routerLinkActive="active">Books</a>
          <a class="nav-link" routerLink="/members" routerLinkActive="active">Members</a>
          <a class="nav-link" routerLink="/loans" routerLinkActive="active">Issue/Return</a>
          <a class="nav-link" *ngIf="auth.isAdmin()" routerLink="/users" routerLinkActive="active">Users</a>
          <a class="nav-link" *ngIf="auth.isAdmin()" routerLink="/audit-logs" routerLinkActive="active">Audit Logs</a>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span class="text-secondary small">{{ auth.user()?.fullName }} · {{ auth.user()?.role }}</span>
          <button class="btn btn-sm btn-outline-secondary" type="button" (click)="logout()">Logout</button>
        </div>
      </div>
    </nav>
    <main class="page-shell">
      <router-outlet></router-outlet>
    </main>
  `
})
export class AppComponent {
  constructor(public readonly auth: AuthService, private readonly router: Router) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
