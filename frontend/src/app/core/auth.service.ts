import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthTokenResponse, CreateUserPayload, LoginPayload, User } from '../models/auth';

const STORAGE_KEY = 'library-core-auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = `${environment.apiUrl}/auth`;
  private readonly session = signal<AuthTokenResponse | null>(this.readSession());

  readonly user = computed(() => this.session());
  readonly isAuthenticated = computed(() => {
    const session = this.session();
    return !!session && new Date(session.expiresAt).getTime() > Date.now();
  });
  readonly isAdmin = computed(() => this.session()?.role === 'ADMIN');

  constructor(private readonly http: HttpClient) {}

  token(): string | null {
    return this.isAuthenticated() ? this.session()?.accessToken ?? null : null;
  }

  login(payload: LoginPayload): Observable<AuthTokenResponse> {
    return this.http.post<AuthTokenResponse>(`${this.baseUrl}/login`, payload).pipe(
      tap((session) => this.saveSession(session))
    );
  }

  bootstrap(payload: CreateUserPayload): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/bootstrap`, payload);
  }

  createUser(payload: CreateUserPayload): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/users`, payload);
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.session.set(null);
  }

  private saveSession(session: AuthTokenResponse): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    this.session.set(session);
  }

  private readSession(): AuthTokenResponse | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    try {
      const session = JSON.parse(raw) as AuthTokenResponse;
      if (new Date(session.expiresAt).getTime() <= Date.now()) {
        localStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return session;
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }
}
