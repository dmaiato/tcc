import { Injectable, inject, signal, computed, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of, map, switchMap } from 'rxjs';
import { ApiService } from '../api.service';
import { User, UserResponse } from '../models/user.model';
import { LoginRequest, RegisterRequest, AuthResponseWithUser } from '../models/auth-response.model';

const ACCESS_TOKEN_KEY = 'accessToken';
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
    const userStr = localStorage.getItem(USER_KEY);

    if (accessToken && userStr) {
      this._token.set(accessToken);
      try {
        this._user.set(JSON.parse(userStr));
      } catch {
        this.clearStorage();
      }
    }
  }

  readonly isAdmin = computed(() => this._user()?.role === 'ADMIN');

  private saveToStorage(accessToken: string, user: User): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  private clearStorage(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
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
          level: 1,
          role: response.role as 'USER' | 'ADMIN'
        };
        this._token.set(response.token);
        this._user.set(user);
        this.saveToStorage(response.token, user);
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
          level: 1,
          role: response.role as 'USER' | 'ADMIN'
        };
        this._token.set(response.token);
        this._user.set(user);
        this.saveToStorage(response.token, user);
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
    this._token.set(null);
    this._user.set(null);
    this.clearStorage();
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this._token();
  }

  fetchUserProfile(): Observable<User> {
    return this.api.get<UserResponse>('/users/me').pipe(
      map(response => this.syncFromResponse(response))
    );
  }

  syncFromResponse(response: UserResponse): User {
    const user: User = {
      id: response.id,
      email: response.email,
      username: response.username,
      createdAt: response.createdAt,
      xp: response.xp,
      level: response.level,
      role: response.role as 'USER' | 'ADMIN'
    };
    this._user.set(user);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    return user;
  }

}