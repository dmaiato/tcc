# Angular 21 Study Notes

## What is Angular?

Angular is a web framework maintained by Google that empowers developers to build fast, reliable applications. Key features include:

- **Components** - Building blocks for UI
- **Signals** - Fine-grained reactivity model
- **Server-side rendering (SSR)** - With full DOM hydration
- **Dependency injection** - For code reuse
- **Angular Routing** - With lazy-loading, guards, data resolution
- **Forms** - Standardized form validation
- **HTTP Client** - For API calls

## Installation

```bash
npm install -g @angular/cli
ng new my-app
```

## Project Structure

```
src/
  app/
    app.component.ts      # Root component
    app.component.html
    app.component.css
    app.config.ts        # App configuration
    app.routes.ts        # Routing configuration
  main.ts                # Bootstrap entry
  index.html
  styles.css             # Global styles
```

## Components

### Defining a Component

```typescript
// user-profile.ts
@Component({
  selector: 'app-user-profile',       // HTML selector
  templateUrl: 'user-profile.html',  // Template file
  styleUrl: 'user-profile.css',      // Styles file
  standalone: true,                  // Standalone component (default in v17+)
})
export class UserProfile {
  // Component logic
}
```

### Inline Template/Styles

```typescript
@Component({
  selector: 'app-user-profile',
  template: `
    <h1>User profile</h1>
    <p>{{ userName() }}</p>
  `,
  styles: `h1 { font-size: 2em; }`,
})
export class UserProfile {
  userName = signal('Default User');
}
```

### Component Inputs/Outputs

```typescript
import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-user-card',
  template: `
    <h1>{{ userName }}</h1>
    <button (click)="onSelect()">Select</button>
  `,
  standalone: true,
})
export class UserCard {
  @Input() userName = '';
  @Output() selected = new EventEmitter<string>();

  onSelect() {
    this.selected.emit(this.userName);
  }
}
```

### Using Child Components

```typescript
@Component({
  selector: 'app-parent',
  imports: [UserCard],  // Import child components
  template: `
    <app-user-card
      [userName]="name()"
      (selected)="onUserSelected($event)"
    />
  `,
})
export class ParentComponent {
  name = signal('Alice');

  onUserSelected(user: string) {
    console.log('Selected:', user);
  }
}
```

## Signals

Signals are Angular's fine-grained reactivity system.

### Creating Signals

```typescript
import { signal, computed } from '@angular/core';

// Writable signal
const count = signal(0);

// Read signal value (signals are functions)
console.log(count()); // 0

// Update signal
count.set(10);
count.update(c => c + 1);
```

### Computed Signals

```typescript
const count = signal(0);
const doubleCount = computed(() => count() * 2);

// Auto-updates when count changes
count.set(5);
console.log(doubleCount()); // 10
```

### Signals in Components

```typescript
@Component({...})
export class CounterComponent {
  count = signal(0);
  doubleCount = computed(() => this.count() * 2);

  increment() {
    this.count.update(c => c + 1);
  }
}
```

### Effects (Side Effects)

```typescript
import { effect } from '@angular/core';

@Component({...})
export class MyComponent {
  private logEffect = effect(() => {
    // Runs whenever tracked signals change
    console.log('Count changed:', this.count());
  });
}
```

### signal() vs computed() vs effect()

| Function | Description | Use Case |
|----------|-------------|----------|
| `signal(initial)` | Writable state | Local component state |
| `computed(fn)` | Derived read-only state | Derived values |
| `effect(fn)` | Side effect observer | Logging, sync, external state |

## Templates

### Text Interpolation

```html
<h1>Hello, {{ userName() }}!</h1>
```

### Property Binding

```html
<button [disabled]="!isValid()">Submit</button>
<img [src]="imageUrl()" />
```

### Attribute Binding

```html
<ul [attr.role]="listRole"></ul>
```

### Event Binding

```html
<button (click)="onSubmit()">Click me</button>
<form (submit)="onSubmit($event)">...</form>
```

### Control Flow (New in Angular 17+)

```html
@if (isAdmin) {
  <p>Admin panel</p>
} @else if (isUser) {
  <p>User panel</p>
} @else {
  <p>Please log in</p>
}
```

```html
@for (user of users(); track user.id) {
  <li>{{ user.name }}</li>
} @empty {
  <li>No users found</li>
}
```

### @switch

```html
@switch (status()) {
  @case ('active') { <p>Active</p> }
  @case ('inactive') { <p>Inactive</p> }
  @default { <p>Unknown</p> }
}
```

### New @let (Angular 18+)

```html
@let greeting = 'Hello, ' + name();
<p>{{ greeting }}</p>
```

### Template Variables

```html
<form #myForm="ngForm">
  <button (click)="myForm.reset()">Reset</button>
</form>
```

