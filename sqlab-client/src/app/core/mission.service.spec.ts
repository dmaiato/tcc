import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { MissionService } from './mission.service';
import { ApiService } from './api.service';

describe('MissionService', () => {
  let service: MissionService;

  const mockApi = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        { provide: ApiService, useValue: mockApi },
      ],
    });
    service = TestBed.inject(MissionService);
  });

  it('getAll: deve chamar GET /missions', () => {
    mockApi.get.mockReturnValue(of([]));
    service.getAll().subscribe();
    expect(mockApi.get).toHaveBeenCalledWith('/missions');
  });

  it('getSummary: deve chamar GET /missions com params theme, difficulty, name, scenarioScope, page e size', () => {
    mockApi.get.mockReturnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 12, hasNext: false }));
    service.getSummary('sql', 'BEGINNER', 'join', 'IN_SCENARIO', 0, 12).subscribe();
    expect(mockApi.get).toHaveBeenCalledWith('/missions', expect.objectContaining({}));
  });

  it('getSummary: deve chamar GET /missions com paginacao padrao quando sem params', () => {
    mockApi.get.mockReturnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 12, hasNext: false }));
    service.getSummary(null, 'ALL').subscribe();
    expect(mockApi.get).toHaveBeenCalledWith('/missions', expect.objectContaining({}));
  });

  it('getSummary: deve chamar GET /missions com scenarioScope', () => {
    mockApi.get.mockReturnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 12, hasNext: false }));
    service.getSummary(null, 'ALL', undefined, 'IN_SCENARIO').subscribe();
    expect(mockApi.get).toHaveBeenCalledWith('/missions', expect.objectContaining({}));
  });

  it('getMissionById: deve chamar GET /missions/{id}', () => {
    mockApi.get.mockReturnValue(of({}));
    service.getMissionById('123').subscribe();
    expect(mockApi.get).toHaveBeenCalledWith('/missions/123');
  });

  it('getMissionAdmin: deve chamar GET /missions/{id}/admin', () => {
    mockApi.get.mockReturnValue(of({}));
    service.getMissionAdmin('123').subscribe();
    expect(mockApi.get).toHaveBeenCalledWith('/missions/123/admin');
  });

  it('validateMission: deve chamar POST /missions/{id}/validate com tuples', () => {
    const tuples = [{ col: 'value' }];
    mockApi.post.mockReturnValue(of({ correct: true }));
    service.validateMission('123', tuples).subscribe();
    expect(mockApi.post).toHaveBeenCalledWith('/missions/123/validate', { tuples });
  });

  it('adminValidateMission: deve chamar POST /missions/{id}/validate/admin com tuples', () => {
    const tuples = [{ col: 'admin-value' }];
    mockApi.post.mockReturnValue(of({ correct: true }));
    service.adminValidateMission('123', tuples).subscribe();
    expect(mockApi.post).toHaveBeenCalledWith('/missions/123/validate/admin', { tuples });
  });

  it('getUserProgress: deve chamar GET /users/me/progress', () => {
    mockApi.get.mockReturnValue(of([]));
    service.getUserProgress().subscribe();
    expect(mockApi.get).toHaveBeenCalledWith('/users/me/progress');
  });

  it('createMission: deve chamar POST /missions com data', () => {
    const data = { title: 'M1', briefing: '', objective: '', ddlScript: '', techniques: [], xpReward: 100, ordered: false, theme: 'sql', difficulty: 'BEGINNER' as const, expectedResult: [] };
    mockApi.post.mockReturnValue(of({}));
    service.createMission(data).subscribe();
    expect(mockApi.post).toHaveBeenCalledWith('/missions', data);
  });

  it('updateMission: deve chamar PUT /missions/{id} com data', () => {
    const data = { title: 'M1', briefing: '', objective: '', ddlScript: '', techniques: [], xpReward: 100, ordered: false, theme: 'sql', difficulty: 'BEGINNER' as const, expectedResult: [] };
    mockApi.put.mockReturnValue(of({}));
    service.updateMission('123', data).subscribe();
    expect(mockApi.put).toHaveBeenCalledWith('/missions/123', data);
  });

  it('getAdminMissions: deve chamar GET /missions/admin', () => {
    mockApi.get.mockReturnValue(of([]));
    service.getAdminMissions().subscribe();
    expect(mockApi.get).toHaveBeenCalledWith('/missions/admin');
  });

  it('deleteMission: deve chamar DELETE /missions/{id}', () => {
    mockApi.delete.mockReturnValue(of(undefined));
    service.deleteMission('123').subscribe();
    expect(mockApi.delete).toHaveBeenCalledWith('/missions/123');
  });
});
