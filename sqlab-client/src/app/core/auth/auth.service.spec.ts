import { TestBed } from '@angular/core/testing';
import { PLATFORM_ID } from '@angular/core';
import { of, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';
import { AuthResponseWithUser } from '../models/auth-response.model';
import { User } from '../models/user.model';

function mockLocalStorage(store: Record<string, string>) {
  Object.defineProperty(window, 'localStorage', {
    value: {
      getItem: vi.fn((key: string) => store[key] ?? null),
      setItem: vi.fn((key: string, value: string) => { store[key] = value; }),
      removeItem: vi.fn((key: string) => { delete store[key]; }),
      clear: vi.fn(() => { Object.keys(store).forEach(k => delete store[k]); }),
      length: 0,
      key: vi.fn(),
    },
    writable: true,
    configurable: true,
  });
}

describe('AuthService', () => {
  let store: Record<string, string> = {};

  const mockApi = {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  };

  const mockRouter = {
    navigate: vi.fn(),
  };

  function createService(): AuthService {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        { provide: ApiService, useValue: mockApi },
        { provide: Router, useValue: mockRouter },
      ],
    });
    return TestBed.inject(AuthService);
  }

  beforeEach(() => {
    store = {};
    vi.clearAllMocks();
    mockLocalStorage(store);
  });

  describe('constructor', () => {
    it('deve carregar dados do localStorage quando browser platform', () => {
      store['accessToken'] = 'token123';
      store['user'] = JSON.stringify({ id: '1', email: 'a@b.com', username: 'u', role: 'USER' });
      const service = createService();
      expect(service.currentToken()).toBe('token123');
      expect(service.currentUser()).toBeTruthy();
    });

    it('deve NÃO carregar dados do localStorage quando server platform', () => {
      store['accessToken'] = 'token123';
      store['user'] = JSON.stringify({ id: '1', email: 'a@b.com', username: 'u', role: 'USER' });
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          { provide: ApiService, useValue: mockApi },
          { provide: Router, useValue: mockRouter },
          { provide: PLATFORM_ID, useValue: 'server' },
        ],
      });
      const service = TestBed.inject(AuthService);
      expect(service.currentToken()).toBeNull();
      expect(service.currentUser()).toBeNull();
    });
  });

  describe('login', () => {
    const credentials = { email: 'test@test.com', password: 'password' };
    const mockResponse: AuthResponseWithUser = {
      token: 'jwt-token',
      id: '1',
      email: 'test@test.com',
      username: 'testuser',
      role: 'USER',
    };

    it('deve chamar api.post(/auth/login, credentials)', () => {
      mockApi.post.mockReturnValue(of(mockResponse));
      const service = createService();
      service.login(credentials).subscribe();
      expect(mockApi.post).toHaveBeenCalledWith('/auth/login', credentials);
    });

    it('deve armazenar token e user no signal e localStorage', () => {
      mockApi.post.mockReturnValue(of(mockResponse));
      const service = createService();
      service.login(credentials).subscribe(() => {
        expect(service.currentToken()).toBe('jwt-token');
        expect(service.currentUser()?.username).toBe('testuser');
        expect(store['accessToken']).toBe('jwt-token');
        expect(JSON.parse(store['user']).username).toBe('testuser');
      });
    });

    it('deve navegar para /dashboard', () => {
      mockApi.post.mockReturnValue(of(mockResponse));
      const service = createService();
      service.login(credentials).subscribe(() => {
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
      });
    });

    it('deve retornar User', () => {
      mockApi.post.mockReturnValue(of(mockResponse));
      const service = createService();
      service.login(credentials).subscribe((user: User) => {
        expect(user.email).toBe('test@test.com');
        expect(user.username).toBe('testuser');
      });
    });

    it('deve setar loading=false e propagar erro em caso de erro', () => {
      const testError = new Error('Login failed');
      mockApi.post.mockReturnValue(throwError(() => testError));
      const service = createService();
      service.login(credentials).subscribe({
        error: (err) => {
          expect(service.isLoading()).toBe(false);
          expect(err).toBe(testError);
        },
      });
    });
  });

  describe('register', () => {
    const data = { email: 'new@test.com', password: 'password', username: 'newuser' };
    const mockResponse: AuthResponseWithUser = {
      token: 'jwt-register',
      id: '2',
      email: 'new@test.com',
      username: 'newuser',
      role: 'USER',
    };

    it('deve chamar api.post(/auth/register, data)', () => {
      mockApi.post.mockReturnValue(of(mockResponse));
      const service = createService();
      service.register(data).subscribe();
      expect(mockApi.post).toHaveBeenCalledWith('/auth/register', data);
    });

    it('deve armazenar token e user no signal e localStorage', () => {
      mockApi.post.mockReturnValue(of(mockResponse));
      const service = createService();
      service.register(data).subscribe(() => {
        expect(service.currentToken()).toBe('jwt-register');
        expect(service.currentUser()?.username).toBe('newuser');
        expect(store['accessToken']).toBe('jwt-register');
      });
    });

    it('deve navegar para /dashboard', () => {
      mockApi.post.mockReturnValue(of(mockResponse));
      const service = createService();
      service.register(data).subscribe(() => {
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/dashboard']);
      });
    });
  });

  describe('logout', () => {
    it('deve limpar signals e localStorage', () => {
      store['accessToken'] = 'token';
      store['user'] = JSON.stringify({ id: '1', email: 'a@b.com', username: 'u', role: 'USER' });
      const service = createService();
      service.logout();
      expect(service.currentToken()).toBeNull();
      expect(service.currentUser()).toBeNull();
      expect(store['accessToken']).toBeUndefined();
      expect(store['user']).toBeUndefined();
    });

    it('deve navegar para /login', () => {
      store['accessToken'] = 'token';
      store['user'] = JSON.stringify({ id: '1', email: 'a@b.com', username: 'u', role: 'USER' });
      const service = createService();
      service.logout();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('fetchUserProfile', () => {
    const mockUserResponse = {
      id: '1',
      email: 'test@test.com',
      username: 'testuser',
      createdAt: '2024-01-01',
      xp: 500,
      level: 3,
      role: 'USER',
    };

    it('deve chamar GET /users/me', () => {
      mockApi.get.mockReturnValue(of(mockUserResponse));
      const service = createService();
      service.fetchUserProfile().subscribe();
      expect(mockApi.get).toHaveBeenCalledWith('/users/me');
    });

    it('deve atualizar signal e localStorage com user', () => {
      mockApi.get.mockReturnValue(of(mockUserResponse));
      const service = createService();
      service.fetchUserProfile().subscribe(() => {
        expect(service.currentUser()?.xp).toBe(500);
        expect(service.currentUser()?.level).toBe(3);
        expect(JSON.parse(store['user']).xp).toBe(500);
      });
    });
  });

  describe('computed signals', () => {
    it('isLoggedIn deve ser true quando token presente', () => {
      store['accessToken'] = 'token';
      store['user'] = JSON.stringify({ id: '1', email: 'a@b.com', username: 'u', role: 'USER' });
      const service = createService();
      expect(service.isLoggedIn()).toBe(true);
    });

    it('isLoggedIn deve ser false quando token ausente', () => {
      const service = createService();
      expect(service.isLoggedIn()).toBe(false);
    });

    it('isAdmin deve ser true quando role é ADMIN', () => {
      store['accessToken'] = 'token';
      store['user'] = JSON.stringify({ id: '1', email: 'a@b.com', username: 'admin', role: 'ADMIN' });
      const service = createService();
      expect(service.isAdmin()).toBe(true);
    });

    it('isAdmin deve ser false quando role é USER', () => {
      store['accessToken'] = 'token';
      store['user'] = JSON.stringify({ id: '1', email: 'a@b.com', username: 'user', role: 'USER' });
      const service = createService();
      expect(service.isAdmin()).toBe(false);
    });
  });
});
