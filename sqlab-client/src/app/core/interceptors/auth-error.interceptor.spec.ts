import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from '../auth/auth.service';
import { authErrorInterceptor } from './auth-error.interceptor';

describe('authErrorInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  const mockAuthService = {
    logout: vi.fn(),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authErrorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: mockAuthService },
      ],
    });
    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    vi.clearAllMocks();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve propagar erro 401 em URL /auth/ (pass-through)', () => {
    let receivedError = false;

    httpClient.get('/auth/login').subscribe({
      error: () => { receivedError = true; },
    });

    const req = httpMock.expectOne('/auth/login');
    req.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(receivedError).toBe(true);
    expect(mockAuthService.logout).not.toHaveBeenCalled();
  });

  it('deve chamar logout e completar sem erro em 401 em URL não-auth', () => {
    let completed = false;
    let errored = false;

    httpClient.get('/api/data').subscribe({
      next: () => { /* não deve ser chamado */ },
      error: () => { errored = true; },
      complete: () => { completed = true; },
    });

    const req = httpMock.expectOne('/api/data');
    req.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(mockAuthService.logout).toHaveBeenCalledOnce();
    expect(completed).toBe(true);
    expect(errored).toBe(false);
  });

  it('deve propagar erro 403 em URL /auth/ (pass-through)', () => {
    let receivedError = false;

    httpClient.post('/auth/register', {}).subscribe({
      error: () => { receivedError = true; },
    });

    const req = httpMock.expectOne('/auth/register');
    req.flush({}, { status: 403, statusText: 'Forbidden' });

    expect(receivedError).toBe(true);
    expect(mockAuthService.logout).not.toHaveBeenCalled();
  });

  it('deve propagar erro 403 com code MISSION_LOCKED (pass-through)', () => {
    let receivedError = false;

    httpClient.get('/api/missions/1').subscribe({
      error: () => { receivedError = true; },
    });

    const req = httpMock.expectOne('/api/missions/1');
    req.flush({ code: 'MISSION_LOCKED' }, { status: 403, statusText: 'Forbidden' });

    expect(receivedError).toBe(true);
    expect(mockAuthService.logout).not.toHaveBeenCalled();
  });

  it('deve propagar erro 403 com code LEVEL_REQUIRED (pass-through)', () => {
    let receivedError = false;

    httpClient.get('/api/missions/2').subscribe({
      error: () => { receivedError = true; },
    });

    const req = httpMock.expectOne('/api/missions/2');
    req.flush({ code: 'LEVEL_REQUIRED' }, { status: 403, statusText: 'Forbidden' });

    expect(receivedError).toBe(true);
    expect(mockAuthService.logout).not.toHaveBeenCalled();
  });

  it('deve chamar logout e completar sem erro em 403 com code desconhecido', () => {
    let completed = false;
    let errored = false;

    httpClient.get('/api/admin').subscribe({
      error: () => { errored = true; },
      complete: () => { completed = true; },
    });

    const req = httpMock.expectOne('/api/admin');
    req.flush({ code: 'UNKNOWN_ERROR' }, { status: 403, statusText: 'Forbidden' });

    expect(mockAuthService.logout).toHaveBeenCalledOnce();
    expect(completed).toBe(true);
    expect(errored).toBe(false);
  });

  it('deve chamar logout e completar sem erro em 403 sem code no body', () => {
    let completed = false;
    let errored = false;

    httpClient.get('/api/admin').subscribe({
      error: () => { errored = true; },
      complete: () => { completed = true; },
    });

    const req = httpMock.expectOne('/api/admin');
    req.flush({}, { status: 403, statusText: 'Forbidden' });

    expect(mockAuthService.logout).toHaveBeenCalledOnce();
    expect(completed).toBe(true);
    expect(errored).toBe(false);
  });

  it('deve propagar erro 500 (outros status) como pass-through', () => {
    let receivedError = false;

    httpClient.get('/api/data').subscribe({
      error: () => { receivedError = true; },
    });

    const req = httpMock.expectOne('/api/data');
    req.flush({}, { status: 500, statusText: 'Internal Server Error' });

    expect(receivedError).toBe(true);
    expect(mockAuthService.logout).not.toHaveBeenCalled();
  });
});
