import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { apiMessage } from '../core/api-error';
import { AuthService } from '../core/auth.service';
import { CreateUserPayload } from '../models/auth';

@Component({
  selector: 'app-setup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-shell">
      <div class="panel auth-panel">
        <div class="mb-4">
          <h1 class="h3 mb-1">First-time setup</h1>
          <p class="text-secondary mb-0">Create first admin account.</p>
        </div>

        <form [formGroup]="form" (ngSubmit)="create()">
          <div class="mb-3">
            <label class="form-label">Full name</label>
            <input class="form-control" formControlName="fullName">
          </div>
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input class="form-control" type="email" formControlName="email" autocomplete="username">
          </div>
          <div class="mb-3">
            <label class="form-label">Password</label>
            <input class="form-control" type="password" formControlName="password" autocomplete="new-password">
          </div>
          <div *ngIf="error" class="alert alert-danger py-2">{{ error }}</div>
          <div *ngIf="success" class="alert alert-success py-2">{{ success }}</div>
          <button class="btn btn-primary w-100" type="submit" [disabled]="form.invalid || loading">Create admin</button>
        </form>

        <div class="text-center mt-3">
          <a routerLink="/login">Back to login</a>
        </div>
      </div>
    </div>
  `
})
export class SetupComponent {
  loading = false;
  error = '';
  success = '';

  form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly router: Router) {}

  create(): void {
    this.loading = true;
    this.error = '';
    this.success = '';
    const raw = this.form.getRawValue();
    const payload: CreateUserPayload = { ...raw, role: 'ADMIN' };
    this.auth.bootstrap(payload).subscribe({
      next: () => {
        this.success = 'Admin created. Redirecting to login.';
        setTimeout(() => this.router.navigate(['/login']), 700);
      },
      error: (err) => this.fail(err)
    });
  }

  private fail(err: unknown): void {
    this.error = apiMessage(err);
    this.loading = false;
  }
}
