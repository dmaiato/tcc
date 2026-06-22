import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from './auth.service';
import { adminGuard } from './admin.guard';

describe('adminGuard', () => {
  const mockAuthService = {
    isAdmin: vi.fn(),
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

  it('deve permitir acesso quando usuário é admin', () => {
    mockAuthService.isAdmin.mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() => adminGuard(mockRoute, mockState));

    expect(result).toBe(true);
  });

  it('deve redirecionar para /dashboard quando usuário é estudante', () => {
    mockAuthService.isAdmin.mockReturnValue(false);
    const urlTree = {} as UrlTree;
    mockRouter.createUrlTree.mockReturnValue(urlTree);

    const result = TestBed.runInInjectionContext(() => adminGuard(mockRoute, mockState));

    expect(mockRouter.createUrlTree).toHaveBeenCalledWith(['/dashboard']);
    expect(result).toBe(urlTree);
  });
});
