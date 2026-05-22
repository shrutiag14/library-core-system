import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { apiMessage } from '../core/api-error';
import { MemberService } from '../core/member.service';
import { Member, MemberPayload, MemberStatus } from '../models/member';

@Component({
  selector: 'app-members',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="toolbar">
      <div>
        <h1 class="h3 mb-1">Members</h1>
        <p class="text-secondary mb-0">Maintain active/inactive borrowers.</p>
      </div>
      <button class="btn btn-outline-primary" type="button" (click)="load()">Refresh</button>
    </div>

    <div class="row g-3">
      <div class="col-lg-4">
        <form class="panel" [formGroup]="form" (ngSubmit)="save()">
          <h2 class="h5">{{ editing ? 'Edit member' : 'Add member' }}</h2>
          <div class="mb-2">
            <label class="form-label">Name</label>
            <input class="form-control" formControlName="name">
          </div>
          <div class="mb-2">
            <label class="form-label">Email</label>
            <input class="form-control" formControlName="email">
          </div>
          <div class="mb-3">
            <label class="form-label">Status</label>
            <select class="form-select" formControlName="status">
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
            </select>
          </div>
          <div *ngIf="error" class="alert alert-danger py-2">{{ error }}</div>
          <div *ngIf="success" class="alert alert-success py-2">{{ success }}</div>
          <div class="form-actions">
            <button class="btn btn-outline-secondary" type="button" (click)="reset()">Clear</button>
            <button class="btn btn-primary" type="submit" [disabled]="form.invalid || loading">Save</button>
          </div>
        </form>
      </div>

      <div class="col-lg-8">
        <div class="panel">
          <div class="d-flex gap-2 mb-3">
            <input
              class="form-control"
              placeholder="Search name or email"
              [(ngModel)]="searchText"
              (ngModelChange)="onSearchChange($event)">
          </div>
          <div *ngIf="loading" class="alert alert-info">Loading members...</div>
          <div class="table-responsive">
            <table class="table table-hover">
              <thead><tr><th>Name</th><th>Email</th><th>Status</th><th></th></tr></thead>
              <tbody>
                <tr *ngFor="let member of members">
                  <td class="fw-semibold">{{ member.name }}</td>
                  <td>{{ member.email }}</td>
                  <td>
                    <span class="status-pill" [class.bg-success-subtle]="member.status === 'ACTIVE'" [class.bg-secondary-subtle]="member.status === 'INACTIVE'">
                      {{ member.status }}
                    </span>
                  </td>
                  <td class="text-end">
                    <button class="btn btn-sm btn-outline-secondary me-1" type="button" (click)="edit(member)">Edit</button>
                    <button class="btn btn-sm btn-outline-warning" type="button" (click)="deactivate(member)" [disabled]="member.status === 'INACTIVE'">Deactivate</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `
})
export class MembersComponent implements OnInit, OnDestroy {
  members: Member[] = [];
  editing?: Member;
  searchText = '';
  loading = false;
  error = '';
  success = '';
  private readonly searchChanges = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    status: this.fb.nonNullable.control<MemberStatus>('ACTIVE', Validators.required)
  });

  constructor(private readonly fb: FormBuilder, private readonly service: MemberService) {}

  ngOnInit(): void {
    this.searchChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => this.load());
    this.load();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearchChange(value: string): void {
    this.searchChanges.next(value);
  }

  load(): void {
    this.loading = true;
    this.service.list(this.searchText).subscribe({
      next: (page) => {
        this.members = page.content;
        this.loading = false;
      },
      error: (err) => this.fail(err)
    });
  }

  save(): void {
    this.error = '';
    this.success = '';
    const payload = this.form.getRawValue() as MemberPayload;
    const request = this.editing ? this.service.update(this.editing.id, payload) : this.service.create(payload);
    request.subscribe({
      next: () => {
        this.success = 'Member saved.';
        this.reset();
        this.load();
      },
      error: (err) => this.fail(err)
    });
  }

  edit(member: Member): void {
    this.editing = member;
    this.form.patchValue(member);
  }

  deactivate(member: Member): void {
    this.service.deactivate(member.id).subscribe({
      next: () => {
        this.success = 'Member deactivated.';
        this.load();
      },
      error: (err) => this.fail(err)
    });
  }

  reset(): void {
    this.editing = undefined;
    this.form.reset({ name: '', email: '', status: 'ACTIVE' });
  }

  private fail(err: unknown): void {
    this.error = apiMessage(err);
    this.loading = false;
  }
}