## Services & Dependency Injection

### Creating a Service

```typescript
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })  // Singleton app-wide
export class UserService {
  private users = signal<User[]>([]);

  getUsers() {
    return this.users;
  }

  addUser(user: User) {
    this.users.update(users => [...users, user]);
  }
}
```

### Injecting a Service

```typescript
import { inject } from '@angular/core';

@Component({...})
export class MyComponent {
  private userService = inject(UserService);
}
```

## HTTP Client

### Setup

```typescript
import { provideHttpClient } from '@angular/common/http';

export const appConfig: ApplicationConfig = {
  providers: [provideHttpClient()],
};
```

### Making Requests

```typescript
import { HttpClient, inject } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);

  getUsers() {
    return this.http.get<User[]>('/api/users');
  }

  createUser(user: User) {
    return this.http.post<User>('/api/users', user);
  }
}
```

## Routing

### Route Configuration

```typescript
// app.routes.ts
import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  {
    path: 'users',
    loadComponent: () => import('./users/users.component').then(m => m.UsersComponent),
  },
  {
    path: 'user/:id',
    loadComponent: () => import('./user-detail/user-detail.component').then(m => m.UserDetailComponent),
  },
  { path: '**', component: NotFoundComponent },
];
```

### Using Router

```typescript
import { Router, RouterLink, RouterOutlet } from '@angular/router';

@Component({
  imports: [RouterOutlet, RouterLink],
  template: `
    <nav>
      <a routerLink="/home">Home</a>
      <a routerLink="/users">Users</a>
    </nav>
    <router-outlet />
  `,
})
export class AppComponent {
  private router = inject(Router);
}
```

### Route Parameters

```typescript
import { ActivatedRoute } from '@angular/router';

@Component({...})
export class UserDetailComponent {
  private route = inject(ActivatedRoute);
  userId = this.route.snapshot.paramMap.get('id');
}
```

### Route Guards

```typescript
// auth.guard.ts
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  return router.createUrlTree(['/login']);
};

// routes.ts
{ path: 'dashboard', canActivate: [authGuard], component: DashboardComponent }
```

## Standalone Components

Angular 17+ uses standalone components by default. No NgModule needed.

```typescript
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './app.component.html',
})
export class AppComponent {}
```

## Forms

### Reactive Forms (Recommended)

```typescript
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';

@Component({
  imports: [ReactiveFormsModule],
  template: `
    <form [formGroup]="form" (ngSubmit)="onSubmit()">
      <input formControlName="name" />
      <span *ngIf="form.get('name')?.errors?.['required']">Required</span>
      <button type="submit" [disabled]="form.invalid">Submit</button>
    </form>
  `,
})
export class MyFormComponent {
  private fb = inject(FormBuilder);
  form = this.fb.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
  });

  onSubmit() {
    console.log(this.form.value);
  }
}
```

### Signal Forms (Angular 18+)

```typescript
import { signalFormGroup } from '@angular/forms';

@Component({
  template: `
    <form [formGroup]="form">
      <input formControlName="name" />
    </form>
  `,
})
export class MyFormComponent {
  form = signalFormGroup({
    name: ['', Validators.required],
  });
}
```

### Template-Driven Forms

```typescript
import { FormsModule } from '@angular/forms';

@Component({
  imports: [FormsModule],
  template: `
    <input [(ngModel)]="name" name="name" />
  `,
})
export class MyComponent {
  name = '';
}
```

## Application Configuration

```typescript
// app.config.ts
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    provideAnimations(),
  ],
};
```

## Bootstrap

```typescript
// main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';

bootstrapApplication(AppComponent, appConfig).catch(err => console.error(err));
```

## New Control Flow Syntax (Angular 17+)

| Old Syntax | New Syntax |
|-----------|-----------|
| `*ngIf` | `@if` |
| `*ngFor` | `@for` |
| `ngSwitch` | `@switch` / `@case` |
| `*ngIf...else` | `@if...@else` |

```typescript
// Old
<div *ngIf="condition; else elseBlock">...</div>
<ng-template #elseBlock>...</ng-template>

// New
@if (condition) {
  <div>...</div>
} @else {
  <div>...</div>
}
```

## Signal Inputs (Angular 17.1+)

```typescript
@Component({...})
export class UserCard {
  // Required input
  @Input() id = input.required<string>();

  // Optional with default
  @Input() name = input<string>('');

  // With transformation
  @Input() active = input<boolean, string>(false, {
    transform: (value: boolean | string) =>
      typeof value === 'string' ? value : value ? 'yes' : 'no',
  });
}
```

## Signal Outputs (Angular 17.1+)

```typescript
@Component({...})
export class UserCard {
  @Output() selected = new EventEmitter<string>();

  // Can also use signal-based outputs
  @Output() clicked = output<string>();
}
```

