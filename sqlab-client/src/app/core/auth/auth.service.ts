import { Injectable, inject, signal, computed, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of, map, switchMap } from 'rxjs';
import { ApiService } from '../api.service';
import { User, UserResponse } from '../models/user.model';
import { LoginRequest, RegisterRequest, AuthResponse, RefreshRequest, AuthResponseWithUser } from '../models/auth-response.model';

const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const USER_KEY = 'user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);

  private _user = signal<User | null>(null);
  private _token = signal<string | null>(null);
  private _refreshToken = signal<string | null>(null);

  readonly currentUser = this._user.asReadonly();
  readonly currentToken = this._token.asReadonly();
  readonly isLoggedIn = computed(() => !!this._token());
  readonly isLoading = signal(false);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      this.loadFromStorage();
    }
  }

  private loadFromStorage(): void {
    const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
    const userStr = localStorage.getItem(USER_KEY);

    if (accessToken && userStr) {
      this._token.set(accessToken);
      this._refreshToken.set(refreshToken);
      try {
        this._user.set(JSON.parse(userStr));
      } catch {
        this.clearStorage();
      }
    }
  }

  private saveToStorage(accessToken: string, refreshToken: string, user: User): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    if (refreshToken) {
      localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    }
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  private clearStorage(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  login(credentials: LoginRequest): Observable<User> {
    this.isLoading.set(true);

    return this.api.post<AuthResponseWithUser>('/auth/login', credentials).pipe(
      tap((response: AuthResponseWithUser) => {
        const user: User = {
          id: response.id,
          email: response.email,
          username: response.username,
          createdAt: '',
          xp: 0,
          level: 1
        };
        this._token.set(response.token);
        this._refreshToken.set('');
        this._user.set(user);
        this.saveToStorage(response.token, '', user);
      }),
      tap(() => {
        this.isLoading.set(false);
        this.router.navigate(['/dashboard']);
      }),
      switchMap(() => of(this._user() as User)),
      catchError(error => {
        this.isLoading.set(false);
        throw error;
      })
    );
  }

  register(data: RegisterRequest): Observable<User> {
    this.isLoading.set(true);

    return this.api.post<AuthResponseWithUser>('/auth/register', data).pipe(
      tap((response: AuthResponseWithUser) => {
        const user: User = {
          id: response.id,
          email: response.email,
          username: response.username,
          createdAt: '',
          xp: 0,
          level: 1
        };
        this._token.set(response.token);
        this._refreshToken.set('');
        this._user.set(user);
        this.saveToStorage(response.token, '', user);
      }),
      tap(() => {
        this.isLoading.set(false);
        this.router.navigate(['/dashboard']);
      }),
      switchMap(() => of(this._user() as User)),
      catchError(error => {
        this.isLoading.set(false);
        throw error;
      })
    );
  }

  logout(): void {
    const refreshToken = this._refreshToken();

    if (refreshToken) {
      this.api.post('/auth/logout', { refreshToken } as RefreshRequest).pipe(
        catchError(() => of(null))
      ).subscribe();
    }

    this._token.set(null);
    this._user.set(null);
    this._refreshToken.set(null);
    this.clearStorage();
    this.router.navigate(['/login']);
  }

  refreshToken(): Observable<boolean> {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      return of(false);
    }

    return this.api.post<AuthResponse>('/auth/refresh', { refreshToken } as RefreshRequest).pipe(
      tap(response => {
        this._token.set(response.accessToken);
        localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
      }),
      map(() => true),
      catchError(() => {
        this.logout();
        return of(false);
      })
    );
  }

  getToken(): string | null {
    return this._token();
  }

  fetchUserProfile(): Observable<User> {
    return this.api.get<UserResponse>('/users/me').pipe(
      tap(user => {
        this._user.set(user);
        const currentUser = this._user();
        if (currentUser) {
          localStorage.setItem(USER_KEY, JSON.stringify(user));
        }
      })
    );
  }

  user(): { (): User | null } {
    return this.currentUser;
  }
}