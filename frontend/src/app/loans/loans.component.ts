import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { apiMessage } from '../core/api-error';
import { BookService } from '../core/book.service';
import { LoanService } from '../core/loan.service';
import { MemberService } from '../core/member.service';
import { Book } from '../models/book';
import { IssuePayload, Loan } from '../models/loan';
import { Member } from '../models/member';

@Component({
  selector: 'app-loans',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="toolbar">
      <div>
        <h1 class="h3 mb-1">Issue/Return</h1>
        <p class="text-secondary mb-0">Issue available books, return active loans, inspect history.</p>
      </div>
      <button class="btn btn-outline-primary" type="button" (click)="loadAll()">Refresh</button>
    </div>

    <div class="row g-3">
      <div class="col-lg-4">
        <form class="panel" [formGroup]="form" (ngSubmit)="issue()">
          <h2 class="h5">Issue book</h2>
          <div class="mb-2">
            <label class="form-label">Book</label>
            <select class="form-select" formControlName="bookId">
              <option [ngValue]="0">Select book</option>
              <option *ngFor="let book of books" [ngValue]="book.id" [disabled]="book.availableCopies <= 0">
                {{ book.title }} ({{ book.availableCopies }} available)
              </option>
            </select>
          </div>
          <div class="mb-2">
            <label class="form-label">Member</label>
            <select class="form-select" formControlName="memberId">
              <option [ngValue]="0">Select member</option>
              <option *ngFor="let member of activeMembers" [ngValue]="member.id">{{ member.name }} · {{ member.email }}</option>
            </select>
          </div>
          <div class="mb-3">
            <label class="form-label">Due date</label>
            <input class="form-control" type="date" formControlName="dueDate">
          </div>
          <div *ngIf="error" class="alert alert-danger py-2">{{ error }}</div>
          <div *ngIf="success" class="alert alert-success py-2">{{ success }}</div>
          <div class="form-actions">
            <button class="btn btn-primary" type="submit" [disabled]="form.invalid || loading">Issue</button>
          </div>
        </form>
      </div>

      <div class="col-lg-8">
        <div class="panel">
          <div class="d-flex gap-2 mb-3">
            <select class="form-select" [(ngModel)]="memberFilter">
              <option [ngValue]="0">All members</option>
              <option *ngFor="let member of members" [ngValue]="member.id">{{ member.name }}</option>
            </select>
            <select class="form-select" [(ngModel)]="bookFilter">
              <option [ngValue]="0">All books</option>
              <option *ngFor="let book of books" [ngValue]="book.id">{{ book.title }}</option>
            </select>
            <button class="btn btn-outline-primary" type="button" (click)="loadHistory()">Filter</button>
          </div>
          <div *ngIf="loading" class="alert alert-info">Loading loans...</div>
          <div class="table-responsive">
            <table class="table table-hover">
              <thead><tr><th>Book</th><th>Member</th><th>Due</th><th>Status</th><th>Fine</th><th></th></tr></thead>
              <tbody>
                <tr *ngFor="let loan of loans">
                  <td>
                    <div class="fw-semibold">{{ loan.bookTitle }}</div>
                    <small class="text-secondary">{{ loan.bookIsbn }}</small>
                  </td>
                  <td>
                    <div>{{ loan.memberName }}</div>
                    <small class="text-secondary">{{ loan.memberEmail }}</small>
                  </td>
                  <td>{{ loan.dueDate }}</td>
                  <td>
                    <span class="status-pill"
                      [class.bg-primary-subtle]="loan.status === 'ISSUED'"
                      [class.bg-success-subtle]="loan.status === 'RETURNED'"
                      [class.bg-danger-subtle]="loan.status === 'OVERDUE'">{{ loan.status }}</span>
                  </td>
                  <td>
                    <div>{{ loan.fineAmount | currency:'INR':'symbol':'1.2-2' }}</div>
                    <small class="text-secondary" *ngIf="loan.overdueDays">{{ loan.overdueDays }} days</small>
                  </td>
                  <td class="text-end">
                    <button class="btn btn-sm btn-outline-success" type="button" (click)="returnLoan(loan)" [disabled]="loan.status === 'RETURNED'">Return</button>
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
export class LoansComponent implements OnInit {
  books: Book[] = [];
  members: Member[] = [];
  loans: Loan[] = [];
  memberFilter = 0;
  bookFilter = 0;
  loading = false;
  error = '';
  success = '';

  form = this.fb.nonNullable.group({
    bookId: [0, [Validators.required, Validators.min(1)]],
    memberId: [0, [Validators.required, Validators.min(1)]],
    dueDate: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly booksService: BookService,
    private readonly membersService: MemberService,
    private readonly loansService: LoanService) {}

  get activeMembers(): Member[] {
    return this.members.filter((member) => member.status === 'ACTIVE');
  }

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.booksService.list().subscribe({ next: (page) => this.books = page.content, error: (err) => this.fail(err) });
    this.membersService.list().subscribe({ next: (page) => this.members = page.content, error: (err) => this.fail(err) });
    this.loadHistory();
  }

  loadHistory(): void {
    this.loading = true;
    this.loansService.history(this.memberFilter || undefined, this.bookFilter || undefined).subscribe({
      next: (page) => {
        this.loans = page.content;
        this.loading = false;
      },
      error: (err) => this.fail(err)
    });
  }

  issue(): void {
    this.error = '';
    this.success = '';
    const value = this.form.getRawValue();
    const payload: IssuePayload = {
      bookId: value.bookId,
      memberId: value.memberId,
      dueDate: value.dueDate || undefined
    };
    this.loansService.issue(payload).subscribe({
      next: () => {
        this.success = 'Book issued.';
        this.form.reset({ bookId: 0, memberId: 0, dueDate: '' });
        this.loadAll();
      },
      error: (err) => this.fail(err)
    });
  }

  returnLoan(loan: Loan): void {
    this.loansService.returnLoan(loan.id).subscribe({
      next: () => {
        this.success = 'Book returned.';
        this.loadAll();
      },
      error: (err) => this.fail(err)
    });
  }

  private fail(err: unknown): void {
    this.error = apiMessage(err);
    this.loading = false;
  }
}