## Lifecycle Hooks

```typescript
import {
  OnInit,
  OnDestroy,
  AfterViewInit,
  AfterContentInit,
  DoCheck,
  OnChanges,
  SimpleChanges,
} from '@angular/core';

@Component({...})
export class MyComponent implements OnInit, OnDestroy {
  ngOnInit() {
    // Called after constructor, before template
  }

  ngOnDestroy() {
    // Called when component is destroyed
  }
}
```

## Common Patterns

### Loading State with Signals

```typescript
@Component({
  template: `
    @if (loading()) {
      <p>Loading...</p>
    } @else {
      <p>{{ data() }}</p>
    }
  `,
})
export class MyComponent {
  loading = signal(true);
  data = signal('');

  async ngOnInit() {
    await loadData();
    this.loading.set(false);
  }
}
```

### Async Pipe with Signals

```typescript
@Component({
  template: `
    @if (users(); as users) {
      @for (user of users; track user.id) {
        <p>{{ user.name }}</p>
      }
    }
  `,
})
export class MyComponent {
  users = toSignal(this.api.getUsers());
}
```

### Error Handling

```typescript
@Component({...})
export class MyComponent {
  error = signal<Error | null>(null);

  async loadData() {
    try {
      const data = await this.api.getData();
    } catch (e) {
      this.error.set(e as Error);
    }
  }
}
```

## Best Practices

1. **Use standalone components** - Default in Angular 17+
2. **Prefer Signals over RxJS** for local component state
3. **Use new control flow** (`@if`, `@for`) over `*ngIf`, `*ngFor`
4. **Use `track` in `@for` blocks** for performance
5. **Lazy load routes** with `loadComponent`
6. **Use `provideHttpClient()`** for HTTP setup
7. **Use `signal()` for component state**, services for shared state
8. **Avoid complex computations in templates**, use `computed()`

## JWT Authentication

### Auth Service (Store Token & User)

```typescript
import { Injectable, signal, computed, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

export interface User {
  id: string;
  email: string;
  name: string;
  roles: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  // Signals for reactive state
  private _user = signal<User | null>(null);
  private _token = signal<string | null>(null);

  // Public computed signals
  readonly user = this._user.asReadonly();
  readonly token = this._token.asReadonly();
  readonly isLoggedIn = computed(() => !!this._token());
  readonly isAdmin = computed(() => this._user()?.roles.includes('admin') ?? false);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      this.loadFromStorage();
    }
  }

  private loadFromStorage() {
    const token = localStorage.getItem('accessToken');
    const userStr = localStorage.getItem('user');
    if (token && userStr) {
      this._token.set(token);
      this._user.set(JSON.parse(userStr));
    }
  }

  private saveToStorage(token: string, user: User) {
    localStorage.setItem('accessToken', token);
    localStorage.setItem('user', JSON.stringify(user));
  }

  private clearStorage() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  }

  async login(email: string, password: string): Promise<void> {
    const response = await this.http.post<AuthResponse>('/api/auth/login', {
      email,
      password,
    }).toPromise();

    if (response) {
      this._token.set(response.accessToken);
      this._user.set(response.user);
      this.saveToStorage(response.accessToken, response.user);
    }
  }

  async register(email: string, password: string, name: string): Promise<void> {
    const response = await this.http.post<AuthResponse>('/api/auth/register', {
      email,
      password,
      name,
    }).toPromise();

    if (response) {
      this._token.set(response.accessToken);
      this._user.set(response.user);
      this.saveToStorage(response.accessToken, response.user);
    }
  }

  async logout(): Promise<void> {
    try {
      await this.http.post('/api/auth/logout', {}).toPromise();
    } catch {
      // Ignore errors on logout
    } finally {
      this._token.set(null);
      this._user.set(null);
      this.clearStorage();
      this.router.navigate(['/login']);
    }
  }

  async refreshToken(): Promise<boolean> {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) return false;

    try {
      const response = await this.http.post<{ accessToken: string }>('/api/auth/refresh', {
        refreshToken,
      }).toPromise();

      if (response) {
        this._token.set(response.accessToken);
        localStorage.setItem('accessToken', response.accessToken);
        return true;
      }
    } catch {
      this.logout();
    }
    return false;
  }
}
```

### Auth Interceptor (Attach Token to Requests)

```typescript
import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const authService = inject(AuthService);
  const token = authService.token();

  // Skip token for auth endpoints
  if (req.url.includes('/api/auth/')) {
    return next(req);
  }

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Try to refresh token
        return throwError(() => error);
      }
      return throwError(() => error);
    })
  );
};
```

### Auth Error Interceptor (Handle 401)

