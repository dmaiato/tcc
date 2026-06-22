import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from '../auth/auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  const mockAuthService = {
    getToken: vi.fn(),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
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

  it('deve passar requisição sem token quando URL contém /auth/', () => {
    mockAuthService.getToken.mockReturnValue('token123');

    httpClient.get('/auth/login').subscribe();

    const req = httpMock.expectOne('/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
    expect(mockAuthService.getToken).not.toHaveBeenCalled();
    req.flush({});
  });

  it('deve adicionar header Authorization com Bearer token quando token existe', () => {
    mockAuthService.getToken.mockReturnValue('token123');

    httpClient.post('/api/data', { name: 'test' }).subscribe();

    const req = httpMock.expectOne('/api/data');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token123');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'test' });
    req.flush({});
  });

  it('deve passar requisição sem header Authorization quando token é null', () => {
    mockAuthService.getToken.mockReturnValue(null);

    httpClient.get('/api/data').subscribe();

    const req = httpMock.expectOne('/api/data');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('deve manter URL e headers originais na requisição repassada', () => {
    mockAuthService.getToken.mockReturnValue('mytoken');

    httpClient.get('/api/users').subscribe();

    const req = httpMock.expectOne('/api/users');
    expect(req.request.url).toBe('/api/users');
    expect(req.request.headers.get('Authorization')).toBe('Bearer mytoken');
    req.flush([]);
  });
});
