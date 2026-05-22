import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiPage } from '../models/api-page';
import { Member, MemberPayload } from '../models/member';

@Injectable({ providedIn: 'root' })
export class MemberService {
  private readonly baseUrl = `${environment.apiUrl}/members`;

  constructor(private readonly http: HttpClient) {}

  list(search = ''): Observable<ApiPage<Member>> {
    let params = new HttpParams().set('size', 100).set('sort', 'name,asc');
    if (search.trim()) {
      params = params.set('search', search.trim());
    }
    return this.http.get<ApiPage<Member>>(this.baseUrl, { params });
  }

  create(payload: MemberPayload): Observable<Member> {
    return this.http.post<Member>(this.baseUrl, payload);
  }

  update(id: number, payload: MemberPayload): Observable<Member> {
    return this.http.put<Member>(`${this.baseUrl}/${id}`, payload);
  }

  deactivate(id: number): Observable<Member> {
    return this.http.patch<Member>(`${this.baseUrl}/${id}/deactivate`, {});
  }
}
