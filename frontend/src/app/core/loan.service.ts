import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiPage } from '../models/api-page';
import { Dashboard, IssuePayload, Loan } from '../models/loan';

@Injectable({ providedIn: 'root' })
export class LoanService {
  private readonly baseUrl = `${environment.apiUrl}`;

  constructor(private readonly http: HttpClient) {}

  dashboard(): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${this.baseUrl}/dashboard`);
  }

  history(memberId?: number, bookId?: number): Observable<ApiPage<Loan>> {
    let params = new HttpParams().set('size', 100).set('sort', 'issuedAt,desc');
    if (memberId) params = params.set('memberId', memberId);
    if (bookId) params = params.set('bookId', bookId);
    return this.http.get<ApiPage<Loan>>(`${this.baseUrl}/loans`, { params });
  }

  issue(payload: IssuePayload): Observable<Loan> {
    return this.http.post<Loan>(`${this.baseUrl}/loans`, payload);
  }

  returnLoan(id: number): Observable<Loan> {
    return this.http.patch<Loan>(`${this.baseUrl}/loans/${id}/return`, {});
  }
}
