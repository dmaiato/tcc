import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { MissionSummary, Mission, MissionProgress, ScenarioDetail, ScenarioSummary, CreateMissionRequest, UpdateMissionRequest } from './models/mission.model';

@Injectable({
  providedIn: 'root'
})
export class MissionService {
  private readonly api = inject(ApiService);

  getMissions(): Observable<MissionSummary[]> {
    return this.api.get<MissionSummary[]>('/missions');
  }

  getAllMissions(): Observable<Mission[]> {
    return this.api.get<Mission[]>('/missions');
  }

  getMissionById(id: string): Observable<Mission> {
    return this.api.get<Mission>(`/missions/${id}`);
  }

  getMissionAdmin(id: string): Observable<Mission> {
    return this.api.get<Mission>(`/missions/${id}/admin`);
  }

  validateMission(id: string, tuples: Record<string, unknown>[]): Observable<{ correct: boolean; feedback?: string }> {
    return this.api.post<{ correct: boolean; feedback?: string }>(`/missions/${id}/validate`, { tuples });
  }

  adminValidateMission(id: string, tuples: Record<string, unknown>[]): Observable<{ correct: boolean; feedback?: string }> {
    return this.api.post<{ correct: boolean; feedback?: string }>(`/missions/${id}/validate/admin`, { tuples });
  }

  getUserProgress(): Observable<MissionProgress[]> {
    return this.api.get<MissionProgress[]>('/users/me/progress');
  }

  getScenarios(): Observable<ScenarioSummary[]> {
    return this.api.get<ScenarioSummary[]>('/scenarios');
  }

  getScenario(id: string): Observable<ScenarioDetail> {
    return this.api.get<ScenarioDetail>(`/scenarios/${id}`);
  }

  createMission(data: CreateMissionRequest): Observable<Mission> {
    return this.api.post<Mission>('/missions', data);
  }

  updateMission(id: string, data: UpdateMissionRequest): Observable<Mission> {
    return this.api.put<Mission>(`/missions/${id}`, data);
  }

  getAdminMissions(): Observable<Mission[]> {
    return this.api.get<Mission[]>('/missions/admin');
  }

  setEnabled(id: string, enabled: boolean): Observable<Mission> {
    return this.api.put<Mission>(`/missions/${id}/enabled`, { enabled });
  }

  deleteMission(id: string): Observable<void> {
    return this.api.delete<void>(`/missions/${id}`);
  }
}