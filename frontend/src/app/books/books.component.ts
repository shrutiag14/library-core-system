import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { BookService } from '../core/book.service';
import { apiMessage } from '../core/api-error';
import { Book, BookPayload } from '../models/book';

@Component({
  selector: 'app-books',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="toolbar">
      <div>
        <h1 class="h3 mb-1">Books</h1>
        <p class="text-secondary mb-0">Add, edit, search, and retire catalogue records.</p>
      </div>
      <button class="btn btn-outline-primary" type="button" (click)="load()">Refresh</button>
    </div>

    <div class="row g-3">
      <div class="col-lg-4">
        <form class="panel" [formGroup]="form" (ngSubmit)="save()">
          <h2 class="h5">{{ editing ? 'Edit book' : 'Add book' }}</h2>
          <div class="mb-2">
            <label class="form-label">Title</label>
            <input class="form-control" formControlName="title">
          </div>
          <div class="mb-2">
            <label class="form-label">Author</label>
            <input class="form-control" formControlName="author">
          </div>
          <div class="mb-2">
            <label class="form-label">ISBN</label>
            <input class="form-control" formControlName="isbn">
          </div>
          <div class="mb-2">
            <label class="form-label">Topic</label>
            <input class="form-control" formControlName="category">
          </div>
          <div class="row">
            <div class="col-6 mb-2">
              <label class="form-label">Total</label>
              <input class="form-control" type="number" formControlName="totalCopies">
            </div>
            <div class="col-6 mb-2">
              <label class="form-label">Available</label>
              <input class="form-control" type="number" formControlName="availableCopies">
            </div>
          </div>
          <div class="mb-3">
            <label class="form-label">Shelf</label>
            <input class="form-control" formControlName="shelfLocation">
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
              placeholder="Search title, author, ISBN, topic"
              [(ngModel)]="searchText"
              (ngModelChange)="onSearchChange($event)">
          </div>
          <div *ngIf="loading" class="alert alert-info">Loading books...</div>
          <div class="table-responsive">
            <table class="table table-hover">
              <thead>
                <tr>
                  <th>Title</th><th>Author</th><th>ISBN</th><th>Copies</th><th></th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let book of books">
                  <td>
                    <div class="fw-semibold">{{ book.title }}</div>
                    <small class="text-secondary">{{ book.category || 'Uncategorised' }} · {{ book.shelfLocation || 'No shelf' }}</small>
                  </td>
                  <td>{{ book.author }}</td>
                  <td>{{ book.isbn }}</td>
                  <td>{{ book.availableCopies }} / {{ book.totalCopies }}</td>
                  <td class="text-end">
                    <button class="btn btn-sm btn-outline-secondary me-1" type="button" (click)="edit(book)">Edit</button>
                    <button class="btn btn-sm btn-outline-danger" type="button" (click)="remove(book)">Delete</button>
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
export class BooksComponent implements OnInit, OnDestroy {
  books: Book[] = [];
  editing?: Book;
  loading = false;
  error = '';
  success = '';
  searchText = '';
  private readonly searchChanges = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    author: ['', Validators.required],
    isbn: ['', Validators.required],
    category: [''],
    totalCopies: [0, [Validators.required, Validators.min(0)]],
    availableCopies: [0, [Validators.required, Validators.min(0)]],
    shelfLocation: ['']
  });

  constructor(private readonly fb: FormBuilder, private readonly service: BookService) {}

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
        this.books = page.content;
        this.loading = false;
      },
      error: (err) => this.fail(err)
    });
  }

  save(): void {
    this.error = '';
    this.success = '';
    const payload = this.form.getRawValue() as BookPayload;
    const request = this.editing ? this.service.update(this.editing.id, payload) : this.service.create(payload);
    request.subscribe({
      next: () => {
        this.success = 'Book saved.';
        this.reset();
        this.load();
      },
      error: (err) => this.fail(err)
    });
  }

  edit(book: Book): void {
    this.editing = book;
    this.form.patchValue(book);
  }

  remove(book: Book): void {
    this.service.delete(book.id).subscribe({
      next: () => {
        this.success = 'Book deleted.';
        this.load();
      },
      error: (err) => this.fail(err)
    });
  }

  reset(): void {
    this.editing = undefined;
    this.form.reset({ title: '', author: '', isbn: '', category: '', totalCopies: 0, availableCopies: 0, shelfLocation: '' });
  }

  private fail(err: unknown): void {
    this.error = apiMessage(err);
    this.loading = false;
  }
}
