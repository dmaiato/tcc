import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ScenarioService } from './scenario.service';
import { ApiService } from './api.service';

describe('ScenarioService', () => {
  let service: ScenarioService;

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
    service = TestBed.inject(ScenarioService);
  });

  it('getAll: deve chamar GET /scenarios', () => {
    mockApi.get.mockReturnValue(of([]));
    service.getAll().subscribe();
    expect(mockApi.get).toHaveBeenCalledWith('/scenarios');
  });

  it('getById: deve chamar GET /scenarios/{id}', () => {
    mockApi.get.mockReturnValue(of({}));
    service.getById('123').subscribe();
    expect(mockApi.get).toHaveBeenCalledWith('/scenarios/123');
  });

  it('getAllAdmin: deve chamar GET /admin/scenarios', () => {
    mockApi.get.mockReturnValue(of([]));
    service.getAllAdmin().subscribe();
    expect(mockApi.get).toHaveBeenCalledWith('/admin/scenarios');
  });

  it('getAdminDetail: deve chamar GET /admin/scenarios/{id}', () => {
    mockApi.get.mockReturnValue(of({}));
    service.getAdminDetail('123').subscribe();
    expect(mockApi.get).toHaveBeenCalledWith('/admin/scenarios/123');
  });

  it('create: deve chamar POST /admin/scenarios com data', () => {
    const data = { title: 'S1', description: 'Desc', theme: 'sql', requiredLevel: 1, enabled: true };
    mockApi.post.mockReturnValue(of({}));
    service.create(data).subscribe();
    expect(mockApi.post).toHaveBeenCalledWith('/admin/scenarios', data);
  });

  it('update: deve chamar PUT /admin/scenarios/{id} com data', () => {
    const data = { title: 'S1', description: 'Updated', theme: 'sql', requiredLevel: 2, enabled: false };
    mockApi.put.mockReturnValue(of({}));
    service.update('123', data).subscribe();
    expect(mockApi.put).toHaveBeenCalledWith('/admin/scenarios/123', data);
  });

  it('delete: deve chamar DELETE /admin/scenarios/{id}', () => {
    mockApi.delete.mockReturnValue(of(undefined));
    service.delete('123').subscribe();
    expect(mockApi.delete).toHaveBeenCalledWith('/admin/scenarios/123');
  });

  it('reorderMissions: deve chamar PUT /admin/scenarios/{id}/missions/reorder com data', () => {
    const data = { missionIds: ['m1', 'm2'] };
    mockApi.put.mockReturnValue(of(undefined));
    service.reorderMissions('123', data).subscribe();
    expect(mockApi.put).toHaveBeenCalledWith('/admin/scenarios/123/missions/reorder', data);
  });
});
