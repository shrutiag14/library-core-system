import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiPage } from '../models/api-page';
import { Book, BookPayload } from '../models/book';

@Injectable({ providedIn: 'root' })
export class BookService {
  private readonly baseUrl = `${environment.apiUrl}/books`;

  constructor(private readonly http: HttpClient) {}

  list(search = ''): Observable<ApiPage<Book>> {
    let params = new HttpParams().set('size', 100).set('sort', 'title,asc');
    if (search.trim()) {
      params = params.set('search', search.trim());
    }
    return this.http.get<ApiPage<Book>>(this.baseUrl, { params });
  }

  create(payload: BookPayload): Observable<Book> {
    return this.http.post<Book>(this.baseUrl, payload);
  }

  update(id: number, payload: BookPayload): Observable<Book> {
    return this.http.put<Book>(`${this.baseUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
