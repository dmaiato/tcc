import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ProfileService, ProfileData } from './profile.service';
import { ApiService } from './api.service';
import { AuthService } from './auth/auth.service';

describe('ProfileService', () => {
  let service: ProfileService;

  const mockApi = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  };

  const mockAuthService = {};

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        { provide: ApiService, useValue: mockApi },
        { provide: AuthService, useValue: mockAuthService },
      ],
    });
    service = TestBed.inject(ProfileService);
  });

  it('deve chamar 3 endpoints no fetchProfile', () => {
    const mockUser = { id: '1', email: 'test@test.com', username: 'test', createdAt: '', xp: 0, level: 1, role: 'USER' };
    const mockProgress = [{ missionId: 'm1', completed: true, completedAt: '2024-01-01', missionTitle: 'M1' }];
    const mockSkills = { skills: ['SQL', 'JOIN'] };

    mockApi.get.mockImplementation((url: string) => {
      if (url === '/users/me') return of(mockUser);
      if (url === '/users/me/progress') return of(mockProgress);
      if (url === '/users/me/skills') return of(mockSkills);
      return of(null);
    });

    service.fetchProfile().subscribe();

    expect(mockApi.get).toHaveBeenCalledWith('/users/me');
    expect(mockApi.get).toHaveBeenCalledWith('/users/me/progress');
    expect(mockApi.get).toHaveBeenCalledWith('/users/me/skills');
  });

  it('deve mapear resultado para ProfileData extraindo skills.skills', () => {
    const mockUser = { id: '1', email: 'test@test.com', username: 'test', createdAt: '', xp: 0, level: 1, role: 'USER' };
    const mockProgress = [{ missionId: 'm1', completed: true, completedAt: '2024-01-01', missionTitle: 'M1' }];
    const mockSkills = { skills: ['SQL', 'JOIN'] };

    mockApi.get.mockImplementation((url: string) => {
      if (url === '/users/me') return of(mockUser);
      if (url === '/users/me/progress') return of(mockProgress);
      if (url === '/users/me/skills') return of(mockSkills);
      return of(null);
    });

    service.fetchProfile().subscribe(data => {
      expect(data.user).toEqual(mockUser);
      expect(data.progress).toEqual(mockProgress);
      expect(data.skills).toEqual(['SQL', 'JOIN']);
    });
  });

  it('deve atualizar signal _profile e _loading após fetchProfile', () => {
    const mockUser = { id: '1', email: 'test@test.com', username: 'test', createdAt: '', xp: 0, level: 1, role: 'USER' };
    const mockProgress: { missionId: string; completed: boolean; completedAt: string | null; missionTitle: string }[] = [];
    const mockSkills = { skills: [] };

    mockApi.get.mockImplementation((url: string) => {
      if (url === '/users/me') return of(mockUser);
      if (url === '/users/me/progress') return of(mockProgress);
      if (url === '/users/me/skills') return of(mockSkills);
      return of(null);
    });

    service.fetchProfile().subscribe(() => {
      expect(service.loading()).toBe(false);
      expect(service.profile()).toBeTruthy();
    });
  });

  it('deve manter loading true após erro no fetchProfile', () => {
    mockApi.get.mockReturnValue(throwError(() => new Error('API error')));

    service.fetchProfile().subscribe({
      error: () => {
        expect(service.loading()).toBe(true);
      },
    });
  });

  describe('computed signals', () => {
    it('deve retornar solvedCount baseado em progress completed', () => {
      const mockUser = { id: '1', email: 'test@test.com', username: 'test', createdAt: '', xp: 0, level: 1, role: 'USER' };
      const mockProgress = [
        { missionId: 'm1', completed: true, completedAt: '2024-01-01', missionTitle: 'M1' },
        { missionId: 'm2', completed: false, completedAt: null, missionTitle: 'M2' },
        { missionId: 'm3', completed: true, completedAt: '2024-01-02', missionTitle: 'M3' },
      ];
      const mockSkills = { skills: ['SQL'] };

      mockApi.get.mockImplementation((url: string) => {
        if (url === '/users/me') return of(mockUser);
        if (url === '/users/me/progress') return of(mockProgress);
        if (url === '/users/me/skills') return of(mockSkills);
        return of(null);
      });

      service.fetchProfile().subscribe(() => {
        expect(service.solvedCount()).toBe(2);
      });
    });

    it('deve retornar skillCount baseado em skills.length', () => {
      const mockUser = { id: '1', email: 'test@test.com', username: 'test', createdAt: '', xp: 0, level: 1, role: 'USER' };
      const mockProgress: { missionId: string; completed: boolean; completedAt: string | null; missionTitle: string }[] = [];
      const mockSkills = { skills: ['SQL', 'JOIN', 'GROUP BY'] };

      mockApi.get.mockImplementation((url: string) => {
        if (url === '/users/me') return of(mockUser);
        if (url === '/users/me/progress') return of(mockProgress);
        if (url === '/users/me/skills') return of(mockSkills);
        return of(null);
      });

      service.fetchProfile().subscribe(() => {
        expect(service.skillCount()).toBe(3);
      });
    });

    it('deve retornar 0 para solvedCount quando profile é null', () => {
      expect(service.solvedCount()).toBe(0);
    });

    it('deve retornar 0 para skillCount quando profile é null', () => {
      expect(service.skillCount()).toBe(0);
    });
  });

  describe('xpForLevel / xpProgress', () => {
    it('xpForLevel deve delegar para função importada', () => {
      expect(service.xpForLevel(1)).toBe(0);
      expect(service.xpForLevel(2)).toBe(100);
      expect(service.xpForLevel(5)).toBe(1600);
    });

    it('xpProgress deve delegar para função importada', () => {
      expect(service.xpProgress(300, 2)).toBeCloseTo(66.666, 2);
    });
  });
});
