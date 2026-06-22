import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, UrlSegment } from '@angular/router';
import { AuthService } from './auth.service';
import { authGuard, guestGuard } from './auth.guard';

describe('authGuard', () => {
  const mockAuthService = {
    isLoggedIn: vi.fn(),
  };

  const mockRouter = {
    createUrlTree: vi.fn(),
  };

  function createMockRoute(urlPaths: string[]): ActivatedRouteSnapshot {
    return {
      url: urlPaths.map(p => ({ path: p, toString: () => p } as UrlSegment)),
    } as ActivatedRouteSnapshot;
  }

  const mockState = {} as RouterStateSnapshot;

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
    });
  });

  it('deve permitir acesso quando usuário está logado', () => {
    mockAuthService.isLoggedIn.mockReturnValue(true);
    const route = createMockRoute(['dashboard']);

    const result = TestBed.runInInjectionContext(() => authGuard(route, mockState));

    expect(result).toBe(true);
  });

  it('deve redirecionar para /login com returnUrl quando não logado', () => {
    mockAuthService.isLoggedIn.mockReturnValue(false);
    const urlTree = {} as UrlTree;
    mockRouter.createUrlTree.mockReturnValue(urlTree);
    const route = createMockRoute(['dashboard']);

    const result = TestBed.runInInjectionContext(() => authGuard(route, mockState));

    expect(mockRouter.createUrlTree).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: 'dashboard' },
    });
    expect(result).toBe(urlTree);
  });

  it('deve incluir caminho completo no returnUrl para rotas aninhadas', () => {
    mockAuthService.isLoggedIn.mockReturnValue(false);
    const urlTree = {} as UrlTree;
    mockRouter.createUrlTree.mockReturnValue(urlTree);
    const route = createMockRoute(['admin', 'missions']);

    TestBed.runInInjectionContext(() => authGuard(route, mockState));

    expect(mockRouter.createUrlTree).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: 'admin/missions' },
    });
  });
});

describe('guestGuard', () => {
  const mockAuthService = {
    isLoggedIn: vi.fn(),
  };

  const mockRouter = {
    createUrlTree: vi.fn(),
  };

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = {} as RouterStateSnapshot;

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
    });
  });

  it('deve permitir acesso quando usuário NÃO está logado', () => {
    mockAuthService.isLoggedIn.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() => guestGuard(mockRoute, mockState));

    expect(result).toBe(true);
  });

  it('deve redirecionar para /dashboard quando usuário está logado', () => {
    mockAuthService.isLoggedIn.mockReturnValue(true);
    const urlTree = {} as UrlTree;
    mockRouter.createUrlTree.mockReturnValue(urlTree);

    const result = TestBed.runInInjectionContext(() => guestGuard(mockRoute, mockState));

    expect(mockRouter.createUrlTree).toHaveBeenCalledWith(['/dashboard']);
    expect(result).toBe(urlTree);
  });
});
