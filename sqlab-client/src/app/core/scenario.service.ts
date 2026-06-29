import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  AdminScenarioPage,
  ScenarioPage,
  ScenarioResponse,
  ScenarioSummary,
  ScenarioDetail,
  ScenarioAdminDetail,
  CreateScenarioRequest,
  UpdateScenarioRequest,
  ReorderMissionsRequest
} from './models/mission.model';

@Injectable({
  providedIn: 'root'
})
export class ScenarioService {
  private readonly api = inject(ApiService);

  getAll(): Observable<ScenarioSummary[]> {
    return this.api.get<ScenarioSummary[]>('/scenarios');
  }

  getPaged(name?: string, theme?: string, page: number = 0, size: number = 12): Observable<ScenarioPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (name) {
      params = params.set('name', name);
    }
    if (theme) {
      params = params.set('theme', theme);
    }
    return this.api.get<ScenarioPage>('/scenarios', params);
  }

  getById(id: string): Observable<ScenarioDetail> {
    return this.api.get<ScenarioDetail>(`/scenarios/${id}`);
  }

  getAllAdmin(): Observable<ScenarioResponse[]> {
    return this.api.get<ScenarioResponse[]>('/admin/scenarios');
  }

  getAdminScenariosPaged(name?: string, theme?: string, enabled?: boolean, page: number = 0, size: number = 12): Observable<AdminScenarioPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (name) {
      params = params.set('name', name);
    }
    if (theme) {
      params = params.set('theme', theme);
    }
    if (enabled !== undefined) {
      params = params.set('enabled', String(enabled));
    }
    return this.api.get<AdminScenarioPage>('/admin/scenarios', params);
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
