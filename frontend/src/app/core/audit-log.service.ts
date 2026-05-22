import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiPage } from '../models/api-page';
import { AuditLog } from '../models/audit-log';

@Injectable({ providedIn: 'root' })
export class AuditLogService {
  private readonly baseUrl = `${environment.apiUrl}/audit-logs`;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<ApiPage<AuditLog>> {
    const params = new HttpParams().set('size', 100).set('sort', 'createdAt,desc');
    return this.http.get<ApiPage<AuditLog>>(this.baseUrl, { params });
  }
}