```typescript
import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

let isRefreshing = false;

export const authErrorInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/api/auth/') && !isRefreshing) {
        isRefreshing = true;

        return authService.refreshToken().pipe(
          switchMap((success) => {
            isRefreshing = false;
            if (success) {
              const token = authService.token();
              const newReq = req.clone({
                setHeaders: {
                  Authorization: `Bearer ${token}`,
                },
              });
              return next(newReq);
            }
            return throwError(() => error);
          }),
          catchError((refreshError) => {
            isRefreshing = false;
            authService.logout();
            return throwError(() => refreshError);
          })
        );
      }
      return throwError(() => error);
    })
  );
};
```

### Auth Guard (Protect Routes)

```typescript
import { inject } from '@angular/core';
import { Router, CanActivateFn, activatedRouteSnapshot } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route: activatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login'], {
      queryParams: { returnUrl: route.url.join('/') },
    });
  }

  // Check for required roles
  const requiredRoles = route.data['roles'] as string[];
  if (requiredRoles && requiredRoles.length > 0) {
    const user = authService.user();
    const hasRole = requiredRoles.some((role) => user?.roles.includes(role));
    if (!hasRole) {
      return router.createUrlTree(['/unauthorized']);
    }
  }

  return true;
};

// Shorthand for protected routes
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return router.createUrlTree(['/dashboard']);
  }
  return true;
};
```

### Login Component

```typescript
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="login-container">
      <h1>Login</h1>
      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label for="email">Email</label>
          <input id="email" type="email" formControlName="email" />
          @if (form.get('email')?.touched && form.get('email')?.errors) {
            <span class="error">Valid email required</span>
          }
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input id="password" type="password" formControlName="password" />
          @if (form.get('password')?.touched && form.get('password')?.errors?.['required']) {
            <span class="error">Password required</span>
          }
        </div>
        @if (error()) {
          <span class="error">{{ error() }}</span>
        }
        <button type="submit" [disabled]="form.invalid || loading()">
          @if (loading()) {
            Loading...
          } @else {
            Login
          }
        </button>
      </form>
      <p>Don't have an account? <a routerLink="/register">Register</a></p>
    </div>
  `,
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loading = signal(false);
  error = signal<string | null>(null);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  async onSubmit() {
    if (this.form.invalid) return;

    this.loading.set(true);
    this.error.set(null);

    try {
      await this.authService.login(
        this.form.value.email!,
        this.form.value.password!
      );
      this.router.navigate(['/dashboard']);
    } catch (err) {
      this.error.set('Invalid email or password');
    } finally {
      this.loading.set(false);
    }
  }
}
```

### App Configuration with Interceptors

```typescript
// app.config.ts
import { ApplicationConfig } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

import { routes } from './app.routes';
import { authInterceptor } from './auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(
      withInterceptors([authInterceptor])
    ),
    provideAnimations(),
  ],
};
```

### Route Configuration with Auth

```typescript
// app.routes.ts
import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './auth.guard';

export const routes: Routes = [
  // Guest-only routes (already logged in cannot access)
  { path: 'login', canActivate: [guestGuard], loadComponent: () => import('./login/login.component').then(m => m.LoginComponent) },
  { path: 'register', canActivate: [guestGuard], loadComponent: () => import('./register/register.component').then(m => m.RegisterComponent) },

  // Protected routes
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent),
  },
  {
    path: 'admin',
    canActivate: [authGuard],
    data: { roles: ['admin'] },
    loadComponent: () => import('./admin/admin.component').then(m => m.AdminComponent),
  },
];
```

### JWT Best Practices

1. **Store token securely**:
   - Use `localStorage` for access token (needed for API calls)
   - Use `httpOnly` cookies for refresh token (more secure)
   - Never store sensitive data in localStorage

2. **Token expiration**:
   - Access token: 15-60 minutes
   - Refresh token: 7-30 days

3. **Always use HTTPS** in production

4. **Implement token refresh** before access token expires:
   - Refresh when token is 5 minutes from expiring
   - Handle refresh failures gracefully

5. **Use HttpInterceptors** for:
   - Attaching token to requests
   - Handling 401 errors
   - Centralizing auth logic

6. **Use route guards** to protect:
   - Authenticated routes
   - Role-based access

7. **Clear tokens on**:
   - Logout
   - Token expiration
   - Refresh failure

8. **Never expose tokens** in URLs or logs

## CLI Commands

```bash
ng new my-app              # Create new app
ng generate component foo    # Generate component
ng generate service foo   # Generate service
ng generate guard foo      # Generate guard
ng generate resolver foo # Generate resolver
ng generate pipe foo    # Generate pipe
ng build                 # Build for production
ng serve                  # Development server
ng test                   # Run tests
ng lint                   # Run linter
ng update                 # Update Angular version
```

## Resources

- Official Docs: https://angular.dev/
- API Reference: https://api.angular.dev/
- Tutorial: https://angular.dev/tutorials/learn-angular