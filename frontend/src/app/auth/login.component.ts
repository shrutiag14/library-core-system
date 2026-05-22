import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { apiMessage } from '../core/api-error';
import { AuthService } from '../core/auth.service';
import { LoginPayload } from '../models/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-shell">
      <div class="panel auth-panel">
        <div class="mb-4">
          <h1 class="h3 mb-1">Library Core</h1>
          <p class="text-secondary mb-0">Sign in to continue.</p>
        </div>

        <form [formGroup]="loginForm" (ngSubmit)="login()">
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input class="form-control" type="email" formControlName="email" autocomplete="username">
          </div>
          <div class="mb-3">
            <label class="form-label">Password</label>
            <input class="form-control" type="password" formControlName="password" autocomplete="current-password">
          </div>
          <div *ngIf="error" class="alert alert-danger py-2">{{ error }}</div>
          <button class="btn btn-primary w-100" type="submit" [disabled]="loginForm.invalid || loading">Login</button>
        </form>

        <div class="text-center mt-3">
          <a routerLink="/setup">First-time setup</a>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent {
  loading = false;
  error = '';

  loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly router: Router) {}

  login(): void {
    this.loading = true;
    this.error = '';
    const payload = this.loginForm.getRawValue() as LoginPayload;
    this.auth.login(payload).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => this.fail(err)
    });
  }

  private fail(err: unknown): void {
    this.error = apiMessage(err);
    this.loading = false;
  }
}
