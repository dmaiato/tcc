import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  ScenarioResponse,
  ScenarioSummary,
  ScenarioDetail,
  ScenarioAdminDetail,
  CreateScenarioRequest,
  UpdateScenarioRequest,
  ReorderMissionsRequest
} from './models/scenario.model';

@Injectable({
  providedIn: 'root'
})
export class ScenarioService {
  private readonly api = inject(ApiService);

  getAll(): Observable<ScenarioSummary[]> {
    return this.api.get<ScenarioSummary[]>('/scenarios');
  }

  getById(id: string): Observable<ScenarioDetail> {
    return this.api.get<ScenarioDetail>(`/scenarios/${id}`);
  }

  getAllAdmin(): Observable<ScenarioResponse[]> {
    return this.api.get<ScenarioResponse[]>('/admin/scenarios');
  }

  getByIdAdmin(id: string): Observable<ScenarioResponse> {
    return this.api.get<ScenarioResponse>(`/admin/scenarios/${id}`);
  }

  getAdminDetail(id: string): Observable<ScenarioAdminDetail> {
    return this.api.get<ScenarioAdminDetail>(`/admin/scenarios/${id}`);
  }

  create(data: CreateScenarioRequest): Observable<ScenarioResponse> {
    return this.api.post<ScenarioResponse>('/admin/scenarios', data);
  }

  update(id: string, data: UpdateScenarioRequest): Observable<ScenarioResponse> {
    return this.api.put<ScenarioResponse>(`/admin/scenarios/${id}`, data);
  }

  delete(id: string): Observable<void> {
    return this.api.delete<void>(`/admin/scenarios/${id}`);
  }

  reorderMissions(scenarioId: string, data: ReorderMissionsRequest): Observable<void> {
    return this.api.put<void>(`/admin/scenarios/${scenarioId}/missions/reorder`, data);
  }
}
