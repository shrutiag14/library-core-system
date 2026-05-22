import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { apiMessage } from '../core/api-error';
import { AuthService } from '../core/auth.service';
import { CreateUserPayload, UserRole } from '../models/auth';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="toolbar">
      <div>
        <h1 class="h3 mb-1">Users</h1>
        <p class="text-secondary mb-0">Create staff accounts and assign access.</p>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-lg-5">
        <form class="panel" [formGroup]="form" (ngSubmit)="create()">
          <h2 class="h5">Create user</h2>
          <div class="mb-2">
            <label class="form-label">Full name</label>
            <input class="form-control" formControlName="fullName">
          </div>
          <div class="mb-2">
            <label class="form-label">Email</label>
            <input class="form-control" type="email" formControlName="email">
          </div>
          <div class="mb-2">
            <label class="form-label">Password</label>
            <input class="form-control" type="password" formControlName="password">
          </div>
          <div class="mb-3">
            <label class="form-label">Role</label>
            <select class="form-select" formControlName="role">
              <option value="LIBRARIAN">LIBRARIAN</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>
          <div *ngIf="error" class="alert alert-danger py-2">{{ error }}</div>
          <div *ngIf="success" class="alert alert-success py-2">{{ success }}</div>
          <div class="form-actions">
            <button class="btn btn-primary" type="submit" [disabled]="form.invalid || loading">Create</button>
          </div>
        </form>
      </div>
    </div>
  `
})
export class UsersComponent {
  loading = false;
  error = '';
  success = '';

  form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: this.fb.nonNullable.control<UserRole>('LIBRARIAN', Validators.required)
  });

  constructor(private readonly fb: FormBuilder, private readonly auth: AuthService) {}

  create(): void {
    this.loading = true;
    this.error = '';
    this.success = '';
    const payload = this.form.getRawValue() as CreateUserPayload;
    this.auth.createUser(payload).subscribe({
      next: () => {
        this.success = 'User created.';
        this.form.reset({ fullName: '', email: '', password: '', role: 'LIBRARIAN' });
        this.loading = false;
      },
      error: (err) => this.fail(err)
    });
  }

  private fail(err: unknown): void {
    this.error = apiMessage(err);
    this.loading = false;
  }
}
