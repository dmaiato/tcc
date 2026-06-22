import { TestBed } from '@angular/core/testing';
import { HttpParams, HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ApiService, ApiError } from './api.service';
import { environment } from '../../environments/environment';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve retornar baseUrl igual a environment.apiUrl', () => {
    expect(service.baseUrl).toBe(environment.apiUrl);
  });

  describe('GET', () => {
    it('deve concatenar endpoint com apiUrl', () => {
      const mockData = { id: 1, name: 'test' };
      service.get<{ id: number; name: string }>('/test').subscribe(data => {
        expect(data).toEqual(mockData);
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/test`);
      expect(req.request.method).toBe('GET');
      req.flush(mockData);
    });

    it('deve passar HttpParams na requisição', () => {
      const params = new HttpParams().set('key', 'value');
      service.get('/test', params).subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/test?key=value`);
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('key')).toBe('value');
      req.flush({});
    });
  });

  describe('POST', () => {
    it('deve chamar http.post com endpoint e body', () => {
      const body = { name: 'test' };
      service.post('/test', body).subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/test`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(body);
      req.flush({});
    });
  });

  describe('PUT', () => {
    it('deve chamar http.put com endpoint e body', () => {
      const body = { name: 'updated' };
      service.put('/test/1', body).subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/test/1`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(body);
      req.flush({});
    });
  });

  describe('DELETE', () => {
    it('deve chamar http.delete com endpoint', () => {
      service.delete('/test/1').subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/test/1`);
      expect(req.request.method).toBe('DELETE');
      req.flush({});
    });
  });

  describe('handleError', () => {
    it('deve retornar erro para status 0 sem mensagem do servidor', () => {
      service.get('/test').subscribe({
        error: (err: ApiError) => {
          expect(err.status).toBe(0);
          expect(err.message).toContain('Error');
        },
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/test`);
      req.error(new ProgressEvent('error'), {
        status: 0,
        statusText: 'Unknown Error',
      });
    });

    it('deve retornar { status: 400, message: "Bad request" }', () => {
      service.get('/test').subscribe({
        error: (err: ApiError) => {
          expect(err).toEqual({ status: 400, message: 'Bad request' });
        },
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/test`);
      req.flush({}, { status: 400, statusText: 'Bad Request' });
    });

    it('deve retornar { status: 401, message: "Unauthorized" }', () => {
      service.get('/test').subscribe({
        error: (err: ApiError) => {
          expect(err).toEqual({ status: 401, message: 'Unauthorized' });
        },
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/test`);
      req.flush({}, { status: 401, statusText: 'Unauthorized' });
    });

    it('deve retornar { status: 403, message: "Forbidden" }', () => {
      service.get('/test').subscribe({
        error: (err: ApiError) => {
          expect(err).toEqual({ status: 403, message: 'Forbidden' });
        },
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/test`);
      req.flush({}, { status: 403, statusText: 'Forbidden' });
    });

    it('deve retornar { status: 404, message: "Resource not found" }', () => {
      service.get('/test').subscribe({
        error: (err: ApiError) => {
          expect(err).toEqual({ status: 404, message: 'Resource not found' });
        },
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/test`);
      req.flush({}, { status: 404, statusText: 'Not Found' });
    });

    it('deve retornar { status: 500, message: "Server error" }', () => {
      service.get('/test').subscribe({
        error: (err: ApiError) => {
          expect(err).toEqual({ status: 500, message: 'Server error' });
        },
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/test`);
      req.flush({}, { status: 500, statusText: 'Server Error' });
    });

    it('deve usar mensagem do servidor quando error.error.message existe', () => {
      service.get('/test').subscribe({
        error: (err: ApiError) => {
          expect(err).toEqual({ status: 400, message: 'Custom server message' });
        },
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/test`);
      req.flush({ message: 'Custom server message' }, { status: 400, statusText: 'Bad Request' });
    });
  });
});
